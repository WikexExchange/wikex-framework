package com.wikex.wikex.match.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.screen.ExchangeCoinScreen;

import java.util.List;

/**
 * <p>
 * Coin-to-Coin Trading Pairs Service
 * </p>
 *
 * @author markchao
 * @since 2022-02-07
 */
public interface ExchangeCoinService extends IService<ExchangeCoin> {

    public List<ExchangeCoin> findAllEnabled();

    public List<ExchangeCoin> findAllVisible();

    public List<ExchangeCoin> findAllByRobotType(int robotType);

    public List<ExchangeCoin> findAllByFlag(int flag);

    public ExchangeCoin findOne(String id);

    public void deletes(String[] ids);


    public IPage<ExchangeCoin> pageQuery(int pageNo, Integer pageSize);

    public ExchangeCoin findBySymbol(String symbol) ;

    public List<ExchangeCoin> findAll() ;

    public boolean isSupported(String symbol) ;

    public List<String> getBaseSymbol() ;

    public List<String> getCoinSymbol(String baseSymbol) ;

    public List<String> getAllCoin();

    Page<ExchangeCoin> findAll(ExchangeCoinScreen screen);
}
