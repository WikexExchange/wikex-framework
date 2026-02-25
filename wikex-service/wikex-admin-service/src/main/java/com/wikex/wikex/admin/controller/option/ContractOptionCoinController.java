package com.wikex.wikex.admin.controller.option;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.feign.ContractOptionCoinFeign;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;

@RestController
@RequestMapping("/option-coin")
@Slf4j
public class ContractOptionCoinController extends BaseAdminController {

    @Autowired
    private ContractOptionCoinFeign contractOptionCoinService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Get the list of option contract trading pairs
     * @param pageParam
     * @return
     */
    @RequiresPermissions("option-coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Option contract trading pair list")
    public MessageResult list(PageParam pageParam) {
        Page<ContractOptionCoin> coinList = contractOptionCoinService.findAll(pageParam);
        return success(IPage2Page(coinList));
    }

    /**
     * Get the details of an option contract trading pair
     * @param symbol
     * @return
     */
    @RequiresPermissions("option-coin:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Option contract trading pair details")
    public MessageResult detail(@RequestParam(value = "symbol") String symbol) {
        ContractOptionCoin coin = contractOptionCoinService.findOneBySymbol(symbol);
        if(coin == null){
            return error(messageSource.getMessage("PAIR_NOT_FOUND"));
        }
        return success(coin);
    }

    /**
     * Add an option contract trading pair
     * @param contractOptionCoin
     * @return
     */
    @RequiresPermissions("option-coin:add")
    @PostMapping("add")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Add new option contract trading pair")
    public MessageResult add(@Valid ContractOptionCoin contractOptionCoin) {
        ContractOptionCoin coin = contractOptionCoinService.findOneBySymbol(contractOptionCoin.getSymbol());
        if(coin != null) {
            return error(messageSource.getMessage("ADD_FAILED_PAIR_ALREADY_EXISTS") + contractOptionCoin.getSymbol() + messageSource.getMessage("ALREADY_EXISTS"));
        }
        if(contractOptionCoin.getCloseTimeGap() <= 0 || contractOptionCoin.getOpenTimeGap() <= 0) {
            return error(messageSource.getMessage("BET_OR_DRAW_INTERVAL_MUST_BE_GREATER_THAN_0"));
        }
        contractOptionCoin.setCreateTime(new Date());
        contractOptionCoin.setTotalProfit(BigDecimal.ZERO);
        contractOptionCoinService.add(contractOptionCoin);
        return MessageResult.getSuccessInstance(messageSource.getMessage("ADD_PAIR_SUCCESS"), coin);
    }

    /**
     * Modify an option contract trading pair
     * @param symbol
     * @param enable     Shelf status (1: up, 2: down)
     * @param enableBuy  Can buy (1: yes, 0: no)
     * @param enableSell Can sell (1: yes, 0: no)
     * @param visible    Visibility (1: yes, 2: no)
     * @param sort       Sort order
     * @param amount     Allowed betting amount
     * @param feePercent Fee percentage
     * @param winFeePercent Win fee percentage
     * @param openTimeGap Opening interval
     * @param closeTimeGap Closing interval
     * @param tiedType   Tie result handling type
     * @return
     */
    @RequiresPermissions("option-coin:alter")
    @PostMapping("alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Modify option contract trading pair")
    public MessageResult alter(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "enable", required = false) Integer enable, // Shelf status (1: up, 2: down)
            @RequestParam(value = "enableBuy", required = false) Integer enableBuy, // Can buy (1: yes, 0: no)
            @RequestParam(value = "enableSell", required = false) Integer enableSell, // Can sell (1: yes, 0: no)
            @RequestParam(value = "visible", required = false) Integer visible, // Visibility (1: yes, 2: no)
            @RequestParam(value = "sort", required = false) Integer sort, // Sort order
            @RequestParam(value = "amount", required = false) String amount, // Allowed betting amount
            @RequestParam(value = "feePercent", required = false) BigDecimal feePercent,
            @RequestParam(value = "oods", required = false) BigDecimal oods,
            @RequestParam(value = "winFeePercent", required = false) BigDecimal winFeePercent,
            @RequestParam(value = "openTimeGap", required = false) Integer openTimeGap,
            @RequestParam(value = "closeTimeGap", required = false) Integer closeTimeGap,
            @RequestParam(value = "initBuyReward", required = false) BigDecimal initBuyReward,
            @RequestParam(value = "initSellReward", required = false) BigDecimal initSellReward,
            @RequestParam(value = "tiedType", required = false) Integer tiedType
    ) {
        ContractOptionCoin coin = contractOptionCoinService.findOneBySymbol(symbol);
        if(coin == null) {
            return error(messageSource.getMessage("PAIR") + coin.getSymbol() + messageSource.getMessage("NOT_FOUND"));
        }

        if(enable != null) coin.setEnable(enable);
        if(enableBuy != null) coin.setEnableBuy(enableBuy == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        if(enableSell != null) coin.setEnableSell(enableSell == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        if(visible != null) coin.setVisible(visible);
        if(sort != null) coin.setSort(sort);
        if(amount != null) coin.setAmount(amount);
        if(feePercent != null) coin.setFeePercent(feePercent);
        if(winFeePercent != null) coin.setWinFeePercent(winFeePercent);
        if(openTimeGap != null) coin.setOpenTimeGap(openTimeGap);
        if(closeTimeGap != null) coin.setCloseTimeGap(closeTimeGap);
        if(tiedType != null) coin.setTiedType(tiedType);
        if(initBuyReward != null) coin.setInitBuyReward(initBuyReward);
        if(initSellReward != null) coin.setInitSellReward(initSellReward);
        if(oods != null) coin.setOods(oods);

        contractOptionCoinService.alert(coin);
        return success(messageSource.getMessage("SAVE_SUCCESS"));
    }
}
