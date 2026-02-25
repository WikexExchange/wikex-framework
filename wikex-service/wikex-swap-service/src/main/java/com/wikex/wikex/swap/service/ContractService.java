package com.wikex.wikex.swap.service;

import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractService {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory; 

    @Autowired
    private ContractCoinService contractCoinService; 

    @Autowired
    private ContractOrderEntrustService contractOrderEntrustService; 
}
