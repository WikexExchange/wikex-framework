package com.wikex.wikex.admin.controller.swap;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.feign.ContractMarketFeign;
import com.wikex.wikex.swap.feign.MemberContractWalletFeign;
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
@RequestMapping("/swap/position")
@Slf4j
public class MemberContractWalletController extends BaseAdminController {
    @Autowired
    private MemberContractWalletFeign memberContractWalletService;
    @Autowired
    private ContractMarketFeign contractMarketFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("swap:position:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Perpetual contract user position management - List")
    public MessageResult detail(
            MemberContractWalletScreen screen) {
        // Get query conditions
        Page<MemberContractWallet> all = memberContractWalletService.findAll(screen);
        // Get latest prices
        List<CoinThumb> thumbList = contractMarketFeign.findSymbolThumb4Feign();

        List<MemberContractWallet> list = all.getRecords();
        for(MemberContractWallet wallet : list) {
            for(int i = 0; i < thumbList.size(); i++) {
                CoinThumb thumb = thumbList.get(i);
                if(wallet.getSymbol().equals(thumb.getSymbol())) {
                    wallet.setCurrentPrice(thumb.getClose());
                }
            }

            // Set CNY / USDT exchange rate
            wallet.setCnyRate(BigDecimal.valueOf(6.98));
        }
        return success(IPage2Page(all));
    }

    /**
     * Force market close position
     * @param walletId
     * @return
     */
    @RequiresPermissions("swap:order:force-close")
    @PostMapping("force-close")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Perpetual contract user position management - Force close position")
    public MessageResult forceClose(Long walletId) {
        // Not implemented
//        MemberContractWallet wallet = memberContractWalletService.findOne(walletId);
//        if(wallet == null) {
//            return MessageResult.error("Cancel entrust failed");
//        }
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }

}
