package com.wikex.wikex.rpc.controller;

import com.alibaba.fastjson.JSON;

import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Account;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.user.dto.ContractDTO;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.rpc.component.EthWatcher;
import com.wikex.wikex.rpc.service.AccountService;
import com.wikex.wikex.rpc.service.EthService;
import com.wikex.wikex.rpc.util.Md5;
import com.wikex.wikex.rpc.util.MessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rpc")
public class WalletController {
    private Logger logger = LoggerFactory.getLogger(WalletController.class);
    @Autowired
    private EthService service;
    @Autowired
    private Web3j web3j;
    @Autowired
    private EthWatcher watcher;
    @Autowired
    private Coin coin;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CoinFeign coinFeign;

    @GetMapping("height")
    public MessageResult getHeight() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            long rpcBlockNumber = blockNumber.getBlockNumber().longValue();
            MessageResult result = new MessageResult(0, "success");
            result.setData(rpcBlockNumber);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Query failed, error:" + e.getMessage());
        }
    }

    @GetMapping("address/{account}")
    public MessageResult getNewAddress(@PathVariable String account) {
        try {
            String address;
            Account acct = accountService.findByName("ETH", account);
            if (acct != null) {
                address = acct.getAddress();
            } else {
                logger.info("create new account={}", account);
                address = service.createNewWallet(account, coin.getWithdrawWalletPassword());
            }
            MessageResult result = new MessageResult(0, "success");
            result.setData(address);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "rpc error:" + e.getMessage());
        }

    }

    @GetMapping("transfer")
    public MessageResult transfer(String password, String address, String coinName, BigDecimal amount, BigDecimal fee) {
        try {
            if (!password.equals(coin.getWithdrawWalletPassword())) {
                return MessageResult.error(500, "Password verification failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Password verification failed");
        }
        logger.info("transfer:coinName={},address={},amount={},fee={}", coinName, address, amount, fee);
        if (coinName == null || coinName.equals("ETH")) {
            try {
                if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                    fee = service.getMinerFee(coin.getGasLimit());
                }
                MessageResult result = service.transferFromWallet(password, address, amount, fee, coin.getMinCollectAmount());
                logger.info("Returned result : " + result.toString());
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return MessageResult.error(500, "error:" + e.getMessage());
            }
        } else {
            BigDecimal transferredAmount = BigDecimal.ZERO;
            Contract contract = ContractsConfig.getContractByCoinName(coinName);
            try {
                if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                    fee = service.getMinerFee(contract.getGasLimit());
                }
                logger.info("transfer: fee = {}", fee);
                List<Account> accountList = accountService.findByBalanceAndGas(contract.getMinCollectAmount(), fee, coinName);
                logger.info("transfer: account count = {}", accountList.size());
                for (Account account : accountList) {
                    if (service.getBalance(account.getAddress()).compareTo(fee) < 0) {
                        logger.info("Address {} insufficient fee, minimum is {}", account.getAddress(), fee);
                        continue;
                    }
                    BigDecimal availAmt = service.getTokenBalance(account.getAddress(), contract);
                    if (availAmt.compareTo(contract.getMinCollectAmount()) < 0) {
                        logger.info("Address {} insufficient balance, minimum is {}", account.getAddress(), contract.getMinCollectAmount());
                        continue;
                    }
                    logger.info("from={},amount={},fee={}", account.getAddress(), availAmt, fee);
                    MessageResult result = service.transferToken(password, contract, account.getAddress(), address, availAmt, true);
                    if (result.getCode() == 0) {
                        transferredAmount = transferredAmount.add(availAmt);
                    }
                    if (transferredAmount.compareTo(amount) >= 0) break;
                }
                logger.info("Total transferred: {}", transferredAmount);
                MessageResult mr = new MessageResult(0, "Transfer successful");
                mr.setData(transferredAmount);
                return mr;
            } catch (Exception e) {
                e.printStackTrace();
                return MessageResult.error(500, "error:" + e.getMessage());
            }
        }
    }

    @GetMapping("transferAll")
    public MessageResult transferAll(String address, String coinName, String password) {
        try {
            if (!password.equals(coin.getWithdrawWalletPassword())) {
                return MessageResult.error(500, "Password verification failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Password verification failed");
        }
        return transfer(password, address, coinName, BigDecimal.valueOf(99999999999999L), BigDecimal.ZERO);
    }

    @GetMapping("withdraw")
    public MessageResult withdraw(String address, BigDecimal amount,
                                  @RequestParam(name = "sync", required = false, defaultValue = "true") Boolean sync,
                                  @RequestParam(name = "coinName", required = false, defaultValue = "true") String coinName,
                                  @RequestParam(name = "withdrawId", required = false, defaultValue = "") String withdrawId) {
        logger.info("withdraw:to={},amount={},coinName={},sync={},withdrawId={}", address, amount, coinName, sync, withdrawId);
        try {
            MessageResult result = service.transferFromWithdrawWallet(address, amount, sync, withdrawId, coinName);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
        }
    }

    @GetMapping("setPassword")
    public MessageResult setPassword(String password) throws Exception {
        String md5Digest = Md5.md5Digest(password);
        if (md5Digest.toLowerCase().equals(coin.getWithdrawWalletPasswordMd5())) {
            coin.setWithdrawWalletPassword(password);
            return MessageResult.success();
        } else {
            return MessageResult.error(500, "Password verification failed");
        }
    }

    // Sync coin types
    @GetMapping("updateContract")
    public MessageResult updateContract(String password) throws Exception {
        String md5Digest = Md5.md5Digest(password);
        if (!md5Digest.toLowerCase().equals(coin.getWithdrawWalletPasswordMd5())) {
            return MessageResult.error(500, "Password verification failed");
        }
        Map<String, String> map = new HashMap<>();
        map.put("0", "wei");
        map.put("3", "kwei");
        map.put("4", "wwei");
        map.put("6", "mwei");
        map.put("8", "lwei");
        map.put("9", "gwei");
        map.put("12", "szabo");
        map.put("15", "finney");
        map.put("18", "ether");
        map.put("21", "kether");
        map.put("24", "mether");
        map.put("27", "gether");

        List<ContractDTO> list = coinFeign.getContractByProtocol(coin.getUnit());

        if (list != null && list.size() > 0) {
            for (ContractDTO dto : list) {
                Contract contract = new Contract();
                BeanUtils.copyProperties(dto, contract);
                contract.setGasLimit(coin.getGasLimit());
                // #wei:0,kwei:3,wwei:4,mwei:6,gwei:9,szabo:12,finney:15,ether:18,kether:21,mether:24,gether:27
                contract.setDecimals(map.get(contract.getDecimals()));
                ContractsConfig.updateContract(contract);
            }
        }

        logger.info("contracts::{}", JSON.toJSONString(ContractsConfig.getContracts()));
        return MessageResult.success();
    }

    /**
     * Get total hot wallet balance
     *
     * @return
     */
    @GetMapping("balance")
    public MessageResult balance(@RequestParam(required = false, defaultValue = "ETH") String coinName) {
        try {
            BigDecimal balance = accountService.findBalanceSum(coinName);
            MessageResult result = new MessageResult(0, "success");
            result.setData(balance);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Query failed, error:" + e.getMessage());
        }
    }

    @GetMapping("balance/{address}")
    public MessageResult addressBalance(@PathVariable String address, @RequestParam(required = false, defaultValue = "ETH") String coinName) {
        try {
            BigDecimal balance = BigDecimal.ZERO;
            if (coinName.equals(coin.getUnit())) {
                balance = service.getBalance(address);
            } else {
                Contract contract = ContractsConfig.getContractByCoinName(coinName);
                balance = service.getTokenBalance(address, contract);
            }

            MessageResult result = new MessageResult(0, "success");
            result.setData(balance);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Query failed, error:" + e.getMessage());
        }
    }

    @GetMapping("transaction/{txid}")
    public MessageResult transaction(@PathVariable String txid) throws IOException {
        EthTransaction transaction = web3j.ethGetTransactionByHash(txid).send();
        EthGasPrice gasPrice = web3j.ethGasPrice().send();

        System.out.println(gasPrice.getGasPrice());
        System.out.println(transaction.getRawResponse());
        return MessageResult.success("");
    }

    @GetMapping("gas-price")
    public MessageResult gasPrice() throws IOException {
        try {
            BigInteger gasPrice = service.getGasPrice();
            MessageResult result = new MessageResult(0, "success");
            result.setData(Convert.fromWei(gasPrice.toString(), Convert.Unit.GWEI));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Query failed, error:" + e.getMessage());
        }
    }

    @GetMapping("sync-block")
    public MessageResult manualSync(Long startBlock, Long endBlock) {
        try {
            watcher.replayBlockInit(startBlock, endBlock);
        } catch (IOException e) {
            e.printStackTrace();
            return MessageResult.error(500, "Sync failed: " + e.getMessage());
        }
        return MessageResult.success();
    }
}
