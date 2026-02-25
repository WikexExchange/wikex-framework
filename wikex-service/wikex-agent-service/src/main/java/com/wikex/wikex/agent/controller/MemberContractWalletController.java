package com.wikex.wikex.agent.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.coinswap.feign.ContractCoinMarketFeign;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.feign.ContractCoinFeign;
import com.wikex.wikex.swap.feign.MemberContractWalletFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Position Management
 */
@RestController
@RequestMapping("/swap/position")
@Slf4j
public class MemberContractWalletController extends BaseController {
    @Autowired
    private MemberContractWalletFeign memberContractWalletFeign;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractCoinMarketFeign contractCoinMarketFeign;
    @Autowired
    private ContractCoinFeign contractCoinFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Paginated query of member contract wallets
     */
    @PermissionOperation
    @PostMapping("page-query")
    public MessageResult detail(
            MemberContractWalletScreen screen, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        // Default sort by balance in descending order
        ArrayList<Sort.Direction> directions = new ArrayList<>();
        directions.add(Sort.Direction.DESC);
        screen.setDirection(directions);
        List<String> property = new ArrayList<>();
        property.add("usdtBalance"); // default amount sorting
        screen.setProperty(property);
        Page<MemberContractWallet> all = memberContractWalletFeign.findAll(screen);

        // Get latest prices
        List<CoinThumb> thumbList = contractCoinMarketFeign.findSymbolThumb4Feign();
        List<MemberContractWallet> list = all.getRecords();
        for (MemberContractWallet wallet : list) {
            for (int i = 0; i < thumbList.size(); i++) {
                CoinThumb thumb = thumbList.get(i);
                if (wallet.getSymbol().equals(thumb.getSymbol())) {
                    wallet.setCurrentPrice(thumb.getClose());
                }
            }
            ContractCoin coin = new ContractCoin();
            coin.setSymbol(wallet.getSymbol());
            wallet.setContractCoin(coin);
            // Set CNY/USDT exchange rate
            wallet.setCnyRate(BigDecimal.valueOf(6.98));
        }
        return success(IPage2Page(all));
    }

    /**
     * Force close a contract position
     */
    @PostMapping("force-close")
    public MessageResult forceClose(Long walletId) {
        MemberContractWallet wallet = memberContractWalletFeign.findOne(walletId);
        if (wallet == null) {
            return MessageResult.error(messageSource.getMessage("CANCEL_ORDER_FAILED"));
        }
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }
}
