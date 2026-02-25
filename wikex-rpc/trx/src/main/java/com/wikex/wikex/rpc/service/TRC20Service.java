package com.wikex.wikex.rpc.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Account;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.component.HttpUtils;
import com.wikex.wikex.rpc.component.TransformUtil;
import com.wikex.wikex.rpc.util.AESUtil;
import com.wikex.wikex.rpc.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.tron.utils.TronUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: little liu
 * @Date: 2020/09/03 16:06
 * @Description: TRC20 related operations and TRX/Token transfers
 */
@Service
public class TRC20Service {
    private Logger logger = LoggerFactory.getLogger(TRC20Service.class);
    @Autowired
    private Coin coin;
    @Autowired
    private AccountService accountService;
    @Value("${trx.api.key}")
    private String apiKey;
    @Autowired
    private PaymentHandler paymentHandler;

    private static Long blockDeep;

    private static Long fee;

    private static Map<String, String> symbolMap;

    private static Map<String, String> contractMap;

    private static Map<String, Integer> weiMap;

    private BigInteger currentBlock = BigInteger.ZERO;

    /**
     * Create a user wallet address
     */
    public static Map<String,String> createAddress() {
        // String url = http + "/wallet/generateaddress";
        Map<String, String> map = TronUtils.createAddress();
        return map;
    }

