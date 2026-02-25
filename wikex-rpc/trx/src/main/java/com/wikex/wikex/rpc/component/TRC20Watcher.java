package com.wikex.wikex.rpc.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.entity.Deposit;
import com.wikex.wikex.rpc.entity.TokenInputData;
import com.wikex.wikex.rpc.event.DepositEvent;
import com.wikex.wikex.rpc.service.AccountService;
import com.wikex.wikex.rpc.service.TRC20Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class TRC20Watcher extends Watcher {
    private Logger logger = LoggerFactory.getLogger(TRC20Watcher.class);

    @Autowired
    private AccountService accountService;
    @Autowired(required = false)
    private TRC20Service trc20Service;

    @Autowired
    private DepositEvent depositEvent;

    /**
     * Replay historical blocks and extract deposit transactions.
     *
     * @param startBlockNumber Starting block height
     * @param endBlockNumber   Ending block height
     * @return List of deposit records
     */
    public List<Deposit> replayBlock(Long startBlockNumber, Long endBlockNumber) {
        List<Deposit> deposits = new ArrayList<>();
        for (Long blockHeight = startBlockNumber; blockHeight <= endBlockNumber; blockHeight++) {
            logger.info("trcGetBlockByNumber {}", blockHeight);
            String blockJson = trc20Service.getBlockByNum(BigInteger.valueOf(blockHeight));
            JSONObject block = JSON.parseObject(blockJson);
            JSONArray jsonArray = block.getJSONArray("transactions");
            String blockId = block.getString("blockID");
            if (jsonArray != null && jsonArray.size() > 0) {
                logger.info("replayBlock: Height({}) - Transactions count({})", blockHeight, jsonArray.size());
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String res = object.getJSONArray("ret").getJSONObject(0).getString("contractRet");
                    if ("SUCCESS".equalsIgnoreCase(res)) {
                        JSONObject contractObject = object.getJSONObject("raw_data")
                                .getJSONArray("contract").getJSONObject(0);
                        String type = contractObject.getString("type");

                        // Handle native TRX transfer transactions
                        if ("TransferContract".equalsIgnoreCase(type)) {
                            JSONObject parameter = contractObject.getJSONObject("parameter").getJSONObject("value");
                            String from = TransformUtil.encode58Check(parameter.getString("owner_address"));
                            String toAddress = TransformUtil.encode58Check(parameter.getString("to_address"));
                            BigDecimal amount = TransformUtil.formatTokenNum(parameter.getBigInteger("amount"), 6);
                            String txID = object.getString("txID");

                            if (accountService.isAddressExist(toAddress) &&
                                    !getCoin().getIgnoreFromAddress().equalsIgnoreCase(from)) {
                                // This is a deposit transaction
                                if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                                    Deposit deposit = new Deposit();
                                    deposit.setTxid(txID);
                                    deposit.setBlockHash(blockId);
                                    deposit.setCoinName("TRX");
                                    deposit.setProtocol("TRX");
                                    deposit.setAmount(amount);
                                    deposit.setAddress(toAddress);
                                    deposit.setFromAddress(from);
                                    deposit.setTime(Calendar.getInstance().getTime());
                                    logger.info("receive {} {}", deposit.getAmount(), getCoin().getUnit());
                                    deposit.setBlockHeight(blockHeight);
                                    deposits.add(deposit);
                                }
                            }
                        }
                        // Handle TRC20 token transfers
                        else if ("TriggerSmartContract".equalsIgnoreCase(type)) {
                            JSONObject parameter = contractObject.getJSONObject("parameter").getJSONObject("value");
                            String contractAddress = TransformUtil.encode58Check(parameter.getString("contract_address"));
                            String from = TransformUtil.encode58Check(parameter.getString("owner_address"));
                            String data = parameter.getString("data");

                            Contract contract = ContractsConfig.getContractByAddress(contractAddress);
                            if (data != null && contract != null &&
                                    !getCoin().getIgnoreFromAddress().equalsIgnoreCase(from)) {

                                TokenInputData tokenInputData = TransformUtil.getTokenInputData(data);
                                BigDecimal amount = TransformUtil.formatTokenNum(
                                        new BigInteger(tokenInputData.getAmount(), 16),
                                        contract.getUnit().getDecimals());
                                String toAddress = TransformUtil.encode58Check(tokenInputData.getTo());
                                String txID = object.getString("txID");

                                if (accountService.isAddressExist(toAddress) &&
                                        tokenInputData.getMethod().equals("a9059cbb")) {
                                    logger.info("Transaction is Deposit: {} ", txID);
                                    // This is a TRC20 token deposit
                                    if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                                        Deposit deposit = new Deposit();
                                        deposit.setTxid(txID);
                                        deposit.setBlockHash(blockId);
                                        deposit.setAmount(amount);
                                        deposit.setCoinName(contract.getName());
                                        deposit.setProtocol("TRX");
                                        deposit.setAddress(toAddress);
                                        deposit.setFromAddress(from);
                                        deposit.setTime(Calendar.getInstance().getTime());
                                        logger.info("receive {} {}", deposit.getAmount(), getCoin().getUnit());
                                        deposit.setBlockHeight(blockHeight);
                                        deposits.add(deposit);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                logger.info("replayBlock: Height({}) - Transactions count({})", blockHeight, 0);
            }
        }
        return deposits;
    }

    /**
     * Replay blocks during system initialization and trigger deposit event directly.
     *
     * @param startBlockNumber Starting block height
     * @param endBlockNumber   Ending block height
     */
    public synchronized void replayBlockInit(Long startBlockNumber, Long endBlockNumber) {
        for (long i = startBlockNumber; i <= endBlockNumber; i++) {
            String blockJson = trc20Service.getBlockByNum(BigInteger.valueOf(i));
            JSONObject block = JSON.parseObject(blockJson);
            JSONArray jsonArray = block.getJSONArray("transactions");
            String blockId = block.getString("blockID");
            if (jsonArray != null && jsonArray.size() > 0) {
                logger.info("replayBlockInit: Height({}) - Transactions count({})", i, jsonArray.size());
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject object = jsonArray.getJSONObject(j);
                    String res = object.getJSONArray("ret").getJSONObject(0).getString("contractRet");
                    if ("SUCCESS".equalsIgnoreCase(res)) {
                        JSONObject contractObject = object.getJSONObject("raw_data")
                                .getJSONArray("contract").getJSONObject(0);
                        String type = contractObject.getString("type");

                        // Handle TRX transfer deposits during initialization
                        if ("TransferContract".equalsIgnoreCase(type)) {
                            JSONObject parameter = contractObject.getJSONObject("parameter").getJSONObject("value");
                            String toAddress = TransformUtil.encode58Check(parameter.getString("to_address"));
                            BigDecimal amount = TransformUtil.formatTokenNum(parameter.getBigInteger("amount"), 6);
                            String txID = object.getString("txID");
                            logger.info("toAddress::{},amount::{}", toAddress, amount.toPlainString());
                            if (accountService.isAddressExist(toAddress)) {
                                logger.info("Transaction is Deposit: {} ", txID);
                                logger.info("################{}###################{}", toAddress, amount);
                                if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                                    Deposit deposit = new Deposit();
                                    deposit.setTxid(txID);
                                    deposit.setBlockHash(blockId);
                                    deposit.setAmount(amount);
                                    deposit.setAddress(toAddress);
                                    deposit.setTime(Calendar.getInstance().getTime());
                                    logger.info("receive {} {}", deposit.getAmount(), getCoin().getUnit());
                                    deposit.setBlockHeight(i);
                                    depositEvent.onConfirmed(deposit);
                                }
                            }
                        }
                    }
                }
            } else {
                logger.info("replayBlock: Height({}) - Transactions count({})", i, 0);
            }
        }
    }

    @Override
    public Long getNetworkBlockHeight() {
        try {
            BigInteger blockNumber = trc20Service.getNowBlock();
            return blockNumber.longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
