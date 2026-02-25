package com.wikex.wikex.rpc.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EtherscanApi {
    private Logger logger = LoggerFactory.getLogger(EtherscanApi.class);
    private String token;

    private String url = "https://api-ropsten.etherscan.io/api";
//    private String url = "https://api.etherscan.io/api";

    public void sendRawTransaction(String hexValue){
        try {
            HttpResponse<String> response =  Unirest.post(url)
                    .field("module","proxy")
                    .field("action","eth_sendRawTransaction")
                    .field("hex",hexValue)
                    .field("apikey",token)
                    .asString();
            logger.info("sendRawTransaction result = {}",response.getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }


    public boolean checkEventLog(final Long blockHeight,String address,String topic0,String txid){
        try {
            HttpResponse<String> response = Unirest.post(url)
                    .field("module", "logs")
                    .field("action", "getLogs")
                    .field("fromBlock", blockHeight)
                    .field("toBlock",blockHeight)
                    .field("address",address)
                    .field("topic0",topic0)
                    .field("apikey", token)
                    .asString();
            logger.info("getLogs result = {}",response.getBody());
            JSONObject result = JSON.parseObject(response.getBody());
            if(result.getInteger("status")==0){
                return false;
            }
            else{
                JSONArray txs = result.getJSONArray("result");
                for(int i=0;i<txs.size();i++){
                    JSONObject item = txs.getJSONObject(i);
                    if(item.getString("transactionHash").equalsIgnoreCase(txid))return true;
                }
                return false;
            }

        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    public static void main(String[] args){
        EtherscanApi api = new EtherscanApi();
        //api.sendRawTransaction("0xf86e0585012a05f200830f4240950db4a46649c041b506e5d4965b8ed4f682f75b18ff8801c6fc1379856000801ca08e5e25623e588079f4fd795b48f34f128a07b63dc7385ca7d533671014417a11a00d093b1512b40265daf5db6bf3762188490a8a8d812a4756b599378e0d42855e");
        String txid = "0x7c2e14865023012bf2bb88b446e0b736228212e0d33d97f2184f1d9aea4b1c18";
        boolean ret = api.checkEventLog(12070815L,"0x7eced6b0fc5f2b2f404d6d2715896b96e6a14cd6","0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",txid);
        System.out.println(ret);
    }
}
