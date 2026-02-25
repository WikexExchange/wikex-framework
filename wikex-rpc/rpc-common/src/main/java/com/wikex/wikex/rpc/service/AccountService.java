package com.wikex.wikex.rpc.service;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Account;
import com.wikex.wikex.rpc.entity.BalanceSum;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class AccountService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Coin coin;

    /**
     * Get the collection name
     * @return
     */
    public String getCollectionName(){
        return coin.getUnit() + "_address_book";
    }

    /**
     * Get the collection name with coin name
     * @return
     */
    public String getCollectionNameAndCoin(String coinName){
        return coin.getUnit() + "_" + coinName + "_address_book";
    }

    public String getCollectionName(String coinUnit){
        return coinUnit + "_address_book";
    }

    public void save(Account account){
        mongoTemplate.insert(account, getCollectionName());
    }

    /**
     * Find by account name
     * @param coinUnit
     * @param username
     * @return
     */
    public Account findByName(String coinUnit, String username){
        Query query = new Query();
        Criteria criteria = Criteria.where("account").is(username);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, Account.class, getCollectionName(coinUnit));
    }

    public Account findByName(String username){
        return findByName(coin.getUnit(), username);
    }

    /**
     * Find by address
     * @param address
     * @return
     */
    public Account findByAddress(String address){
        Query query = new Query();
        Criteria criteria = Criteria.where("address").is(address);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, Account.class, getCollectionName());
    }

    /**
     * Find by address and coin name
     * @param address
     * @return
     */
    public Account findByAddressAndCoin(String address, String coinName){
        Query query = new Query();
        Criteria criteria = Criteria.where("address").is(address);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, Account.class, getCollectionNameAndCoin(coinName));
    }

    public void removeByName(String name){
        Query query = new Query();
        Criteria criteria = Criteria.where("account").is(name);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, getCollectionName());
    }

    public boolean isAddressExist(String address){
        Query query = new Query();
        Criteria criteria = Criteria.where("address").is(address);
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, getCollectionName());
    }

    /**
     * Save the account and delete the old account
     * @param username
     * @param fileName
     * @param address
     */
    public void saveOne(String username, String fileName, String address) {
        removeByName(username);
        Account account = new Account();
        account.setAccount(username);
        account.setAddress(address);
        account.setWalletFile(fileName);
        save(account);
    }

    public void saveOne(String username, String address) {
        removeByName(username);
        Account account = new Account();
        account.setAccount(username);
        account.setAddress(address);
        save(account);
    }

    public void saveOne(String username, String fileName, String address, String privateKey) {
        removeByName(username);
        Account account = new Account();
        account.setAccount(username);
        account.setAddress(address);
        account.setWalletFile(fileName);
        account.setPrivateKey(privateKey);
        save(account);
    }

    /**
     * Get all accounts
     * @return
     */
    public List<Account> findAll() {
        return mongoTemplate.findAll(Account.class, getCollectionName());
    }

    /**
     * Get the number of accounts
     * @return
     */
    public long count(){
        Query query = new Query();
        Sort sort = Sort.by(Sort.Direction.ASC, "_id");
        query.with(sort);
        return mongoTemplate.count(query, getCollectionName());
    }

    /**
     * Get accounts by page
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<Account> find(int pageNo, int pageSize){
        Sort sort = Sort.by(Sort.Direction.ASC, "_id");
        PageRequest page = PageRequest.of(pageNo, pageSize, sort);
        Query query = new Query();
        query.with(page);
        return mongoTemplate.find(query, Account.class, getCollectionName());
    }

    /**
     * Find by balance
     * @param minAmount
     * @return
     */
    public List<Account> findByBalance(BigDecimal minAmount) {
        Query query = new Query();
        Criteria criteria = Criteria.where("balance").gte(minAmount);
        query.addCriteria(criteria);
        Sort sort = Sort.by(Sort.Direction.DESC, "balance");
        query.with(sort);
        return mongoTemplate.find(query, Account.class, getCollectionName());
    }

    /**
     * Find by balance and gas fee
     * @param minAmount
     * @param gasLimit
     * @return
     */
    public List<Account> findByBalanceAndGas(BigDecimal minAmount, BigDecimal gasLimit, String coinName) {
        Query query = new Query();
        Criteria criteria = Criteria.where("balance").gte(minAmount);
        criteria.andOperator(Criteria.where("gas").gte(gasLimit));
        query.addCriteria(criteria);
        Sort sort = Sort.by(Sort.Direction.DESC, "balance");
        query.with(sort);
        if (coinName == null || coinName.equals(coin.getUnit())) {
            return mongoTemplate.find(query, Account.class, getCollectionName());
        } else {
            return mongoTemplate.find(query, Account.class, getCollectionNameAndCoin(coinName));
        }
    }

    /**
     * Query total wallet balance
     * @return
     */
    public BigDecimal findBalanceSum(String coinName) {
        Aggregation aggregation = Aggregation
                .newAggregation(Aggregation.group("max").sum("balance").as("totalBalance"))
                .withOptions(Aggregation.newAggregationOptions().cursor(new Document()).build());
        AggregationResults<BalanceSum> results = null;
        if (coinName == null && coinName.equals(coin.getUnit())) {
            results = mongoTemplate.aggregate(aggregation, getCollectionName(), BalanceSum.class);
        } else {
            results = mongoTemplate.aggregate(aggregation, getCollectionNameAndCoin(coinName), BalanceSum.class);
        }
        List<BalanceSum> list = results.getMappedResults();
        return list.get(0).getTotalBalance().setScale(8, BigDecimal.ROUND_DOWN);
    }

    /**
     * Update balance
     * @param address
     * @param balance
     */
    public void updateBalance(String address, BigDecimal balance) {
        Query query = new Query();
        Criteria criteria = Criteria.where("address").is(address);
        query.addCriteria(criteria);
        UpdateResult result = mongoTemplate.updateFirst(query, Update.update("balance", balance.setScale(8, BigDecimal.ROUND_DOWN)), getCollectionName());
    }

    public void updateBalanceAndGas(String address, BigDecimal balance, BigDecimal gas) {
        Query query = new Query();
        Criteria criteria = Criteria.where("address").is(address);
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("balance", balance.setScale(8, BigDecimal.ROUND_DOWN));
        update.set("gas", gas);
        UpdateResult result = mongoTemplate.updateFirst(query, update, getCollectionName());
    }

    public void updateBalanceByCoinName(String address, BigDecimal balance, BigDecimal gas, String coinName) {
        Account account = findByAddressAndCoin(address, coinName);
        if (account == null) {
            account = findByAddress(address);
            account.setBalance(balance.setScale(8, BigDecimal.ROUND_DOWN));
            account.setGas(gas);
            
            mongoTemplate.save(account, getCollectionNameAndCoin(coinName));
        } else {
            Query query = new Query();
            Criteria criteria = Criteria.where("address").is(address);
            query.addCriteria(criteria);
            Update update = new Update();
            update.set("balance", balance.setScale(8, BigDecimal.ROUND_DOWN));
            update.set("gas", gas);
            mongoTemplate.updateFirst(query, update, getCollectionNameAndCoin(coinName));
        }
    }

    public void createToken(String address) {
        List<Contract> contracts = ContractsConfig.getContracts();
        for (Contract coin : contracts) {
            this.updateBalanceByCoinName(address, BigDecimal.ZERO, BigDecimal.ZERO, coin.getName());
        }
    }
}
