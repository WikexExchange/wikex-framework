package com.wikex.wikex.option.config;

import com.wikex.wikex.constant.ContractOptionOrderResult;
import com.wikex.wikex.constant.ContractOptionOrderStatus;
import com.wikex.wikex.constant.ContractOptionResult;
import com.wikex.wikex.constant.ContractOptionStatus;
import com.wikex.wikex.option.client.Client;
import com.wikex.wikex.option.engine.ContractOptionCoinMatch;
import com.wikex.wikex.option.engine.ContractOptionCoinMatchFactory;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.entity.ContractOptionOrder;
import com.wikex.wikex.option.handler.MongoMarketHandler;
import com.wikex.wikex.option.handler.NettyHandler;
import com.wikex.wikex.option.handler.WebsocketMarketHandler;
import com.wikex.wikex.option.job.ExchangePushJob;
import com.wikex.wikex.option.service.ContractMarketService;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.option.service.ContractOptionOrderService;
import com.wikex.wikex.option.service.ContractOptionService;
import com.wikex.wikex.option.socket.client.WsClientHuobi;
import com.wikex.wikex.option.util.WebSocketConnectionManage;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ContractCoinMatchStarter implements ApplicationRunner {

    private Logger log = LoggerFactory.getLogger(ContractCoinMatchStarter.class);

    @Autowired
    private Client client;

    @Autowired
    private ContractOptionCoinService contractOptionCoinService;

    @Autowired
    private ContractMarketService marketService;

    @Autowired
    private ContractOptionService contractOptionService;

    @Autowired
    private ContractOptionOrderService contractOptionOrderService;

    @Autowired
    private ExchangePushJob exchangePushJob;

    @Autowired
    MongoMarketHandler mongoMarketHandler;

    @Autowired
    WebsocketMarketHandler wsHandler;

    @Autowired
    NettyHandler nettyHandler;

    @Autowired
    private ContractOptionCoinMatchFactory factory;

    @Autowired
    private MemberWalletFeign walletService;

    @Override
    public void run(ApplicationArguments args){

        
        List<ContractOptionCoin> contractOptionCoinList = contractOptionCoinService.findAllEnabled();

        for (ContractOptionCoin coin : contractOptionCoinList) {
            ContractOptionCoinMatch match = new ContractOptionCoinMatch(coin.getSymbol());
            match.addHandler(mongoMarketHandler);
            match.addHandler(wsHandler);
            match.addHandler(nettyHandler);
            match.setExchangePushJob(exchangePushJob);
            match.run();
            factory.addContractCoinMatch(coin.getSymbol(), match);
        }

        

        
        WebSocketConnectionManage.setClient(client);

        WsClientHuobi w = new WsClientHuobi(factory);
        w.setContractOptionCoinService(contractOptionCoinService);
        w.setContractMarketService(marketService);
        w.setExchangePushJob(exchangePushJob);
        w.run();

        
        for (int i = 0; i < contractOptionCoinList.size(); i++) {
            
            List<ContractOption> optionsStarting = contractOptionService.findBySymbolAndStatus(contractOptionCoinList.get(i).getSymbol(), ContractOptionStatus.STARTING);
            for (int j = 0; j < optionsStarting.size(); j++) {
                ContractOption temOption = optionsStarting.get(j);
                temOption.setStatus(ContractOptionStatus.CANCELED); 
                temOption.setResult(ContractOptionResult.CANCELED); 

                List<ContractOptionOrder> orderList = contractOptionOrderService.findByOptionId(optionsStarting.get(j).getId());
                for (int k = 0; k < orderList.size(); k++) {
                    ContractOptionOrder temOrder = orderList.get(k);
                    walletService.thawBalance(temOrder.getBaseSymbol(), temOrder.getMemberId(), temOrder.getBetAmount());
                    if (temOrder.getFee().compareTo(BigDecimal.ZERO) > 0) {
                        walletService.thawBalance(temOrder.getBaseSymbol(), temOrder.getMemberId(), temOrder.getFee());
                    }

                    temOrder.setResult(ContractOptionOrderResult.CANCELED);
                    temOrder.setStatus(ContractOptionOrderStatus.CANCELED);

                    contractOptionOrderService.saveOrUpdate(temOrder);
                }
                contractOptionService.saveOrUpdate(temOption);
            }

            
            List<ContractOption> optionsOpening = contractOptionService.findBySymbolAndStatus(contractOptionCoinList.get(i).getSymbol(), ContractOptionStatus.OPENING);
            for (int j = 0; j < optionsOpening.size(); j++) {
                ContractOption temOption = optionsOpening.get(j);
                temOption.setStatus(ContractOptionStatus.CANCELED); 
                temOption.setResult(ContractOptionResult.CANCELED); 

                List<ContractOptionOrder> orderList = contractOptionOrderService.findByOptionId(optionsOpening.get(j).getId());
                for (int k = 0; k < orderList.size(); k++) {
                    ContractOptionOrder temOrder = orderList.get(k);

                    walletService.thawBalance(temOrder.getBaseSymbol(), temOrder.getMemberId(), temOrder.getBetAmount());

                    if (temOrder.getFee().compareTo(BigDecimal.ZERO) > 0) {
                        walletService.thawBalance(temOrder.getBaseSymbol(), temOrder.getMemberId(), temOrder.getFee());
                    }

                    temOrder.setResult(ContractOptionOrderResult.CANCELED);
                    temOrder.setStatus(ContractOptionOrderStatus.CANCELED);

                    contractOptionOrderService.saveOrUpdate(temOrder);
                }
                contractOptionService.saveOrUpdate(temOption);
            }
        }
    }
}
