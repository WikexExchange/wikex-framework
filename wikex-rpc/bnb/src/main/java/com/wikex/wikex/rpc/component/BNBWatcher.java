package com.wikex.wikex.rpc.component;

import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.entity.Deposit;
import com.wikex.wikex.rpc.event.DepositEvent;
import com.wikex.wikex.rpc.service.AccountService;
import com.wikex.wikex.rpc.service.EthService;
import com.wikex.wikex.rpc.util.EthConvert;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Component
public class BNBWatcher extends Watcher {
    private Logger logger = LoggerFactory.getLogger(BNBWatcher.class);
    @Autowired
    private Web3j web3j;
    @Autowired
    private EthService ethService;
    @Autowired
    private AccountService accountService;

    @Autowired
    private DepositEvent depositEvent;


    @Override
    public List<Deposit> replayBlock(Long startBlockNumber, Long endBlockNumber) {

        List<Deposit> deposits = new ArrayList<>();
        for (Long blockHeight = startBlockNumber; blockHeight <= endBlockNumber; blockHeight++) {
            EthBlock block = null;
            try {
                logger.info("ethGetBlockByNumber {}", blockHeight);
                block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockHeight), true).send();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<EthBlock.TransactionResult> transactionResults = block.getBlock().getTransactions();
            logger.info("replayBlock: Height({}) - Transactions count({})", blockHeight, transactionResults.size());
            for (EthBlock.TransactionResult transactionResult : transactionResults) {
                EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                Transaction transaction = transactionObject.get();
                String to = transaction.getTo();
                if (StringUtils.isNotEmpty(to)) {
                    if (ContractsConfig.isContractsExist(to)) {
                        // token
                        tokenDeposits(deposits, transaction, blockHeight);
                    } else {
                        // bnb native coin
                        ethDeposits(deposits, transaction, to);
                    }
                }

            }
        }
        return deposits;
    }

    private void ethDeposits(List<Deposit> deposits, Transaction transaction, String to) {
        String from = transaction.getFrom();
        if (StringUtils.isNotEmpty(to)
                && accountService.isAddressExist(transaction.getTo())
                && !from.equalsIgnoreCase(getCoin().getIgnoreFromAddress())) {
            Deposit deposit = new Deposit();
            deposit.setCoinName("BNB");
            deposit.setTxid(transaction.getHash());
            deposit.setBlockHeight(transaction.getBlockNumber().longValue());
            deposit.setBlockHash(transaction.getBlockHash());
            deposit.setAmount(Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER));
            deposit.setAddress(transaction.getTo());
            deposit.setFromAddress(from);
            deposit.setProtocol("BNB");
            deposits.add(deposit);
            logger.info("received coin {} at height {}", transaction.getValue(), transaction.getBlockNumber());
            // Sync balance
            try {
                ethService.syncAddressBalance(deposit.getAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // If the transaction is sent from an address in the address book, sync its balance
        if (StringUtils.isNotEmpty(transaction.getTo()) && accountService.isAddressExist(transaction.getFrom())) {
            logger.info("sync address:{} balance", transaction.getFrom());
            try {
                ethService.syncAddressBalance(transaction.getFrom());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void tokenDeposits(List<Deposit> deposits, Transaction transaction, Long blockHeight) {
        String input = transaction.getInput();
        String cAddress = transaction.getTo();
        String from = transaction.getFrom();
        Contract contract = ContractsConfig.getContractByAddress(cAddress);

        if (StringUtils.isNotEmpty(input) && input.length() >= 138) {
            String data = input.substring(0, 9);
            data = data + input.substring(17, input.length());
            Function function = new Function("transfer", Arrays.asList(), Arrays.asList(new TypeReference<Address>() {
            }, new TypeReference<Uint256>() {
            }));

            List<Type> params = FunctionReturnDecoder.decode(data, function.getOutputParameters());
            // Deposit address
            String toAddress = params.get(0).getValue().toString();
            String amount = params.get(1).getValue().toString();
            if (accountService.isAddressExist(toAddress)) {
                logger.info("============> Find a deposit address: " + toAddress + ", amount: " + amount);
                // When eventTopic0 parameter is not empty, check event_log result to prevent fake deposits in lower token versions
//                if(org.apache.commons.lang3.StringUtils.isNotEmpty(contract.getEventTopic0()) && etherscanApi != null){
//                    boolean checkEvent = etherscanApi.checkEventLog(blockHeight,contract.getAddress(),contract.getEventTopic0(),transaction.getHash());
//                    if(!checkEvent) return;
//                }
                logger.info("Transaction is Deposit: {} ", transaction.getHash());
                // Get deposit information
                if (StringUtils.isNotEmpty(amount)) {
                    Deposit deposit = new Deposit();
                    deposit.setCoinName(contract.getName());
                    deposit.setTxid(transaction.getHash());
                    deposit.setBlockHash(transaction.getBlockHash());
                    deposit.setAmount(EthConvert.fromWei(amount, contract.getUnit()));
                    deposit.setAddress(toAddress);
                    deposit.setFromAddress(from);
                    deposit.setProtocol("BNB");
                    deposit.setTime(Calendar.getInstance().getTime());
                    logger.info("receive {} {}", deposit.getAmount(), getCoin().getUnit());
                    deposit.setBlockHeight(transaction.getBlockNumber().longValue());
                    deposits.add(deposit);
                }
            }
        }
    }


    public synchronized int replayBlockInit(Long startBlockNumber, Long endBlockNumber) throws IOException {
        int count = 0;
        for (Long i = startBlockNumber; i <= endBlockNumber; i++) {
            EthBlock block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send();

            block.getBlock().getTransactions().stream().forEach(transactionResult -> {
                EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                Transaction transaction = transactionObject.get();
                if (StringUtils.isNotEmpty(transaction.getTo())
                        && accountService.isAddressExist(transaction.getTo())
                        && !transaction.getFrom().equalsIgnoreCase(getCoin().getIgnoreFromAddress())) {
                    Deposit deposit = new Deposit();
                    deposit.setTxid(transaction.getHash());
                    deposit.setBlockHeight(transaction.getBlockNumber().longValue());
                    deposit.setBlockHash(transaction.getBlockHash());
                    deposit.setAmount(Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER));
                    deposit.setAddress(transaction.getTo());
                    logger.info("received coin {} at height {}", transaction.getValue(), transaction.getBlockNumber());
                    depositEvent.onConfirmed(deposit);
                    // Sync balance
                    try {
                        ethService.syncAddressBalance(deposit.getAddress());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // If the transaction is sent from an address in the address book, sync its balance
                if (StringUtils.isNotEmpty(transaction.getTo()) && accountService.isAddressExist(transaction.getFrom())) {
                    logger.info("sync address:{} balance", transaction.getFrom());
                    try {
                        ethService.syncAddressBalance(transaction.getFrom());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return count;
    }

    @Override
    public Long getNetworkBlockHeight() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber().longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
