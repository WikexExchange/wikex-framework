package com.wikex.wikex.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.service.UdunService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Withdraw;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.MemberFundingWalletFeign;
import com.wikex.wikex.user.feign.WithdrawFeign;
import com.wikex.wikex.util.MessageResult;
import com.uduncloud.sdk.client.UdunClient;
import com.uduncloud.sdk.domain.Trade;
import com.uduncloud.sdk.util.UdunUtils;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("wallet")
public class CallbackController {
    @Autowired
    private UdunClient udunClient;

    @Autowired
    private UdunService udunService;

    @Autowired
    private MemberFundingWalletFeign memberFundingWalletFeign;

    @Autowired
    private WithdrawFeign withdrawRecordService;

    @Autowired
    private CoinFeign coinService;

    private Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @RequestMapping("notify")
    public synchronized String tradeCallback(@RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("body") String body,
            @RequestParam("sign") String sign) throws Exception {
        logger.info("timestamp:{},nonce:{},sign:{},body:{}", timestamp, nonce, sign, body);

        if (!UdunUtils.checkSign(udunClient.getMerchantKey(), timestamp, nonce, body, sign)) {
            return "error";
        }
        Trade trade = JSONObject.parseObject(body, Trade.class);
        logger.info("trade:{}", JSON.toJSONString(trade));
        // TODO business processing
        if (trade.getTradeType() == 1) {
            logger.info("===== Received deposit notification ======");
            logger.info("address:{},amount:{},coinType:{},fee:{}", trade.getAddress(), trade.getAmount(),
                    trade.getCoinType(), trade.getFee());
            // Amount and fee are in the smallest unit and need conversion, including amount
            // and fee fields
            BigDecimal amount = trade.getAmount().divide(BigDecimal.TEN.pow(trade.getDecimals()), 8, RoundingMode.DOWN);
            BigDecimal fee = trade.getFee().divide(BigDecimal.TEN.pow(trade.getDecimals()), 8, RoundingMode.DOWN);
            logger.info("amount={},fee={}", amount.toPlainString(), fee.toPlainString());

            String txid = trade.getTxId();
            String address = trade.getAddress();
            Coin coin = udunService.convert2Coin(trade);
            logger.info("coin={}", coin);
            if (coin != null
                    && memberFundingWalletFeign.findDeposit(address, txid) == null
                    && amount.compareTo(coin.getMinRechargeAmount()) >= 0) {
                MessageResult mr = memberFundingWalletFeign.recharge(coin.getUnit(), address, amount, txid, "", 0L);
                logger.info("wallet recharge result:{}", mr);
            }
        } else if (trade.getTradeType() == 2) {
            logger.info("===== Received withdrawal processing notification =====");
            logger.info("address:{},amount:{},coinType:{},businessId:{}", trade.getAddress(), trade.getAmount(),
                    trade.getCoinType(), trade.getBusinessId());
            Long withdrawId = Long.parseLong(trade.getBusinessId());
            Withdraw withdrawRecord = withdrawRecordService.findOne(withdrawId);
            if (withdrawRecord == null) {
                return "success";
            }
            String txid = trade.getTxId();
            // If transfer fails, status reverts to 'awaiting release'
            if (trade.getStatus() == 1) {
                logger.info("Approved, transferring");
                // Withdrawal transaction sent; process withdrawal order status and deduct
                // withdrawal funds
                if (withdrawRecord.getStatus() == 2 && StringUtil.isEmpty(withdrawRecord.getHash())) {
                    withdrawRecordService.withdrawSuccess(withdrawId, txid);
                } else {
                    return "success";
                }
            } else if (trade.getStatus() == 2) {
                logger.info("Approval failed");
                // Handle withdrawal order status; order number is businessId
                if (withdrawRecord.getStatus() == 2 && StringUtil.isEmpty(withdrawRecord.getHash())) {
                    withdrawRecordService.withdrawFail(withdrawId);
                } else {
                    return "success";
                }
            } else if (trade.getStatus() == 3) {
                logger.info("Withdrawal credited withdrawId:{}, txid:{}", withdrawRecord.getId(), txid);
                // Withdrawal credited; you can notify the user
                // withdrawRecordService.updateWithrawTxid(withdrawId, trade.getTxId());
            }
        }
        return "success";
    }
}
