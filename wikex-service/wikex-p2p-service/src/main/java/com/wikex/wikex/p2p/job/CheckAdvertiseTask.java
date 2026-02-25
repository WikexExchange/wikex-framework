package com.wikex.wikex.p2p.job;

import com.wikex.wikex.p2p.config.CoinExchangeFactory;
import com.wikex.wikex.p2p.service.AdvertiseService;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class CheckAdvertiseTask {
    @Autowired
    private CoinExchangeFactory coins;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private AdvertiseService advertiseService;

//    @Scheduled(fixedRate = 60000 * 30)
    @XxlJob("checkExpireAdvertise")
    public void checkExpireAdvertise() {
        
        // Supported coins
//        List<OtcCoin> list = otcCoinService.getNormalCoin();
//        
//        Map<String, HashMap<String,BigDecimal>> map = coins.getCoins();
//        
//        list.stream().forEach(
//                x -> {
//                    HashMap<String,BigDecimal> marketPrice = map.get(x.getUnit());
//                    try {
//                        List<AdvertiseVo> list1 = advertiseService.selectSellAutoOffShelves(x.getId(), marketPrice.get(""), x.getJyRate());
//                        List<AdvertiseVo> list2 = advertiseService.selectBuyAutoOffShelves(x.getId(), marketPrice.get(""));
//                        list1.addAll(list2);
//                        list1.stream().forEach(
//                                y -> {
//                                    try {
//                                        advertiseService.autoPutOffShelves(y, x);
//                                    } catch (InformationExpiredException e) {
//                                        e.printStackTrace();
//                                        log.warn("Advertise ID {}: auto put-off failed", y.getId());
//                                    }
//                                }
//                        );
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//        );
        
    }
}
