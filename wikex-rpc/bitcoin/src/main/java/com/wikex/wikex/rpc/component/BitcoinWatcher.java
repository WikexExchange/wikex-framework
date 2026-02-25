package com.wikex.wikex.rpc.component;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.rpc.entity.Deposit;
import com.wikex.wikex.rpc.service.AccountService;
import com.spark.blockchain.rpcclient.Bitcoin;
import com.spark.blockchain.rpcclient.BitcoinRPCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class BitcoinWatcher extends Watcher{
    @Autowired
    private BitcoinRPCClient rpcClient;

    @Autowired
    private AccountService accountService;
    private Logger logger = LoggerFactory.getLogger(Watcher.class);
    @Override
    public List<Deposit> replayBlock(Long startBlockNumber, Long endBlockNumber) {
        List<Deposit> deposits = new ArrayList<Deposit>();
        try {
            for (Long blockHeight = startBlockNumber; blockHeight <= endBlockNumber; blockHeight++) {
                String blockHash = rpcClient.getBlockHash(blockHeight.intValue());

                // Get block
                Bitcoin.Block block = rpcClient.getBlock(blockHash);
                List<String> txids = block.tx();
                logger.info("Get block (" + blockHeight + ") transaction list, total transactions: " + txids.size());

                // Iterate over transactions in the block
                for (String txid : txids) {
                    Bitcoin.RawTransaction transaction = rpcClient.getRawTransaction(txid);
                    List<Bitcoin.RawTransaction.Out> outs = transaction.vOut();

                    if (outs != null) {
                        for (Bitcoin.RawTransaction.Out out : outs) {
                            if (out.scriptPubKey() != null) {
                                List<String> addresses = out.scriptPubKey().addresses();
                                if(addresses != null && addresses.size() > 0) {
                                    String address = out.scriptPubKey().addresses().get(0);
                                    BigDecimal amount = new BigDecimal(out.value());

                                    if (accountService.isAddressExist(address)) {
                                        System.out.println(JSON.toJSON(transaction));
                                        logger.info("Detected deposit address (" + address + "), deposit amount: "
                                                + amount + " BTC");

                                        Deposit deposit = new Deposit();
                                        deposit.setCoinName("BTC");
                                        deposit.setProtocol("BTC");
                                        deposit.setTxid(transaction.txId());
                                        deposit.setBlockHeight((long) block.height());
                                        deposit.setBlockHash(transaction.blockHash());
                                        deposit.setAmount(amount.setScale(8, RoundingMode.HALF_UP));
                                        deposit.setAddress(address);
                                        String fromAddress = getFromAddress(outs, address);
                                        deposit.setFromAddress(fromAddress);
                                        deposit.setTime(transaction.time());

                                        deposits.add(deposit);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return deposits;
    }

    private String getFromAddress(List<Bitcoin.RawTransaction.Out> outs, String toAddress) {
        if(outs != null) {
            for (Bitcoin.RawTransaction.Out out : outs) {
                if (out.scriptPubKey() != null) {
                    List<String> addresses = out.scriptPubKey().addresses();
                    if(addresses != null && addresses.size() > 0) {
                        String address = out.scriptPubKey().addresses().get(0);
                        if(!address.equalsIgnoreCase(toAddress)){
                            return address;
                        }
                    }
                }
            }
        }
        return "from is null";
    }

    @Override
    public Long getNetworkBlockHeight() {
        try {
            return Long.valueOf(rpcClient.getBlockCount());
        }
        catch (Exception e){
            e.printStackTrace();
            return 0L;
        }
    }
}
