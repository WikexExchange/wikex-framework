package com.wikex.wikex.rpc.util;

import com.spark.blockchain.rpcclient.BitcoinException;
import com.spark.blockchain.rpcclient.BitcoinRPCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shenzucai
 * @time 2018.04.19 22:43
 */
public class WalletOperationUtil {

    private static Logger logger = LoggerFactory.getLogger(WalletOperationUtil.class);

    // Prerequisite: The wallet has been unlocked and restarted.
    // Wallet decryption: walletpassphrase <passphrase> <timeout> [mintonly]
    public static void walletpassphrase(BitcoinRPCClient rpcClient, String passphrase) throws BitcoinException {
        rpcClient.query("walletpassphrase", passphrase, 60);
    }

    // Wallet lock: walletlock
    public static void walletlock(BitcoinRPCClient rpcClient) throws BitcoinException {
        rpcClient.query("walletlock");
    }
}
