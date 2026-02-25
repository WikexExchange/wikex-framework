package com.wikex.wikex.admin.controller.coinswap;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.coinswap.feign.ContractCoinMarketFeign;
import com.wikex.wikex.coinswap.feign.MemberContractCoinWalletFeign;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.screen.MemberContractWalletCoinScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Position Management
 */
@RestController
@RequestMapping("/coinswap/position")
@Slf4j
public class MemberContractWalletCoinController extends BaseAdminController {
    @Autowired
    private MemberContractCoinWalletFeign memberContractWalletService;
    @Autowired
    private ContractCoinMarketFeign contractCoinMarketFeign;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("coinswap:position:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract user position management - list")
    public MessageResult detail(
            MemberContractWalletCoinScreen screen) {

        Page<MemberContractWalletCoin> all = memberContractWalletService.findAll(screen);
        // Get latest prices
        List<CoinThumb> thumbList = contractCoinMarketFeign.findSymbolThumb4Feign();

        List<MemberContractWalletCoin> list = all.getRecords();
        for (MemberContractWalletCoin wallet : list) {
            for (int i = 0; i < thumbList.size(); i++) {
                CoinThumb thumb = thumbList.get(i);
                if (wallet.getContractCoin().getSymbol().equals(thumb.getSymbol())) {
                    wallet.setCurrentPrice(thumb.getClose());
                }
            }
            // Set CNY / USDT exchange rate
            wallet.setCnyRate(BigDecimal.valueOf(6.98));
        }
        return success(IPage2Page(all));
    }


    /**
     * Force market close (liquidation)
     * @param walletId wallet ID
     * @return result
     */
    @RequiresPermissions("coinswap:order:force-close")
    @PostMapping("force-close")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract user position management - force close")
    public MessageResult forceClose(Long walletId) {
//        MemberContractWalletCoin wallet = memberContractWalletService.findOne(walletId);
//        if(wallet == null) {
//            return MessageResult.error("Cancel entrust failed");
//        }
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }

}
