package com.wikex.wikex.rpc.event;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.entity.WatcherSetting;
import com.wikex.wikex.user.dto.ContractDTO;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.rpc.component.Watcher;
import com.wikex.wikex.rpc.entity.WatcherLog;
import com.wikex.wikex.rpc.service.WatcherLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AfterServiceStarted implements ApplicationRunner {

    @Autowired
    private CoinFeign coinFeign;
    @Autowired
    private Coin coin;
    @Autowired
    private DepositEvent depositEvent;
    @Autowired(required = false)
    private Watcher watcher;
    @Autowired
    private WatcherLogService watcherLogService;
    @Autowired
    private WatcherSetting watcherSetting;

    /**
     * Will be executed immediately after the service starts
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Initialize parameters coin/getContractByProtocol?protocol=ETH
        initContracts();
        if (watcher != null) {
            
            WatcherLog watcherLog = watcherLogService.findOne(coin.getName());
            
            if (watcherLog != null) {
                watcher.setCurrentBlockHeight(watcherLog.getLastSyncHeight());
            } else if (watcherSetting.getInitBlockHeight().equalsIgnoreCase("latest")) {
                watcher.setCurrentBlockHeight(watcher.getNetworkBlockHeight());
            } else {
                Long height = Long.parseLong(watcherSetting.getInitBlockHeight());
                watcher.setCurrentBlockHeight(height);
            }
            // Initialize parameters
            // Set the number of blocks synchronized each time
            watcher.setStep(watcherSetting.getStep());
            // Set the task execution interval
            watcher.setCheckInterval(watcherSetting.getInterval());
            watcher.setDepositEvent(depositEvent);
            // Set coin configuration information
            watcher.setCoin(coin);
            watcher.setWatcherLogService(watcherLogService);
            // Set the number of confirmations required for a transaction
            watcher.setConfirmation(watcherSetting.getConfirmation());
            new Thread(watcher).start();
        }
    }

    private void initContracts() {
        // Remote RPC service URL, suffix is the coin unit
        // #wei:0, kwei:3, wwei:4, mwei:6, gwei:9, szabo:12, finney:15, ether:18, kether:21, mether:24, gether:27
        Map<String, String> map = new HashMap<>();
        map.put("0", "wei");
        map.put("3", "kwei");
        map.put("4", "wwei");
        map.put("6", "mwei");
        map.put("8", "lwei");
        map.put("9", "gwei");
        map.put("12", "szabo");
        map.put("15", "finney");
        map.put("18", "ether");
        map.put("21", "kether");
        map.put("24", "mether");
        map.put("27", "gether");

        List<ContractDTO> list = coinFeign.getContractByProtocol(coin.getUnit());
        
        if (list != null && list.size() > 0) {
            for (ContractDTO dto : list) {
                Contract contract = new Contract();
                BeanUtils.copyProperties(dto, contract);
                contract.setGasLimit(coin.getGasLimit());
                // #wei:0, kwei:3, wwei:4, mwei:6, gwei:9, szabo:12, finney:15, ether:18, kether:21, mether:24, gether:27
                contract.setDecimals(map.get(dto.getDecimals() + ""));
                ContractsConfig.updateContract(contract);
            }
        }

        
    }

    public static void main(String[] args) {

    }
}
