package com.wikex.wikex.rpc.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.entity.Payment;
import com.wikex.wikex.rpc.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tron.utils.TronUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * TRX and Token payment module. Supports both synchronous and asynchronous tasks.
 * When consecutive payments may occur from a single address, use the async queue.
 */
@Component
public class PaymentHandler {
    private Logger logger = LoggerFactory.getLogger(PaymentHandler.class);

    @Autowired(required = false)
    private Contract contract;
    @Autowired
    private TRC20Service trc20Service;
    @Autowired
    private Coin coin;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired(required = false)
    private Payment current;
    private LinkedList<Payment> tasks = new LinkedList<>();
    private int checkTimes = 0;
    private int maxCheckTimes = 100;

    public void transferTokenAsync(Contract contract, String privateKey, String to, BigDecimal amount, String withdrawId){
        Payment payment = new Payment();
        payment.setPrivateKey(privateKey);
        payment.setAmount(amount);
        payment.setTo(to);
        payment.setContract(contract);
        payment.setTxBizNumber(withdrawId);
        payment.setUnit(coin.getUnit());
        synchronized (tasks) {
            tasks.addLast(payment);
        }
    }

    public void notify(Payment payment, int status){
        JSONObject json = new JSONObject();
        json.put("withdrawId", payment.getTxBizNumber());
        json.put("txid", payment.getTxid());
        json.put("status", status);
        rocketMQTemplate.convertAndSend("withdraw-notify", JSON.toJSONString(json));
    }

    public void transferTRXAsync(String privateKey, String to, BigDecimal amount, String withdrawId){
        Payment payment = new Payment();
        payment.setPrivateKey(privateKey);
        payment.setAmount(amount);
        payment.setTo(to);
        payment.setUnit("TRX");
        payment.setTxBizNumber(withdrawId);
        synchronized (tasks) {
            tasks.addLast(payment);
        }
    }

    public MessageResult transferTRX(String privateKey, String to, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setPrivateKey(privateKey);
        payment.setAmount(amount);
        payment.setTo(to);
        payment.setUnit("TRX");
        return transferTRX(payment);
    }

    public MessageResult transferTRX(Payment payment) {
        try {
            String ownerAddress = TronUtils.getAddressByPrivateKey(payment.getPrivateKey());
            logger.info("from={},value={},address={}", ownerAddress, payment.getAmount(), payment.getTo());
            String transactionHash = null;
            try {
                BigDecimal balance = trc20Service.getTRXBalance(ownerAddress);
                if (balance.compareTo(payment.getAmount()) == -1) {
                    return new MessageResult(500, "Insufficient balance");
                }
                transactionHash = trc20Service.sendTrx(payment.getPrivateKey(), payment.getTo(), payment.getAmount());
            } catch (Throwable throwable) {
                logger.info("Error occurred {}", throwable.getMessage());
                throwable.printStackTrace();
            }
            logger.info("txid:" + transactionHash);
            if (StringUtils.isEmpty(transactionHash)) {
                logger.info("ownerAddress:{},privateKey::{}", ownerAddress, payment.getPrivateKey());
                logger.info("Failed to send transaction!");
                return new MessageResult(500, "Failed to send transaction");
            } else {
                payment.setTxid(transactionHash);
                MessageResult mr = new MessageResult(0, "success");
                mr.setData(transactionHash);
                return mr;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResult(500, "Transaction failed, error:" + e.getMessage());
        }
    }

    public MessageResult transferToken(Payment payment){
        try {
            String ownerAddress = TronUtils.getAddressByPrivateKey(payment.getPrivateKey());
            logger.info("from={},value={},address={}", ownerAddress, payment.getAmount(), payment.getTo());
            String transactionHash = null;
            try {
                BigDecimal balance = trc20Service.getTokenBalance(ownerAddress, payment.getContract());
                if (balance.compareTo(payment.getAmount()) == -1) {
                    return new MessageResult(500, "Insufficient balance");
                }
                transactionHash = trc20Service.sendTrc20Token(payment.getContract(), payment.getPrivateKey(), payment.getTo(), payment.getAmount());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            logger.info("txid:" + transactionHash);
            if (StringUtils.isEmpty(transactionHash)) {
                logger.info("Failed to send transaction!");
                return new MessageResult(500, "Failed to send transaction");
            } else {
                payment.setTxid(transactionHash);
                MessageResult mr = new MessageResult(0, "success");
                mr.setData(transactionHash);
                return mr;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResult(500, "Transaction failed, error:" + e.getMessage());
        }
    }

    public MessageResult transferToken(Contract contract, String privateKey, String to, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setPrivateKey(privateKey);
        payment.setAmount(amount);
        payment.setTo(to);
        payment.setContract(contract);
        payment.setUnit(coin.getUnit());
        return transferToken(payment);
    }

    /**
     * Check whether the current task has been paid successfully.
     */
    @Scheduled(cron = "0/30 * * * * *")
    public synchronized void checkJob(){
        logger.info("Checking payment task status");
        // && StringUtils.isNotEmpty(current.getTxid())
        if (current != null) {
            synchronized (current) {
                try {
                    checkTimes++;
                    if (trc20Service.isTransactionSuccess(current.getTxid())) {
                        logger.info("Transfer {} succeeded, check times: {}", JSON.toJSON(current), checkTimes);
                        notify(current, 1);
                        current = null;
                    }
                    else{
                        logger.info("Transfer {} not successful, check times: {}", JSON.toJSON(current), checkTimes);
                        if (checkTimes > maxCheckTimes){
                            // Timeout without success
                            notify(current, 0);
                            current = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            logger.info("No pending tasks to confirm");
        }
    }

    public MessageResult transfer(Payment payment){
        if (payment.getUnit().equalsIgnoreCase("TRX")){
            return transferTRX(payment);
        } else {
            return transferToken(payment);
        }
    }

    @Scheduled(cron = "0/30 * * * * *")
    public synchronized void doJob(){
        synchronized (tasks) {
            logger.info("Start executing payment tasks, current queue length {}", tasks.size());
            if (current == null && tasks.size() > 0) {
                logger.info("Start executing payment task: current---" + JSONObject.toJSONString(current));
                Payment payment = tasks.getFirst();
                MessageResult result = transfer(payment);
                if (result.getCode() == 0) {
                    logger.info("------txID:" + result.getData().toString());
                    payment.setTxid(result.getData().toString());
                    tasks.removeFirst();
                    current = payment;
                    checkTimes = 0;
                }
            }
        }
    }
}
