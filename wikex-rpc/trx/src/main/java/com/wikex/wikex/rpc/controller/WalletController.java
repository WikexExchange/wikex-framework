package com.wikex.wikex.rpc.controller;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Account;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.service.AccountService;
import com.wikex.wikex.rpc.service.TRC20Service;
import com.wikex.wikex.rpc.util.Md5;
import com.wikex.wikex.rpc.util.MessageResult;
import com.wikex.wikex.user.dto.ContractDTO;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.rpc.component.TRC20Watcher;
import com.wikex.wikex.rpc.util.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/rpc")
@RestController
public class WalletController {

    @Autowired
    private TRC20Service service;
    @Autowired
    private AccountService accountService;
    @Autowired
    private Coin coin;
    @Autowired
    private CoinFeign coinFeign;
    @Autowired
    private TRC20Watcher watcher;
    private Logger logger = LoggerFactory.getLogger(WalletController.class);

    @GetMapping("balance")
    public MessageResult walletBalance(@RequestParam(required = false, defaultValue = "TRX") String coinName) {
        try {
            BigDecimal amt = accountService.findBalanceSum(coinName);
            MessageResult result = new MessageResult(0, "success");
            result.setData(amt);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Query failed, error: " + e.getMessage());
        }
    }

    @GetMapping("height")
    public MessageResult getHeight() {
        try {
            Long blockNumber = watcher.getCurrentBlockHeight();
            long rpcBlockNumber = blockNumber;
            MessageResult result = new MessageResult(0, "success");
            result.setData(rpcBlockNumber);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Query failed, error: " + e.getMessage());
        }
    }

    @GetMapping("balance/{address}")
    public MessageResult addressBalance(@PathVariable String address) {
        try {
            BigDecimal amt = service.getTRXBalance(address);
            MessageResult result = new MessageResult(0, "success");
            result.setData(amt);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Query failed, error: " + e.getMessage());
        }
    }

    @GetMapping("address/{account}")
    public MessageResult getNewAddress(@PathVariable String account) {
        logger.info("create new account={}", account);
        try {
            String address;
            Account old = accountService.findByName(coin.getUnit(), account);
            if (old != null) {
                MessageResult result = new MessageResult(0, "success");
                result.setData(old.getAddress());
                return result;
            } else {
                Map<String, String> map = service.createAddress();
                logger.info("address::{}", JSON.toJSONString(map));
                address = map.get("address");
                service.createAccount(address);
                String privateKey = AESUtil.encrypt(map.get("privateKey"), coin.getWithdrawWalletPassword());
                accountService.saveOne(account, "", address, privateKey);
            }
            MessageResult result = new MessageResult(0, "success");
            result.setData(address);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "RPC error: " + e.getMessage());
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

    // Synchronize coins
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

    @GetMapping("transfer-from-address")
    public MessageResult transferFromAddress(String fromAddress, String address, BigDecimal amount, BigDecimal fee) {
        logger.info("transferFromAddress:from={},to={},amount={},fee={}", fromAddress, address, amount, fee);
        try {
            MessageResult result = service.transferTRX(fromAddress, address, amount, true);
            logger.info("Return result : " + result.toString());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "error:" + e.getMessage());
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
        logger.info("transfer:address={},amount={},fee={}", address, amount, fee);
        BigDecimal transferredAmount = BigDecimal.ZERO;
        if (coinName == null || coinName.equals("TRX")) {
            try {
                List<Account> accountList = accountService.findByBalanceAndGas(coin.getMinCollectAmount(), fee, null);
                for (Account account : accountList) {
                    BigDecimal availAmt = service.getTRXBalance(account.getAddress());
                    if (availAmt.compareTo(coin.getMinCollectAmount()) < 0) {
                        logger.info("Address {} balance is insufficient, minimum is {}", account.getAddress(), coin.getMinCollectAmount());
                        continue;
                    }
                    logger.info("from={},amount={},fee={}", account.getAddress(), availAmt, fee);
                    MessageResult result = service.transferTRX(account.getAddress(), address, availAmt, true);
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
        } else {
            try {
                Contract contract = ContractsConfig.getContractByCoinName(coinName);
                List<Account> accountList = accountService.findByBalanceAndGas(contract.getMinCollectAmount(), fee, coinName);
                for (Account account : accountList) {
                    BigDecimal availAmt = service.getTokenBalance(account.getAddress(), contract);
                    if (availAmt.compareTo(contract.getMinCollectAmount()) < 0) {
                        logger.info("Address {} balance is insufficient, minimum is {}", account.getAddress(), contract.getMinCollectAmount());
                        continue;
                    }
                    logger.info("from={},amount={},fee={}", account.getAddress(), availAmt, fee);
                    MessageResult result = service.transferToken(contract, account.getAddress(), address, availAmt, true);
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

    @GetMapping("sync-block")
    public MessageResult manualSync(Long startBlock, Long endBlock) {
        try {
            watcher.replayBlockInit(startBlock, endBlock);
            return MessageResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, "Sync failed: " + e.getMessage());
        }
    }

    @GetMapping("sync-height")
    public MessageResult getCurrentSyncHeight() {
        MessageResult result = MessageResult.success();
        result.setData(watcher.getCurrentBlockHeight());
        return result;
    }
}
