package com.wikex.wikex.rpc.service;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Account;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.util.EthConvert;
import com.wikex.wikex.rpc.util.MessageResult;
import com.wikex.wikex.rpc.util.AESUtil;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.List;

@Component
public class EthService {
    private Logger logger = LoggerFactory.getLogger(EthService.class);
    @Autowired
    private Coin coin;
    @Autowired
    private Web3j web3j;
    @Autowired
    private PaymentHandler paymentHandler;
    @Autowired
    private AccountService accountService;
    @Autowired
    private JsonRpcHttpClient jsonrpcClient;
    @Autowired(required = false)
    private Contract contract;

    public String createNewWallet(String account, String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException, CipherException {
        logger.info("====>  Generate new wallet file for ETH.");
        String fileName = WalletUtils.generateNewWalletFile(password, new File(coin.getKeystorePath()), true);
        Credentials credentials = WalletUtils.loadCredentials(password, coin.getKeystorePath() + "/" + fileName);
        String address = credentials.getAddress();
        accountService.saveOne(account, fileName, address);
        accountService.createToken(address);
        return address;
    }

    /**
     * Synchronize balance
     *
     * @param address Wallet address
     * @throws IOException
     */
    public void syncAddressBalance(String address) throws IOException {
        BigDecimal balance = getBalance(address);
        accountService.updateBalance(address, balance);
    }

    public MessageResult transferFromWithdrawWallet(String toAddress, BigDecimal amount, boolean sync, String withdrawId, String coinName) throws Exception {
        if (StringUtils.isEmpty(coinName) || "ETH".equals(coinName)) {
            return transfer(getWalletPrivateKey(), toAddress, amount, sync, withdrawId);
        } else {
            Contract contract = ContractsConfig.getContractByCoinName(coinName);
            logger.info("Contract::address{}", contract.getAddress());
            if (contract != null) {
                return transferTokenByWallet(getWalletPrivateKey(), contract, toAddress, amount, sync);
            } else {
                return new MessageResult(500, "Coin type does not support withdrawal");
            }
        }
    }

    public MessageResult transfer(String privateKey, String toAddress, BigDecimal amount, boolean sync, String withdrawId) {
        Credentials credentials = Credentials.create(privateKey);
        if (sync) {
            return paymentHandler.transferEth(credentials, toAddress, amount);
        } else {
            paymentHandler.transferEthAsync(credentials, toAddress, amount, withdrawId);
            return new MessageResult(0, "Submitted successfully");
        }
    }

    public MessageResult transfer(String walletFile, String password, String toAddress, BigDecimal amount, boolean sync, String withdrawId) {
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(password, walletFile);
        } catch (IOException e) {
            e.printStackTrace();
            return new MessageResult(500, "Wallet file does not exist");
        } catch (CipherException e) {
            e.printStackTrace();
            return new MessageResult(500, "Decryption failed, incorrect password");
        }
        if (sync) {
            return paymentHandler.transferEth(credentials, toAddress, amount);
        } else {
            paymentHandler.transferEthAsync(credentials, toAddress, amount, withdrawId);
            return new MessageResult(0, "Submitted successfully");
        }
    }

    public BigDecimal getBalance(String address) throws IOException {
        EthGetBalance getBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        return Convert.fromWei(getBalance.getBalance().toString(), Convert.Unit.ETHER);
    }

    public BigInteger getGasPrice() throws IOException {
        EthGasPrice gasPrice = web3j.ethGasPrice().send();
        BigInteger baseGasPrice = gasPrice.getGasPrice();
        return new BigDecimal(baseGasPrice).multiply(coin.getGasSpeedUp()).toBigInteger();
    }