    /**
     * Activate address
     *
     * @param address Address to activate
     * @return transaction id or null
     */
    public String createAccount(String address) throws Exception {
        String url = coin.getRpc() + "/wallet/createaccount";
        Map<String, Object> map = new HashMap<>();
        map.put("owner_address", TronUtils.toHexAddress(coin.getWithdrawWallet()));
        map.put("account_address", TronUtils.toHexAddress(address));
        String param = JSON.toJSONString(map);

        String _result = HttpUtils.postForEntity(url, param, apiKey).getBody();
        JSONObject transaction = JSONObject.parseObject(_result);
        String result = null;
        try {
            result = TronUtils.signAndBroadcast(coin.getRpc(), getWalletPrivateKey(), transaction);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;
    }

    /**
     * Get TRX account info by address
     *
     * @param address Address
     * @return JSON string
     */
    public String getAccount(String address) {
        String url = coin.getRpc() + "/wallet/getaccount";
        Map<String, Object> map = new HashMap<>();
        map.put("address", TronUtils.toHexAddress(address));
        String param = JSON.toJSONString(map);
        return HttpUtils.postForEntity(url, param, apiKey).getBody();
    }

    public BigDecimal getTRXBalance(String address){
        String account = getAccount(address);
        String accountBalance = JSON.parseObject(account).getString("balance");
        if(accountBalance==null){
            accountBalance = "0";
        }
        return TransformUtil.formatTokenNum(new BigInteger(accountBalance),6);
    }

    /**
     * Get TRC20 token balance for an address
     *
     * @param address Address
     * @return JSON string
     */
    public String getTrc20Account(String contractAddress, String address) {
        String url = coin.getRpc() + "/wallet/triggerconstantcontract";
        Map<String, Object> map = new HashMap<>();
        address = TransformUtil.addZeroForNum(TronUtils.toHexAddress(address), 64);
        map.put("contract_address", TronUtils.toHexAddress(contractAddress));
        map.put("function_selector", "balanceOf(address)");
        map.put("parameter", address);
        map.put("owner_address", TronUtils.toHexAddress(coin.getWithdrawWallet()));
        String param = JSON.toJSONString(map);
        return HttpUtils.postForEntity(url, param, apiKey).getBody();
    }

    public BigDecimal getTokenBalance(String address, Contract contract){
        String json = getTrc20Account(contract.getAddress(),address);
        JSONObject result = JSON.parseObject(json);
        if(result!=null && result.keySet().size()>0) {
            JSONArray constant_result = result.getJSONArray("constant_result");
            if(constant_result!=null && constant_result.getString(0)!=null){
                return TransformUtil.formatTokenNum(new BigInteger(result.getJSONArray("constant_result").getString(0),16),contract.getUnit().getDecimals());
            }else {
                return BigDecimal.ZERO;
            }
        }else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * TRC20 transfer (from withdraw wallet)
     *
     * @param symbol  Token symbol
     * @param toAddress Recipient address
     * @param amount    Amount
     * @return result body
     */
    public String trc20Transaction(String symbol, String toAddress, BigDecimal amount) throws Exception {
        // initiate transaction
        String url = coin.getRpc() + "/wallet/triggersmartcontract";

        Map<String, Object> map = new HashMap<>();

        String to_address = TronUtils.toHexAddress(toAddress);
        to_address = TransformUtil.addZeroForNum(to_address, 64);
        amount = amount.multiply(new BigDecimal(1 + TransformUtil.getSeqNumByLong(0L, weiMap.get(symbol))));
        String uint256 = TransformUtil.addZeroForNum(amount.toBigInteger().toString(16), 64);

        map.put("owner_address", TronUtils.toHexAddress(coin.getWithdrawWallet()));
        map.put("contract_address", TronUtils.toHexAddress(symbolMap.get(symbol)));
        map.put("function_selector", "transfer(address,uint256)");
        map.put("parameter", to_address + uint256);
        map.put("call_value", 0);
        map.put("fee_limit", fee);

        String param = JSON.toJSONString(map);

        ResponseEntity<String> stringResponseEntity = HttpUtils.postForEntity(url, param, apiKey);

        return signAndBroadcast(JSON.parseObject(stringResponseEntity.getBody()).getString("transaction"), coin.getWithdrawWalletPassword());
    }

    /**
     * TRC20 collection (sweep) specific API
     *
     * @param symbol      Token symbol
     * @param fromAddress Source address
     * @param privateKey  Private key
     * @param toAddress   Destination address
     * @param amount      Amount
     * @return result body
     */
    private String trc20Transaction(String symbol, String fromAddress, String privateKey, String toAddress, BigDecimal amount) {
        // initiate transaction
        String url = coin.getRpc() + "/wallet/triggersmartcontract";

        Map<String, Object> map = new HashMap<>();

        String to_address = TronUtils.toHexAddress(toAddress);
        to_address = TransformUtil.addZeroForNum(to_address, 64);
        amount = amount.multiply(new BigDecimal(1 + TransformUtil.getSeqNumByLong(0L, weiMap.get(symbol))));
        String uint256 = TransformUtil.addZeroForNum(amount.toBigInteger().toString(16), 64);

        map.put("owner_address", TronUtils.toHexAddress(fromAddress));
        map.put("contract_address", TronUtils.toHexAddress(symbolMap.get(symbol)));
        map.put("function_selector", "transfer(address,uint256)");
        map.put("parameter", to_address + uint256);
        map.put("call_value", 0);
        map.put("fee_limit", fee);

        String param = JSON.toJSONString(map);

        ResponseEntity<String> stringResponseEntity = HttpUtils.postForEntity(url, param, apiKey);

        // sign
        url = coin.getRpc() + "/wallet/gettransactionsign";
        map = new HashMap<>();
        map.put("transaction", JSON.parseObject(stringResponseEntity.getBody()).get("transaction"));
        map.put("privateKey", privateKey);
        param = JSON.toJSONString(map);
        stringResponseEntity = HttpUtils.postForEntity(url, param, apiKey);

        // broadcast
        url = coin.getRpc() + "/wallet/broadcasttransaction";
        stringResponseEntity = HttpUtils.postForEntity(url, stringResponseEntity.getBody(), apiKey);

        return stringResponseEntity.getBody();
    }

    /**
     * Sign and broadcast
     *
     * @param transaction Transaction object
     * @return result body
     */
    private String signAndBroadcast(String transaction, String privateKey) {

        // sign
        String url = coin.getRpc() + "/wallet/gettransactionsign";
        Map<String, Object> map = new HashMap<>();
        map.put("transaction", transaction);
        map.put("privateKey", privateKey);
        String param = JSON.toJSONString(map);
        ResponseEntity<String> stringResponseEntity = HttpUtils.postForEntity(url, param, apiKey);

        // broadcast
        url = coin.getRpc() + "/wallet/broadcasttransaction";
        stringResponseEntity = HttpUtils.postForEntity(url, stringResponseEntity.getBody(), apiKey);

        return stringResponseEntity.getBody();
    }

    /**
     * TRX transfer (easy transfer by private key)
     *
     * @param toAddress Recipient address
     * @param amount    Amount
     */
    public String trxTransaction(String toAddress, BigDecimal amount) throws Exception {
        String url = coin.getRpc() + "/wallet/easytransferbyprivate";
        Map<String, Object> map = new HashMap<>();
        map.put("privateKey", coin.getWithdrawWalletPassword());
        map.put("toAddress", TronUtils.toHexAddress(toAddress));
        amount = amount.multiply(new BigDecimal(1 + TransformUtil.getSeqNumByLong(0L, weiMap.get("TRX"))));
        map.put("amount", amount.toBigInteger());
        String param = JSON.toJSONString(map);
        return HttpUtils.postForEntity(url, param, apiKey).getBody();
    }

    /**
     * Create a TRX transaction object
     *
     * @param toAddress Recipient address
     * @param amount    Amount
     * @return tx result body
     */
    public String transaction(String toAddress, BigDecimal amount) throws Exception {
        String url = coin.getRpc() + "/wallet/createtransaction";
        Map<String, Object> map = new HashMap<>();
        map.put("owner_address", TronUtils.toHexAddress(coin.getWithdrawWallet()));
        map.put("to_address", TronUtils.toHexAddress(toAddress));
        amount = amount.multiply(new BigDecimal(1 + TransformUtil.getSeqNumByLong(0L, weiMap.get("TRX"))));
        map.put("amount", amount.toBigInteger());
        String param = JSON.toJSONString(map);
        return signAndBroadcast(HttpUtils.postForEntity(url, param, apiKey).getBody(), coin.getWithdrawWalletPassword());
    }

    /**
     * https://cn.developers.tron.network/docs/%E4%BA%A4%E6%98%9311#%E4%BA%A4%E6%98%93%E7%A1%AE%E8%AE%A4%E6%96%B9%E6%B3%95
     * Query transaction by hash
     *
     * @param txId Transaction id
     * @return JSON string
     */
    public String getTransactionById(String txId) {
        String url = coin.getRpc() + "/wallet/gettransactionbyid";
        Map<String, Object> map = new HashMap<>();
        map.put("value", txId);
        String param = JSON.toJSONString(map);
        return HttpUtils.postForEntity(url, param, apiKey).getBody();
    }

    /**
     * Query transaction info, including fee, block, VM logs, etc.
     *
     * @param txId Transaction id
     * @return JSON string
     */
    public String getTransactionInfoById(String txId) {
        String url = coin.getRpc() + "/wallet/gettransactioninfobyid";
        Map<String, Object> map = new HashMap<>();
        map.put("value", txId);
        String param = JSON.toJSONString(map);
        return HttpUtils.postForEntity(url, param, apiKey).getBody();
    }

    /**
     * Get all transaction info in a specific block
     *
     * @param num Block number
     * @return JSON string
     */
    public String getTransactionInfoByBlockNum(BigInteger num) {
        String url = coin.getRpc() + "/wallet/gettransactioninfobyblocknum";
        Map<String, Object> map = new HashMap<>();
        map.put("num", num);
        String param = JSON.toJSONString(map);
        return HttpUtils.postForEntity(url, param, apiKey).getBody();
    }

    /**
     * Get the latest block
     *
     * @return block number
     */
    public BigInteger getNowBlock() {
        String url = coin.getRpc() + "/wallet/getnowblock";
        String result = HttpUtils.getForEntity(url);
        JSONObject jsonObject = JSON.parseObject(result);
        BigInteger num = jsonObject.getJSONObject("block_header").getJSONObject("raw_data").getBigInteger("number");
        return num;
    }

    /**
     * Get the block content by number
     *
     * @param num Block number
     * @return JSON string
     */
    public String getBlockByNum(BigInteger num) {
        String url = coin.getRpc() + "/wallet/getblockbynum";
        Map<String, Object> map = new HashMap<>();
        map.put("num", num);
        String param = JSON.toJSONString(map);
        return HttpUtils.postForEntity(url, param, apiKey).getBody();
    }

    public void collectionTrc20Listener() {
        try {
            // Get addresses that need collection
            Map<String, String> addressMap = new HashMap<>();
            addressMap.put("xxx", "xxxx");
            // Collection destination address
            String toAddress = "xxx";
            String fromAddress = null;
            String privateKey = null;

            for (String symbol : symbolMap.keySet()) {
                for (String key : addressMap.keySet()) {
                    fromAddress = key;
                    privateKey = addressMap.get(key);
                    String trc20Account = getTrc20Account(symbol, fromAddress);
                    JSONObject jsonObject = JSON.parseObject(trc20Account);
                    String constant_result = jsonObject.getString("constant_result");

                    if (StringUtils.isEmpty(constant_result)) {
                        continue;
                    }

                    List<String> strings = JSON.parseArray(constant_result.toString(), String.class);

                    String data = strings.get(0).replaceAll("^(0+)", "");
                    if (data.length() == 0) {
                        continue;
                    }

                    String amountStr = new BigInteger(data, 16).toString();
                    BigDecimal amount = new BigDecimal(amountStr).divide(new BigDecimal(1 + TransformUtil.getSeqNumByLong(0L, weiMap.get(symbol))));

                    if (amount.compareTo(BigDecimal.ONE) < 0) {
                        continue;
                    }

                    String account = getAccount(fromAddress);
                    String accountBalance = JSON.parseObject(account).getString("balance");
                    BigDecimal balance = BigDecimal.ZERO;

                    if (StringUtils.isNotEmpty(accountBalance)) {
                        balance = new BigDecimal(accountBalance).divide(new BigDecimal(1 + TransformUtil.getSeqNumByLong(0L, weiMap.get("TRX"))));
                    }

                    if (balance.compareTo(new BigDecimal("0.5")) < 0) {
                        // Top up miner fee
                        String transaction = transaction(fromAddress, new BigDecimal("0.5"));
                        continue;
                    }

                    // Collect (sweep) tokens
                    String transaction = trc20Transaction(symbol, fromAddress, privateKey, toAddress, amount);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MessageResult transferToken(Contract contract,String fromAddress, String toAddress, BigDecimal amount, boolean sync) {
        Account account = accountService.findByAddress(fromAddress);
        if(account==null || StringUtils.isEmpty(account.getPrivateKey())){
            return new MessageResult(500, "Private key file does not exist");
        }
        if(sync) {
            return paymentHandler.transferToken(contract,getPrivateKey(account), toAddress, amount);
        }
        else{
            paymentHandler.transferTokenAsync(contract,getPrivateKey(account), toAddress, amount,"");
            return new MessageResult(0,"Submitted successfully");
        }
    }

    public MessageResult transferTRX(String fromAddress, String toAddress, BigDecimal amount, boolean sync) {
        Account account = accountService.findByAddress(fromAddress);
        if(account==null || StringUtils.isEmpty(account.getPrivateKey())){
            return new MessageResult(500, "Private key file does not exist");
        }
        if(sync) {
            return paymentHandler.transferTRX(getPrivateKey(account), toAddress, amount);
        }
        else{
            paymentHandler.transferTRXAsync(getPrivateKey(account), toAddress, amount,"");
            return new MessageResult(0,"Submitted successfully");
        }
    }

    private String getPrivateKey(Account account) {
        logger.info("privateKey::{},password:{}",account.getPrivateKey(),coin.getWithdrawWalletPassword());
        String privateKey = null;
        try {
            privateKey = AESUtil.decrypt(account.getPrivateKey(),coin.getWithdrawWalletPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return privateKey;
    }
    private String getWalletPrivateKey() {
        String privateKey = null;
        try {
            privateKey = AESUtil.decrypt(coin.getWithdrawWalletPrivateKey(),coin.getWithdrawWalletPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    public static void main(String[] args) throws Throwable {

        String privateKey = "dc0d7199f14a9be22e41748b3236737d149cb41c2dff5bba1a695ca9863bef97";

        // BigDecimal amount=BigDecimal.valueOf(10);
        // String ownerAddress = TronUtils.getAddressByPrivateKey(privateKey);
        // JSONObject jsonObject = new JSONObject();
        // jsonObject.put("contract_address", TronUtils.toHexAddress("TCcbSHjG2W1F7NBN4vttGtW5jDpZvDDgaC"));
        // jsonObject.put("function_selector", "transfer(address,uint256)");
        // List<Type> inputParameters = new ArrayList<>();
        // inputParameters.add(new Address(TronUtils.toHexAddress("TK4VF8W8yZiTnuucspNsX2ftqZRePKaRQZ").substring(2)));
        // inputParameters.add(new Uint256(amount.multiply(BigDecimal.TEN.pow(6)).toBigInteger()));
        // String parameter = FunctionEncoder.encodeConstructor(inputParameters);
        // jsonObject.put("parameter", parameter);
        // jsonObject.put("owner_address", TronUtils.toHexAddress(ownerAddress));
        // jsonObject.put("call_value", 0);
        // jsonObject.put("fee_limit", 6000000L);
        // String trans1 = HttpUtils.postForEntity( "https://api.trongrid.io/wallet/triggersmartcontract", jsonObject.toJSONString(), "apiKey").getBody();
        // JSONObject result = JSONObject.parseObject(trans1);
        // if (result.containsKey("Error")) {
        //     System.out.println("send error==========");
        // }
        // JSONObject tx = result.getJSONObject("transaction");
        // System.out.println(tx.toJSONString());
        // // tx.getJSONObject("raw_data").put("data", org.spongycastle.util.encoders.Hex.toHexString("I'm Tricky".getBytes())); // fill in remark
        // String txid = TronUtils.signAndBroadcast("https://api.trongrid.io", privateKey, tx);
        // if (txid != null) {
        //     System.out.println("Transaction Id:" + txid);
        // }

        BigDecimal amount = BigDecimal.valueOf(13);
        String url = "https://api.trongrid.io/wallet/createtransaction";
        JSONObject param = new JSONObject();
        param.put("owner_address",TronUtils.toHexAddress(TronUtils.getAddressByPrivateKey(privateKey)));
        param.put("to_address",TronUtils.toHexAddress("TSiZqeLiDvo3BpN3U9i1rodsptY76JTi47"));
        // param.put("to_address",TronUtils.toHexAddress("TBb8GEJ7frvswgeoxnss5E4ozNsQk4tscM"));
        param.put("amount",amount.multiply(BigDecimal.TEN.pow(6)).toBigInteger());
        String _result = HttpUtils.postForEntity(url, param.toJSONString(),"apiKey").getBody();
        String txid = null; // transaction id
        if(StringUtils.isNotEmpty(_result)){
            JSONObject transaction = JSONObject.parseObject(_result);
            System.out.println(transaction.toJSONString());
            // transaction.getJSONObject("raw_data").put("data", org.spongycastle.util.encoders.Hex.toHexString("Remark information here".getBytes()));
            txid = TronUtils.signAndBroadcast("https://api.trongrid.io", privateKey, transaction);
        }
    }

    /**
     * TRC20 transfer — offline signing
     * @param contract   Contract metadata
     * @param privateKey Private key
     * @param toAddress  Recipient
     * @param amount     Amount
     * @throws Throwable
     */
    public String sendTrc20Token(Contract contract,String privateKey,String toAddress,BigDecimal amount) throws Throwable {
        String ownerAddress = TronUtils.getAddressByPrivateKey(privateKey);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("contract_address", TronUtils.toHexAddress(contract.getAddress()));
        jsonObject.put("function_selector", "transfer(address,uint256)");
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Address(TronUtils.toHexAddress(toAddress).substring(2)));
        inputParameters.add(new Uint256(amount.multiply(BigDecimal.TEN.pow(contract.getUnit().getDecimals())).toBigInteger()));
        String parameter = FunctionEncoder.encodeConstructor(inputParameters);
        jsonObject.put("parameter", parameter);
        jsonObject.put("owner_address", TronUtils.toHexAddress(ownerAddress));
        jsonObject.put("call_value", 0);
        jsonObject.put("fee_limit", 30000000L);
        String trans1 = HttpUtils.postForEntity(coin.getRpc() + "/wallet/triggersmartcontract", jsonObject.toJSONString(), apiKey).getBody();
        JSONObject result = JSONObject.parseObject(trans1);
        if (result.containsKey("Error")) {
            System.out.println("send error==========");
            return null;
        }
        JSONObject tx = result.getJSONObject("transaction");
        // tx.getJSONObject("raw_data").put("data", org.spongycastle.util.encoders.Hex.toHexString("I'm Tricky".getBytes())); // fill in remark
        String txid = TronUtils.signAndBroadcast(coin.getRpc(), privateKey, tx);
        if (txid != null) {
            System.out.println("Transaction Id:" + txid);
        }
        return txid;
    }

    /**
     * TRX transfer — offline signing
     * @param privateKey Private key
     * @param toAddress  Recipient
     * @param amount     Amount
     * @throws Throwable
     */
    public String sendTrx(String privateKey,String toAddress,BigDecimal amount) throws Throwable {
        String url = coin.getRpc() + "/wallet/createtransaction";
        JSONObject param = new JSONObject();
        param.put("owner_address",TronUtils.toHexAddress(TronUtils.getAddressByPrivateKey(privateKey)));
        param.put("to_address",TronUtils.toHexAddress(toAddress));
        param.put("amount",amount.multiply(BigDecimal.TEN.pow(6)).toBigInteger());
        String _result = HttpUtils.postForEntity(url, param.toJSONString(), apiKey).getBody();
        String txid = null; // transaction id
        if(StringUtils.isNotEmpty(_result)){
            JSONObject transaction = JSONObject.parseObject(_result);
            // transaction.getJSONObject("raw_data").put("data", org.spongycastle.util.encoders.Hex.toHexString("Remark information here".getBytes()));
            txid = TronUtils.signAndBroadcast(coin.getRpc(), privateKey, transaction);
        }
        return txid;
    }

    public boolean isTransactionSuccess(String txid) {
        String transaction = getTransactionById(txid);
        if(transaction!=null){
            JSONObject result = JSONObject.parseObject(transaction);
            String res = result.getJSONArray("ret").getJSONObject(0).getString("contractRet");
            return "SUCCESS".equalsIgnoreCase(res);
        }
        return false;
    }

    public MessageResult transferTokenFromWithdrawWallet(Contract contract,String toAddress, BigDecimal amount, boolean sync,String withdrawId){
        if(StringUtils.isEmpty(coin.getWithdrawWalletPrivateKey())){
            return new MessageResult(500, "Private key file does not exist");
        }
        if(sync) {
            return paymentHandler.transferToken(contract,getWalletPrivateKey(), toAddress, amount);
        }
        else{
            paymentHandler.transferTokenAsync(contract,getWalletPrivateKey(), toAddress, amount, withdrawId);
            return new MessageResult(0,"Submitted successfully");
        }
    }

    public MessageResult transferTRXFromWithdrawWallet(String toAddress, BigDecimal amount, boolean sync) throws Exception {
        if(StringUtils.isEmpty(coin.getWithdrawWalletPrivateKey())){
            return new MessageResult(500, "Private key file does not exist");
        }
        if(sync) {
            return paymentHandler.transferTRX(getWalletPrivateKey(), toAddress, amount);
        }
        else{
            paymentHandler.transferTRXAsync(getWalletPrivateKey(), toAddress, amount, "");
            return new MessageResult(0,"Submitted successfully");
        }
    }

    public MessageResult transferFromWithdrawWallet(String toAddress, BigDecimal amount, Boolean sync, String withdrawId, String coinName) {
        if(StringUtils.isEmpty(coin.getWithdrawWalletPrivateKey())){
            return new MessageResult(500, "Private key file does not exist");
        }

        if(StringUtils.isEmpty(coinName) || "TRX".equals(coinName)){
            if(sync) {
                return paymentHandler.transferTRX(getWalletPrivateKey(), toAddress, amount);
            }
            else{
                paymentHandler.transferTRXAsync(getWalletPrivateKey(), toAddress, amount, withdrawId);
                return new MessageResult(0,"Submitted successfully");
            }
        }else {
            Contract contract = ContractsConfig.getContractByCoinName(coinName);
            if(contract!=null){
                return transferTokenFromWithdrawWallet(contract,toAddress,amount,sync,withdrawId);
            }else {
                return new MessageResult(500,"This asset does not support withdrawals");
            }
        }
    }
}
