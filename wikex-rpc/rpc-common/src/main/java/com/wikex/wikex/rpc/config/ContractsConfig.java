package com.wikex.wikex.rpc.config;

import com.wikex.wikex.rpc.entity.Contract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ContractsConfig {

    private static List<Contract> contracts = new ArrayList<>();

    public static List<Contract> getContracts(){
        return contracts;
    }

    public static void updateContract(Contract contract){
        boolean isOk = false;
        for (Contract contract1 : contracts) {
            if(contract.getName().equals(contract1.getName())){
                contract1 = contract;
                isOk = true;
            }
        }
        if(!isOk){
            contracts.add(contract);
        }
    }


    public static boolean isContractsExist(String address){
        List<String> contracts = getContracts().stream().map(c->c.getAddress()).collect(Collectors.toList());
        return contracts.contains(address);
    }

    public static Contract getContractByAddress(String address){
        Map<String,Contract> map = getContracts().stream().collect(Collectors.toMap(Contract::getAddress, Function.identity()));
        return map.get(address);
    }

    public static Contract getContractByCoinName(String coinName){
        Map<String,Contract> map = getContracts().stream().collect(Collectors.toMap(Contract::getName, Function.identity()));
        return map.get(coinName.toUpperCase());
    }

    public static void main(String[] args) {
        Contract contract = new Contract();
        contract.setAddress("0x7eced6b0fc5f2b2f404d6d2715896b96e6a14cd6");
        contract.setDecimals("ether");
        contract.setName("USDT");
        contract.setMinCollectAmount(BigDecimal.valueOf(10));
        contract.setGasLimit(BigInteger.valueOf(200000));
        contract.setEventTopic0("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");


        ContractsConfig.updateContract(contract);




        contract.setAddress("0x7eced6b0fc5f2b2f404d6d2715896b96e6a14cd7");
        contract.setDecimals("ether");
        contract.setName("USDT");
        contract.setMinCollectAmount(BigDecimal.valueOf(10));
        contract.setGasLimit(BigInteger.valueOf(200000));
        contract.setEventTopic0("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");

        // System.out.println(JSON.toJSONString(ContractsConfig.getContracts()));
    }

}
