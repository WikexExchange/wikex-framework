package com.wikex.wikex.admin.controller.swap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.feign.ContractCoinFeign;
import com.wikex.wikex.swap.feign.MemberContractWalletFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/swap-coin")
@Slf4j
public class ContractCoinController extends BaseAdminController {

    @Autowired
    private ContractCoinFeign contractCoinFeign;
    @Autowired
    private MemberContractWalletFeign memberContractWalletFeign;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Get perpetual contract trading pair list
     * @param pageParam
     * @return
     */
    @RequiresPermissions("swap-coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Perpetual contract trading pair list")
    public MessageResult list(PageParam pageParam) {
        Page<ContractCoin> coinList = contractCoinFeign.findAll(pageParam);
        return success(IPage2Page(coinList));
    }

    /**
     * Get perpetual contract trading pair details
     * @param contractId
     * @return
     */
    @RequiresPermissions("swap-coin:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Perpetual contract trading pair details")
    public MessageResult detail(@RequestParam(value = "symbol") Long contractId) {
        ContractCoin coin = contractCoinFeign.findOne(contractId);
        if(coin == null){
            return error(messageSource.getMessage("CONTRACT_NOT_EXIST"));
        }
        return success(coin);
    }

    /**
     * Add perpetual contract trading pair
     * @param contractCoin
     * @return
     */
    @RequiresPermissions("swap-coin:add")
    @PostMapping("add")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Add perpetual contract trading pair")
    public MessageResult add(@Valid ContractCoin contractCoin) {
        ContractCoin coin = contractCoinFeign.findBySymbol(contractCoin.getSymbol());
        if(coin != null) {
            return error(messageSource.getMessage("ADD_FAILED_PAIR") + contractCoin.getSymbol() + messageSource.getMessage("ALREADY_EXISTS"));
        }
        contractCoin.setTotalProfit(BigDecimal.ZERO);
        contractCoin.setTotalCloseFee(BigDecimal.ZERO);
        contractCoin.setTotalLoss(BigDecimal.ZERO);
        contractCoin.setTotalOpenFee(BigDecimal.ZERO);
        contractCoin = contractCoinFeign.save(contractCoin);
        return MessageResult.getSuccessInstance(messageSource.getMessage("ADD_PAIR_SUCCESS"), contractCoin);
    }

    /**
     * Modify perpetual contract trading pair information
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
     * @param isIfind
     * @param ifindCode
     * @param needConvert
     * @return
     */
    @RequiresPermissions("swap-coin:alter")
    @PostMapping("alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Modify perpetual contract trading pair")
    public MessageResult alter(
            @RequestParam("id") Long id,
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "sort", required = false) Integer sort, // Sorting
            @RequestParam(value = "enable", required = false) Integer enable, // Listing status (1: listed, 2: delisted)
            @RequestParam(value = "baseCoinScale", required = false) Integer baseCoinScale, // Base coin precision
            @RequestParam(value = "coinScale", required = false) Integer coinScale, // Coin precision
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
            @RequestParam(value = "leverage", required = false) String leverage, // Allowed leverage
            @RequestParam(value = "minShare", required = false) BigDecimal minShare,
            @RequestParam(value = "maxShare", required = false) BigDecimal maxShare,
            @RequestParam(value = "intervalHour", required = false) Integer intervalHour,
            @RequestParam(value = "feePercent", required = false) BigDecimal feePercent,
            @RequestParam(value = "maintenanceMarginRate", required = false) BigDecimal maintenanceMarginRate,
            @RequestParam(value = "openFee", required = false) BigDecimal openFee,
            @RequestParam(value = "closeFee", required = false) BigDecimal closeFee,
            @RequestParam(value = "takerFee", required = false) BigDecimal takerFee,
            @RequestParam(value = "makerFee", required = false) BigDecimal makerFee,
            @RequestParam(value = "isIfind", required = false) Integer isIfind,
            @RequestParam(value = "ifindCode", required = false) String ifindCode,
            @RequestParam(value = "needConvert", required = false) Integer needConvert
    ) {
        ContractCoin coin = contractCoinFeign.findOne(id);
        if(coin == null) {
            return error(messageSource.getMessage("PAIR") + symbol + messageSource.getMessage("NOT_FOUND"));
        }

        if(baseCoinScale != null) coin.setBaseCoinScale(baseCoinScale);
        if(coinScale != null) coin.setCoinScale(coinScale);
        if(name != null) coin.setName(name);
        if(sort != null) coin.setSort(sort);
        if(enable != null) coin.setEnable(enable);
        if(visible != null) coin.setVisible(visible);
        if(exchangeable != null) coin.setExchangeable(exchangeable);
        if(enableOpenSell != null) coin.setEnableOpenSell(enableOpenSell == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if(enableOpenBuy != null) coin.setEnableOpenBuy(enableOpenBuy == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if(enableMarketSell != null) coin.setEnableMarketSell(enableMarketSell == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if(enableMarketBuy != null) coin.setEnableMarketBuy(enableMarketBuy == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if(enableTriggerEntrust != null) coin.setEnableTriggerEntrust(enableTriggerEntrust == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
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
        if(isIfind != null) coin.setIsIfind(isIfind);
        coin.setIfindCode(ifindCode);
        if(needConvert != null) coin.setNeedConvert(needConvert);

        contractCoinFeign.save(coin);
        return success(messageSource.getMessage("SAVE_SUCCESS"));
    }

    /**
     * Batch add wallets for users
     * @param contractId
     * @return
     */
    @RequiresPermissions("swap-coin:init-wallet")
    @PostMapping("init-wallet")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Add wallets for perpetual contract trading pair")
    public MessageResult generateWallet(@RequestParam("contractId") Long contractId) {
        ContractCoin coin = contractCoinFeign.findOne(contractId);
        if(coin == null) {
            return MessageResult.error(messageSource.getMessage(messageSource.getMessage("CONTRACT_CURRENCY_CONFIGURATION_NOT_FOUND")));
        }
        memberContractWalletFeign.initWallet(contractId);
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }

    /**
     * Forced liquidation
     * @param contractId
     * @param price
     * @return
     */
    @RequiresPermissions("swap-coin:blast")
    @PostMapping("blast")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Perpetual contract forced liquidation")
    public MessageResult blast(@RequestParam("contractId") Long contractId, @RequestParam("price") BigDecimal price) {
        ContractCoin coin = contractCoinFeign.findOne(contractId);
        if(coin == null) {
            return MessageResult.error(messageSource.getMessage("CONTRACT_CURRENCY_CONFIGURATION_NOT_FOUND"));
        }

        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }

    /**
     * Poke (trigger a manual check)
     * @param contractId
     * @param price
     * @return
     */
    @RequiresPermissions("swap-coin:poke")
    @PostMapping("poke")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Perpetual contract poke")
    public MessageResult poke(@RequestParam("contractId") Long contractId, @RequestParam("price") BigDecimal price) {
        ContractCoin coin = contractCoinFeign.findOne(contractId);
        if(coin == null) {
            return MessageResult.error(messageSource.getMessage("CONTRACT_CURRENCY_CONFIGURATION_NOT_FOUND"));
        }
        JSONObject msg = new JSONObject();
        msg.put("price", price);
        msg.put("symbol", coin.getSymbol());
        rocketMQTemplate.convertAndSend("admin-save-swap-poke", JSON.toJSONString(msg));
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }
}
