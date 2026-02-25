package com.wikex.wikex.rpc.config;

import com.spark.blockchain.rpcclient.BitcoinException;
import com.spark.blockchain.rpcclient.BitcoinRPCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;

/**
 * Initialize RPC client
 */
@Configuration
public class RpcClientConfig {

    private Logger logger = LoggerFactory.getLogger(RpcClientConfig.class);

    @Bean
    public BitcoinRPCClient setClient(@Value("${coin.rpc}") String uri) {
        try {
            logger.info("uri={}", uri);
            BitcoinRPCClient client = new BitcoinRPCClient(uri);
            int blockCount = client.getBlockCount();
            logger.info("blockHeight={}", blockCount);
            return client;
        } catch (MalformedURLException e) {
            logger.info("init wallet failed");
            e.printStackTrace();
            return null;
        } catch (BitcoinException e) {
            logger.info("BitcoinException");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws MalformedURLException, BitcoinException {
        String url = "coin.rpc";
        BitcoinRPCClient rpcClient = new BitcoinRPCClient(url);

//        System.out.println(rpcClient.getBalance());

//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//        String account = "acct-change-" + df.format(new Date());
//        String changeAddress = rpcClient.getAccountAddress(account);
//        System.out.println(changeAddress);
//
//        String account1 = rpcClient.getAccount(changeAddress);
//        System.out.println(account1);
//        BigDecimal balance = new BigDecimal(rpcClient.getBalance(account));
//        System.out.println(balance.toPlainString());
//
//        String txid = BitcoinUtil.sendTransaction(rpcClient,"33tpHfgAQNDxh59ysf81UjBbnruxo77ky8",BigDecimal.valueOf(0.00001),BigDecimal.valueOf(0.0001));
//        System.out.println(txid);

//        List<Bitcoin.Unspent> unspents = rpcClient.listUnspent(2);
//        System.out.println(unspents.size());

        try {
            String sss = rpcClient.getNewAddress("sss");
            System.out.println(sss);

//            BigDecimal balance = new BigDecimal(rpcClient.getBalance());
//            balance=balance.setScale(8, RoundingMode.FLOOR);
//            System.out.println("balance="+balance.toPlainString());
//            if(balance.compareTo(BigDecimal.valueOf(0.00001).add(BigDecimal.valueOf(0.000005))) <= 0){
////                return MessageResult.error(500,"The amount must be greater than "+BigDecimal.valueOf(0.00001));
//            }
//            System.out.println(balance.subtract(BigDecimal.valueOf(0.00001).add(BigDecimal.valueOf(0.000005))));
//            String txid = BitcoinUtil.sendTransaction(rpcClient,"3FmTHJdifVsjFc13dooXtkxmX3RDm52tgD",balance.subtract(BigDecimal.valueOf(0.00001).add(BigDecimal.valueOf(0.000005))),BigDecimal.valueOf(0.00001));
//            System.out.println(txid);
//            MessageResult result = new MessageResult(0,"success");
//            result.setData(txid);
//            return result;
        } catch (Exception e) {
            e.printStackTrace();
//            return MessageResult.error(500,"error:"+e.getMessage());
        }

//        System.out.println(client.dumpPrivKey("tb1qrq6ly4w222ndfku3qph9jkasd45tkvd65704gz"));
//
//        String account = client.getAccount("tb1qrq6ly4w222ndfku3qph9jkasd45tkvd65704gz");
//        System.out.println(account);
////        System.out.println("account="+account+",address=tb1qrq6ly4w222ndfku3qph9jkasd45tkvd65704gz");
//        BigDecimal balance = new BigDecimal(client.getBalance(account));
//        System.out.println(balance.toPlainString());
//
//        System.out.println(client.getAccountAddress("adddd"));
//        System.out.println(client.getAccountAddress("add"));
//        String txid = BitcoinUtil.sendTransaction(client,"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP",BigDecimal.valueOf(0.00001),BigDecimal.valueOf(0.00001));

//        String txid = "b89bf889360b932f5ba1cfe21bf717bd82de7a55cee38d3ea6ca586816ce751a";
//
////        System.out.println( rpcClient.omniGetBalance("tb1qrq6ly4w222ndfku3qph9jkasd45tkvd65704gz").toPlainString());
//
//        Bitcoin.RawTransaction transaction =  rpcClient.getRawTransaction(txid);
//        List<Bitcoin.RawTransaction.Out> outs = transaction.vOut();
//        if(outs != null) {
//            for (Bitcoin.RawTransaction.Out out : outs) {
//                if (out.scriptPubKey() != null) {
//                    List<String> addresses = out.scriptPubKey().addresses();
//                    if(addresses != null && addresses.size() > 0) {
//                        String address = out.scriptPubKey().addresses().get(0);
//                        BigDecimal amount = new BigDecimal(out.value());
//
//                        System.out.println("Detected deposit address (" + address + "), deposit amount: " + amount + " BTC");
//
//                    }
//                }
//            }
//        }

//        String blockHash = rpcClient.getBlockHash(2222939);
//        // Get block
//        Bitcoin.Block block =  rpcClient.getBlock(blockHash);
//        List<String> txids = block.tx();
//        System.out.println("Total transactions: " + txids.size());
//        for(String txid:txids){
//            if(txid.equals("becf37aa48dc7301436aa6e47a327262090edcdd53bb9fdceaca29af9e3ca913")){
//                System.out.println("sss");
//            }
//            Bitcoin.RawTransaction transaction =  rpcClient.getRawTransaction(txid);
//            List<Bitcoin.RawTransaction.Out> outs = transaction.vOut();
//            if(outs != null) {
//                for (Bitcoin.RawTransaction.Out out : outs) {
//                    Bitcoin.RawTransaction.Out.ScriptPubKey scriptPubKey = out.scriptPubKey();
//
//                    if (scriptPubKey != null) {
//                        List<String> addresses = scriptPubKey.addresses();
//                        if(addresses != null && addresses.size() > 0) {
//                            String address = out.scriptPubKey().addresses().get(0);
//                            BigDecimal amount = new BigDecimal(out.value());
////                            if (accountService.isAddressExist(address)) {
//                            System.out.println("Detected deposit address (" + address + "), deposit amount: " + amount + " BTC");
//
////                            }
//                        }
//                    }
//                }
//            }
//        }
//        for (Long blockHeight = startBlockNumber; blockHeight <= endBlockNumber; blockHeight++) {
//            String blockHash = rpcClient.getBlockHash(blockHeight.intValue());
//            // Get block
//            Bitcoin.Block block =  rpcClient.getBlock(blockHash);
//            List<String> txids = block.tx();
//            logger.info("Get block (" + blockHeight + ") transaction list, total transactions: " + txids.size() + "");
//            // Iterate transactions in the block
//        }
    }
}
