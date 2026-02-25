package com.wikex.wikex.feign;

import com.wikex.wikex.service.UdunService;
import com.uduncloud.sdk.constant.MainCoinType;
import com.uduncloud.sdk.domain.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("udun")
public class UdunFeignController {
    @Autowired
    private UdunService biPayService;

    /**
     * Create a new address
     *
     * @param symbol
     * @return
     */
    @RequestMapping("create-address")
    public Address createCoinAddress(@RequestParam("symbol") String symbol) {
        return biPayService.createCoinAddress(MainCoinType.symbolOf(symbol), "", "");
    }

//    /**
//     * Initiate a transfer request
//     *
//     * @param coinType
//     * @param amount
//     * @param address
//     * @return
//     */
//    @RequestMapping("transfer")
//    public ResponseMessage<String> transfer(int coinType, BigDecimal amount, String address, String memo) {
//        String orderId = String.valueOf(Calendar.getInstance().getTimeInMillis());
//        CoinType coin = CoinType.codeOf(coinType);
//        ResponseMessage<String> resp = biPayService.transfer(orderId, amount, coin, coin.getCode(), address, memo);
//        return resp;
//    }
//
//    /**
//     * Proxy payment (auto transfer)
//     *
//     * @param coinType
//     * @param amount
//     * @param address
//     * @return
//     */
//    @RequestMapping("autotransfer")
//    public ResponseMessage<String> autoTransfer(int coinType, BigDecimal amount, String address, String memo) {
//        String orderId = String.valueOf(Calendar.getInstance().getTimeInMillis());
//        CoinType coin = CoinType.codeOf(coinType);
//        ResponseMessage<String> resp = biPayService.autoTransfer(orderId, amount, coin, coin.getCode(), address, memo);
//        return resp;
//    }
//
//    @RequestMapping("test")
//    public String test() {
//        return "Success";
//    }
//
//    @RequestMapping("transaction")
//    public List<Transaction> queryTransaction() throws Exception {
//        return biPayService.queryTransaction();
//    }
//
//    /**
//     * Validate address legitimacy
//     * @param mainCoinType
//     * @param address
//     */
//    @RequestMapping("checkAddress")
//    public boolean checkAddress(String mainCoinType, String address) throws Exception {
//        return biPayService.checkAddress(mainCoinType, address);
//    }
//
//    /**
//     * Get supported coins
//     * @param showBalance
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping("getSupportCoin")
//    public List<SupportCoin> getSupportCoin(Boolean showBalance) throws Exception {
//        return biPayService.getSupportCoin(showBalance);
//    }
}
