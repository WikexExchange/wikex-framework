package com.wikex.wikex.rpc.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.entity.Payment;
import com.wikex.wikex.rpc.util.EthConvert;
import com.wikex.wikex.rpc.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * ETH and Token payment module, supports synchronous and asynchronous tasks.
 * When consecutive payments may occur from a single address, an asynchronous queue is used.
 */
@Component
public class PaymentHandler {
    private Logger logger = LoggerFactory.getLogger(PaymentHandler.class);
    @Autowired
    private Web3j web3j;
    @Autowired
    private EthService ethService;
//    @Autowired(required = false)
//    private Contract contract;
    @Autowired
    private Coin coin;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired(required = false)
    private EtherscanApi etherscanApi;
    private Payment current;
    private LinkedList<Payment> tasks = new LinkedList<>();
    private int checkTimes = 0;
    private int maxCheckTimes = 100;

    public void transferTokenAsync(Credentials credentials, Contract contract, String to, BigDecimal amount, String withdrawId){
        Payment payment = Payment.builder()
                .credentials(credentials)
                .amount(amount)
                .to(to)
                .txBizNumber(withdrawId)
                .unit(contract.getName())
                .contract(contract)
                .build();
        synchronized (tasks) {
            tasks.addLast(payment);
        }
    }

    public void notify(Payment payment,int status){
        JSONObject json = new JSONObject();
        json.put("withdrawId",payment.getTxBizNumber());
        json.put("txid",payment.getTxid());
        json.put("status",status);
        json.put("name",coin.getName());
        rocketMQTemplate.convertAndSend("withdraw-notify", JSON.toJSONString(json));
    }

    public void transferEthAsync(Credentials credentials, String to, BigDecimal amount,String withdrawId){
        Payment payment = Payment.builder()
                .credentials(credentials)
                .amount(amount)
                .to(to)
                .txBizNumber(withdrawId)
                .unit("ETH")
                .build();
        synchronized (tasks) {
            tasks.addLast(payment);
        }
    }

    public MessageResult transferEth(Credentials credentials, String to, BigDecimal amount) {
        Payment payment = Payment.builder()
                .credentials(credentials)
                .amount(amount)
                .to(to)
                .unit("ETH")
                .build();
        return transferEth(payment);
    }

    public MessageResult transferEth(Payment payment) {
        try {
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    payment.getCredentials().getAddress(),
                    DefaultBlockParameterName.LATEST
            ).sendAsync().get();

            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = ethService.getGasPrice();
            BigInteger value = Convert.toWei(payment.getAmount(), Convert.Unit.ETHER).toBigInteger();

            BigInteger maxGas = coin.getGasLimit();
            logger.info("value={},gasPrice={},gasLimit={},nonce={},address={}", value, gasPrice, maxGas, nonce, payment.getTo());
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, gasPrice, maxGas, payment.getTo(), value);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, coin.getChainId(), payment.getCredentials());
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            String transactionHash = ethSendTransaction.getTransactionHash();
            logger.info("ethSendTransaction::{}",JSON.toJSONString(ethSendTransaction));
            logger.info("txid = {}", transactionHash);
            if (StringUtils.isEmpty(transactionHash) || transactionHash.equals("null")) {
                return new MessageResult(500, "Failed to send transaction");
            }
            else {
//                if(etherscanApi != null){
//                    logger.info("=====Broadcast transaction via Etherscan======");
//                    etherscanApi.sendRawTransaction(hexValue);
//                }
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
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    payment.getCredentials().getAddress(),
                    DefaultBlockParameterName.LATEST
            ).sendAsync().get();

            Contract contract = payment.getContract();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = ethService.getGasPrice();
            BigInteger value = EthConvert.toWei(payment.getAmount(), contract.getUnit()).toBigInteger();
            logger.info("Payment.to = " + payment.getTo());
            Function fn = new Function(
                    "transfer",
                    Arrays.asList(new Address(payment.getTo()), new Uint256(value)),
                    Collections.<TypeReference<?>>emptyList()
            );
            String data = FunctionEncoder.encode(fn);
            BigInteger maxGas = contract.getGasLimit();
            logger.info("from={},value={},gasPrice={},gasLimit={},nonce={},address={}",
                    payment.getCredentials().getAddress(), value, gasPrice, maxGas, nonce, payment.getTo());
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce, gasPrice, maxGas, contract.getAddress(), data);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, coin.getChainId(), payment.getCredentials());
            String hexValue = Numeric.toHexString(signedMessage);
            logger.info("hexRawValue={}",hexValue);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            String transactionHash = ethSendTransaction.getTransactionHash();
            logger.info("txid:" + transactionHash);
            logger.info("ethSendTransaction::"+JSON.toJSONString(ethSendTransaction));
            if (StringUtils.isEmpty(transactionHash)) {
                logger.info("Failed to send transaction!");
                return new MessageResult(500, "Failed to send transaction");
            }
            else {
//                if(etherscanApi != null){
//                    logger.info("=====Broadcast transaction via Etherscan======");
//                    etherscanApi.sendRawTransaction(hexValue);
//                }
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

    public MessageResult transferToken(Credentials credentials,Contract contract, String to, BigDecimal amount) {
        Payment payment = Payment.builder()
                .credentials(credentials)
                .amount(amount)
                .to(to)
                .unit(contract.getName())
                .contract(contract)
                .build();
        return transferToken(payment);
    }

    /**
     * Check whether the current task has completed payment
     */
    @Scheduled(cron = "0/30 * * * * *")
    public synchronized void checkJob(){
        logger.info("Checking payment task status");
//        && StringUtils.isNotEmpty(current.getTxid())
        if (current != null ) {
            synchronized (current) {
                try {
                    checkTimes ++;
                    if (ethService.isTransactionSuccess(current.getTxid())) {
                        logger.info("Transfer {} succeeded, check count: {}", JSON.toJSON(current),checkTimes);
                        notify(current,1);
                        current = null;
                    }
                    else{
                        logger.info("Transfer {} not successful, check count: {}", JSON.toJSON(current),checkTimes);
                        if(checkTimes > maxCheckTimes){
                            // Timed out without success
                            notify(current,0);
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
        if(payment.getUnit().equalsIgnoreCase("ETH")){
            return transferEth(payment);
        }
        else{
            return transferToken(payment);
        }
    }

    @Scheduled(cron = "0/30 * * * * *")
    public synchronized void doJob(){
        synchronized (tasks) {
            logger.info("Start executing payment tasks, current queue length {}",tasks.size());
            if (current == null && tasks.size() > 0) {
                logger.info("Start executing payment task: current---"+JSONObject.toJSONString(current));
                Payment payment = tasks.getFirst();
                MessageResult result = transfer(payment);
                if (result.getCode() == 0) {
                    logger.info("------txID:"+result.getData().toString());
                    payment.setTxid(result.getData().toString());
                    tasks.removeFirst();
                    current = payment;
                    checkTimes = 0;
                }
            }
        }
    }
}
