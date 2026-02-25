package com.wikex.wikex.coinswap.service;

import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractService {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory; 

    @Autowired
    private ContractCoinCoinService contractCoinService;

    @Autowired
    private ContractOrderEntrustCoinService contractOrderEntrustService;
}
