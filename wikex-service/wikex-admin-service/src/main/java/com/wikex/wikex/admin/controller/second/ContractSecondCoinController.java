package com.wikex.wikex.admin.controller.second;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.feign.ContractSecondCoinFeign;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.feign.MemberSecondWalletFeign;
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

@RestController
@RequestMapping("/second-coin")
@Slf4j
public class ContractSecondCoinController extends BaseAdminController {

    @Autowired
    private ContractSecondCoinFeign contractCoinService;
    @Autowired
    private MemberSecondWalletFeign memberSecondWalletService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Get the list of perpetual contract pairs
     * @param pageParam
     * @return
     */
    @RequiresPermissions("second-coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Second contract pair list")
    public MessageResult list(PageParam pageParam) {
        Page<ContractSecondCoin> coinList = contractCoinService.findAll(pageParam);
        return success(IPage2Page(coinList));
    }

    /**
     * Get details of a perpetual contract pair
     * @param contractId
     * @return
     */
    @RequiresPermissions("second-coin:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Second contract pair details")
    public MessageResult detail(@RequestParam(value = "symbol") Long contractId) {
        ContractSecondCoin coin = contractCoinService.findOne(contractId);
        if(coin == null){
            return error(messageSource.getMessage("PAIR_NOT_FOUND"));
        }
        return success(coin);
    }

    /**
     * Add a new perpetual contract pair
     * @param contractCoin
     * @return
     */
    @RequiresPermissions("second-coin:add")
    @PostMapping("add")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Add new second contract pair")
    public MessageResult add(@Valid ContractSecondCoin contractCoin) {
        ContractSecondCoin coin = contractCoinService.findBySymbol(contractCoin.getSymbol());
        if(coin != null) {
            return error(messageSource.getMessage("ADD_FAILED_PAIR") + contractCoin.getSymbol() + messageSource.getMessage("ALREADY_EXISTS"));
        }
        contractCoin.setTotalProfit(BigDecimal.ZERO);
        contractCoin.setTotalCloseFee(BigDecimal.ZERO);
        contractCoin.setTotalLoss(BigDecimal.ZERO);
        contractCoin.setTotalOpenFee(BigDecimal.ZERO);
        contractCoin = contractCoinService.save(contractCoin);
        return MessageResult.getSuccessInstance(messageSource.getMessage("ADD_PAIR_SUCCESS"), contractCoin);
    }

    /**
     * Modify perpetual contract pair information
     * @param id
     * @param symbol
     * @param sort
     * @param enable
     * @param visible
     * @param exchangeable
     * @param enableOpenSell
     * @param enableOpenBuy
     * @param enableMarketSell
     * @param enableMarketBuy
     * @param enableTriggerEntrust
     * @param spreadType
     * @param spread
     * @param leverageType
     * @param leverage
     * @param minShare
     * @param maxShare
     * @param intervalHour
     * @param feePercent
     * @param maintenanceMarginRate
     * @param openFee
     * @param closeFee
     * @param takerFee
     * @param makerFee
     * @return
     */
    @RequiresPermissions("second-coin:alter")
    @PostMapping("alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Modify second contract pair")
    public MessageResult alter(
            @RequestParam("id") Long id,
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "sort", required = false) Integer sort, // Sorting
            @RequestParam(value = "enable", required = false) Integer enable, // Listing status (1: listed, 2: delisted)
            @RequestParam(value = "visible", required = false) Integer visible, // Visible (1: yes, 2: no)
            @RequestParam(value = "exchangeable", required = false) Integer exchangeable, // Exchangeable (1: yes, 2: no)
            @RequestParam(value = "enableOpenSell", required = false) Integer enableOpenSell, // Allow open sell (1: yes, 0: no)
            @RequestParam(value = "enableOpenBuy", required = false) Integer enableOpenBuy, // Allow open buy (1: yes, 0: no)
            @RequestParam(value = "enableMarketSell", required = false) Integer enableMarketSell, // Allow market sell (1: yes, 0: no)
            @RequestParam(value = "enableMarketBuy", required = false) Integer enableMarketBuy, // Allow market buy (1: yes, 0: no)
            @RequestParam(value = "enableTriggerEntrust", required = false) Integer enableTriggerEntrust, // Allow trigger entrust (1: yes, 0: no)
            @RequestParam(value = "spreadType", required = false) Integer spreadType, // Spread type
            @RequestParam(value = "spread", required = false) BigDecimal spread,
            @RequestParam(value = "leverageType", required = false) Integer leverageType, // Leverage type
            @RequestParam(value = "leverage", required = false) String leverage, // Allowed leverage values
            @RequestParam(value = "minShare", required = false) BigDecimal minShare,
            @RequestParam(value = "maxShare", required = false) BigDecimal maxShare,
            @RequestParam(value = "intervalHour", required = false) Integer intervalHour,
            @RequestParam(value = "feePercent", required = false) BigDecimal feePercent,
            @RequestParam(value = "maintenanceMarginRate", required = false) BigDecimal maintenanceMarginRate,
            @RequestParam(value = "openFee", required = false) BigDecimal openFee,
            @RequestParam(value = "closeFee", required = false) BigDecimal closeFee,
            @RequestParam(value = "takerFee", required = false) BigDecimal takerFee,
            @RequestParam(value = "makerFee", required = false) BigDecimal makerFee
    ) {
        ContractSecondCoin coin = contractCoinService.findOne(id);
        if(coin == null) {
            return error(messageSource.getMessage("PAIR") + symbol + messageSource.getMessage("NOT_FOUND"));
        }

        if(name != null) coin.setName(name);
        if(sort != null) coin.setSort(sort);
        if(enable != null) coin.setEnable(enable);
        if(visible != null) coin.setVisible(visible);
        if(exchangeable != null) coin.setExchangeable(exchangeable);
        if(enableOpenSell != null) coin.setEnableOpenSell(enableOpenSell == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        if(enableOpenBuy != null) coin.setEnableOpenBuy(enableOpenBuy == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        if(enableMarketSell != null) coin.setEnableMarketSell(enableMarketSell == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        if(enableMarketBuy != null) coin.setEnableMarketBuy(enableMarketBuy == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        if(enableTriggerEntrust != null) coin.setEnableTriggerEntrust(enableTriggerEntrust == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        if(spreadType != null) coin.setSpreadType(spreadType);
        if(spread != null) coin.setSpread(spread);
        if(leverageType != null) coin.setLeverageType(leverageType);
        if(leverage != null) coin.setLeverage(leverage);
        if(minShare != null) coin.setMinShare(minShare);
        if(maxShare != null) coin.setMaxShare(maxShare);
        if(intervalHour != null) coin.setIntervalHour(intervalHour);
        if(feePercent != null) coin.setFeePercent(feePercent);
        if(maintenanceMarginRate != null) coin.setMaintenanceMarginRate(maintenanceMarginRate);
        if(openFee != null) coin.setOpenFee(openFee);
        if(closeFee != null) coin.setCloseFee(closeFee);
        if(takerFee != null) coin.setTakerFee(takerFee);
        if(makerFee != null) coin.setMakerFee(makerFee);

        contractCoinService.save(coin);
        return success(messageSource.getMessage("SAVE_SUCCESS"));
    }

}
