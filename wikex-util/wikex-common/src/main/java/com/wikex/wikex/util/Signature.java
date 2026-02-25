package com.wikex.wikex.util;

import com.alibaba.fastjson.JSON;

import java.util.*;

public class Signature {

    private String skey;

    private String salt;

    public Signature(String skey, String salt) {
        this.skey = skey;
        this.salt = salt;
    }

    public Map<String, String> security(String ciphertext) throws Exception {

        String decrypt = new String(AESUtil.encryptAndDecrypt(Base64Util.decodeURL(ciphertext), skey, 2), "UTF-8");

        Map<String, String> decryptTreeMap = JSON.parseObject(decrypt, TreeMap.class);

        String signature = decryptTreeMap.remove("signature");
        String localSignature = MD5.md5(JSON.toJSONString(decryptTreeMap), salt);

        return signature.equals(localSignature) ? decryptTreeMap : null;
    }

    public String security(Map<String, String> dataMap) throws Exception {

        dataMap = JSON.parseObject(JSON.toJSONString(dataMap), TreeMap.class);

        String treeJson = JSON.toJSONString(dataMap);

        String signature = MD5.md5(treeJson, salt);

        dataMap.put("signature", signature);

        return Base64Util.encodeURL(AESUtil.encryptAndDecrypt(JSON.toJSONString(dataMap).getBytes("UTF-8"), skey, 1));
    }

    public static void main(String[] args) throws Exception {
        String skey = "ab2cc473d3334c39";
        String salt = "XPYQZb1kMES8HNaJWW8+TDu/4JdBK4owsU9eXCXZDOI=";

        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("body", "Mall Orders-");
        dataMap.put("out_trade_no", "AAA");
        dataMap.put("device_info", "PC");
        dataMap.put("fee_type", "CNY");
        dataMap.put("total_fee", "1");
        dataMap.put("spbill_create_ip", "192.168.100.130");
        dataMap.put("notify_url", "http://www.example.com/wxpay/notify");
        dataMap.put("trade_type", "NATIVE");

        Signature signature = new Signature(skey, salt);

        String cSrc = signature.security(dataMap);
        // System.out.println(cSrc);
        Map<String, String> map1 = signature.security(cSrc);
        // System.out.println(map1);

    }
}