    public MessageResult transferFromWallet(String password, String address, BigDecimal amount, BigDecimal fee, BigDecimal minAmount) {
        logger.info("transferFromWallet method");
        List<Account> accounts = accountService.findByBalance(minAmount);
        if (accounts == null || accounts.size() == 0) {
            MessageResult messageResult = new MessageResult(500, "No accounts meet the transfer condition (greater than " + minAmount.toPlainString() + ")!");
            logger.info(messageResult.toString());
            return messageResult;
        }
        BigDecimal transferredAmount = BigDecimal.ZERO;
        for (Account account : accounts) {
            BigDecimal realAmount = account.getBalance().subtract(fee);
            if (realAmount.compareTo(amount.subtract(transferredAmount)) > 0) {
                realAmount = amount.subtract(transferredAmount);
            }
            MessageResult result = transfer(coin.getKeystorePath() + "/" + account.getWalletFile(), password, address, realAmount, true, "");
            if (result.getCode() == 0 && result.getData() != null) {
                logger.info("transfer address={},amount={},txid={}", account.getAddress(), realAmount, result.getData());
                transferredAmount = transferredAmount.add(realAmount);
                try {
                    syncAddressBalance(account.getAddress());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (transferredAmount.compareTo(amount) >= 0) {
                break;
            }
        }
        MessageResult result = new MessageResult(0, "success");
        result.setData(transferredAmount);
        return result;
    }

    public MessageResult transferTokenByWallet(String privateKey, Contract contract, String toAddress, BigDecimal amount, boolean sync) {
        Credentials credentials = Credentials.create(privateKey);
        if (sync) {
            return paymentHandler.transferToken(credentials, contract, toAddress, amount);
        } else {
            paymentHandler.transferTokenAsync(credentials, contract, toAddress, amount, "");
            return new MessageResult(0, "Submitted successfully");
        }
    }

    public MessageResult transferTokenByWalletFile(String password, Contract contract, String toAddress, BigDecimal amount, boolean sync) {
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(password, coin.getKeystorePath() + "/" + coin.getWithdrawWallet());
        } catch (IOException e) {
            e.printStackTrace();
            return new MessageResult(500, "Private key file does not exist");
        } catch (CipherException e) {
            e.printStackTrace();
            return new MessageResult(500, "Decryption failed, incorrect password");
        }
        if (sync) {
            return paymentHandler.transferToken(credentials, contract, toAddress, amount);
        } else {
            paymentHandler.transferTokenAsync(credentials, contract, toAddress, amount, "");
            return new MessageResult(0, "Submitted successfully");
        }
    }

    public MessageResult transferToken(String password, Contract contract, String fromAddress, String toAddress, BigDecimal amount, boolean sync) {
        Account account = accountService.findByAddress(fromAddress);
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(password, coin.getKeystorePath() + "/" + account.getWalletFile());
        } catch (IOException e) {
            e.printStackTrace();
            return new MessageResult(500, "Private key file does not exist");
        } catch (CipherException e) {
            e.printStackTrace();
            return new MessageResult(500, "Decryption failed, incorrect password");
        }
        if (sync) {
            return paymentHandler.transferToken(credentials, contract, toAddress, amount);
        } else {
            paymentHandler.transferTokenAsync(credentials, contract, toAddress, amount, "");
            return new MessageResult(0, "Submitted successfully");
        }
    }

    /**
     * Get the token balance of an account
     * @param account account address
     * @param coinAddress token contract address
     * @return token balance (unit: smallest token unit)
     * @throws IOException
     */
    public BigInteger getBalanceOfCoin(String account, String coinAddress) throws IOException {
        Function balanceOf = new Function("balanceOf",
                Arrays.<Type>asList(new Address(account)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));

        if (coinAddress == null) {
            return null;
        }
        String value = web3j.ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(account, coinAddress, FunctionEncoder.encode(balanceOf)), DefaultBlockParameterName.PENDING).send().getValue();
        return new BigInteger(value.substring(2), 16);
    }

    public BigDecimal getTokenBalance(String address, Contract contract) throws IOException {
        logger.info("address::{},tokenAddress::{}", address, contract.getAddress());
        BigInteger balance = getBalanceOfCoin(address, contract.getAddress());
        return EthConvert.fromWei(new BigDecimal(balance), contract.getUnit());
    }

    public BigDecimal getMinerFee(BigInteger gasLimit) throws IOException {
        BigDecimal fee = new BigDecimal(getGasPrice().multiply(gasLimit));
        return Convert.fromWei(fee, Convert.Unit.ETHER);
    }

    public Boolean isTransactionSuccess(String txid) throws IOException {
        EthTransaction transaction = web3j.ethGetTransactionByHash(txid).send();
        try {
            if (transaction != null && transaction.getTransaction().get() != null) {
                Transaction tx = transaction.getTransaction().get();
                if (!tx.getBlockHash().equalsIgnoreCase("0x0000000000000000000000000000000000000000000000000000000000000000")) {
                    EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txid).send();
                    if (receipt != null && receipt.getTransactionReceipt().get().getStatus().equalsIgnoreCase("0x1")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public MessageResult transferFeeFromWithdrawWallet(String toAddress, BigDecimal amount, boolean sync) throws Exception {
        return transfer(getWalletPrivateKey(), toAddress, amount, sync, "");
    }

    private String getWalletPrivateKey() {
        String privateKey = null;
        try {
            privateKey = AESUtil.decrypt(coin.getWithdrawWalletPrivateKey(), coin.getWithdrawWalletPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return privateKey;
    }
}
