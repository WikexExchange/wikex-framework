package com.wikex.wikex.admin.controller.coinswap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.feign.ContractCoinCoinFeign;
import com.wikex.wikex.coinswap.feign.MemberContractCoinWalletFeign;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
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
@RequestMapping("/coinswap-coin")
@Slf4j
public class ContractCoinCoinController extends BaseAdminController {

    @Autowired
    private ContractCoinCoinFeign contractCoinCoinService;
    @Autowired
    private MemberContractCoinWalletFeign memberContractWalletFeign;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Get coin-margined perpetual contract trading pair list
     * @return result
     */
    @RequiresPermissions("coinswap-coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract pair - list")
    public MessageResult list(PageParam pageParam) {
        Page<ContractCoinCoin> coinList = contractCoinCoinService.findAll(pageParam);
        return success(IPage2Page(coinList));
    }

    /**
     * Get coin-margined perpetual contract trading pair details
     * @param contractId contract id
     * @return result
     */
    @RequiresPermissions("coinswap-coin:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract pair - detail")
    public MessageResult detail(@RequestParam(value = "symbol") Long contractId) {
        ContractCoinCoin coin = contractCoinCoinService.findOne(contractId);
        if (coin == null) {
            return error(messageSource.getMessage("PAIR_NOT_FOUND"));
        }
        return success(coin);
    }

    /**
     * Add coin-margined perpetual contract trading pair
     * @param contractCoin pair info
     * @return result
     */
    @RequiresPermissions("coinswap-coin:add")
    @PostMapping("add")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract pair - add")
    public MessageResult add(@Valid ContractCoinCoin contractCoin) {
        ContractCoinCoin coin = contractCoinCoinService.findBySymbol(contractCoin.getSymbol());
        if (coin != null) {
            return error(messageSource.getMessage("ADD_FAILED") + "!" + messageSource.getMessage("ADD_FAILED") + contractCoin.getSymbol() + messageSource.getMessage("ALREADY_EXISTS"));
        }
        contractCoin.setTotalProfit(BigDecimal.ZERO);
        contractCoin.setTotalCloseFee(BigDecimal.ZERO);
        contractCoin.setTotalLoss(BigDecimal.ZERO);
        contractCoin.setTotalOpenFee(BigDecimal.ZERO);
        contractCoin = contractCoinCoinService.save(contractCoin);
        return MessageResult.getSuccessInstance(messageSource.getMessage("ADD_PAIR_SUCCESS"), contractCoin);
    }

    /**
     * Modify coin-margined perpetual contract trading pair
     * @param id            id
     * @param symbol        symbol
     * @param sort          sort order
     * @param enable        on/off shelf (1:on, 2)
     * @param visible       visible (1:yes, 2)
     * @param exchangeable  exchangeable (1:yes, 2)
     * @param enableOpenSell enable open short (1:yes, 0:no)
     * @param enableOpenBuy  enable open long (1:yes, 0:no)
     * @param enableMarketSell enable market short (1:yes, 0:no)
     * @param enableMarketBuy  enable market long (1:yes, 0:no)
     * @param enableTriggerEntrust enable trigger entrust (1:yes, 0:no)
     * @param spreadType    spread type (1:on, 2)
     * @param spread        spread
     * @param leverageType  leverage type (1:yes, 2)
     * @param leverage      leverage configuration
     * @param minShare      minimum shares
     * @param maxShare      maximum shares
     * @param intervalHour  settlement interval (hours)
     * @param feePercent    fee percent
     * @param maintenanceMarginRate maintenance margin rate
     * @param openFee       open fee
     * @param closeFee      close fee
     * @param takerFee      taker fee
     * @param makerFee      maker fee
     * @return result
     */
    @RequiresPermissions("coinswap-coin:alter")
    @PostMapping("alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract pair - modify")
    public MessageResult alter(
            @RequestParam("id") Long id,
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "sort", required = false) Integer sort, // sort
            @RequestParam(value = "enable", required = false) Integer enable, // on/off shelf (1:on, 2)
            @RequestParam(value = "visible", required = false) Integer visible, // visible (1:yes, 2)
            @RequestParam(value = "exchangeable", required = false) Integer exchangeable, // exchangeable (1:yes, 2)
            @RequestParam(value = "enableOpenSell", required = false) Integer enableOpenSell, // enable open short (1:yes,0:no)
            @RequestParam(value = "baseCoinScale", required = false) Integer baseCoinScale, // base coin scale
            @RequestParam(value = "coinScale", required = false) Integer coinScale,
            @RequestParam(value = "enableOpenBuy", required = false) Integer enableOpenBuy, // enable open long (1:yes,0:no)
            @RequestParam(value = "enableMarketSell", required = false) Integer enableMarketSell, // enable market short (1:yes,0:no)
            @RequestParam(value = "enableMarketBuy", required = false) Integer enableMarketBuy, // enable market long (1:yes,0:no)
            @RequestParam(value = "enableTriggerEntrust", required = false) Integer enableTriggerEntrust, // enable trigger entrust (1:yes,0:no)
            @RequestParam(value = "spreadType", required = false) Integer spreadType, // spread type
            @RequestParam(value = "spread", required = false) BigDecimal spread,
            @RequestParam(value = "leverageType", required = false) Integer leverageType, // leverage type
            @RequestParam(value = "leverage", required = false) String leverage, // leverage configuration
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
        ContractCoinCoin coin = contractCoinCoinService.findOne(id);
        if (coin == null) {
            return error(messageSource.getMessage("PAIR") + coin.getSymbol() + messageSource.getMessage("NOT_FOUND"));
        }

        if (baseCoinScale != null) coin.setBaseCoinScale(baseCoinScale);
        if (coinScale != null) coin.setCoinScale(coinScale);
        if (name != null) coin.setName(name);
        if (sort != null) coin.setSort(sort);
        if (enable != null) coin.setEnable(enable);
        if (visible != null) coin.setVisible(visible);
        if (exchangeable != null) coin.setExchangeable(exchangeable);
        if (enableOpenSell != null) coin.setEnableOpenSell(enableOpenSell == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if (enableOpenBuy != null) coin.setEnableOpenBuy(enableOpenBuy == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if (enableMarketSell != null) coin.setEnableMarketSell(enableMarketSell == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if (enableMarketBuy != null) coin.setEnableMarketBuy(enableMarketBuy == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if (enableTriggerEntrust != null) coin.setEnableTriggerEntrust(enableTriggerEntrust == 1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
        if (spreadType != null) coin.setSpreadType(spreadType);
        if (spread != null) coin.setSpread(spread);
        if (leverageType != null) coin.setLeverageType(leverageType);
        if (leverage != null) coin.setLeverage(leverage);
        if (minShare != null) coin.setMinShare(minShare);
        if (maxShare != null) coin.setMaxShare(maxShare);
        if (intervalHour != null) coin.setIntervalHour(intervalHour);
        if (feePercent != null) coin.setFeePercent(feePercent);
        if (maintenanceMarginRate != null) coin.setMaintenanceMarginRate(maintenanceMarginRate);
        if (openFee != null) coin.setOpenFee(openFee);
        if (closeFee != null) coin.setCloseFee(closeFee);
        if (takerFee != null) coin.setTakerFee(takerFee);
        if (makerFee != null) coin.setMakerFee(makerFee);

        contractCoinCoinService.save(coin);
        return success(messageSource.getMessage("SAVE_SUCCESS"));
    }

    /**
     * Batch create wallets for users (for this contract pair)
     * @param contractId contract id
     * @return result
     */
    @RequiresPermissions("coinswap-coin:init-wallet")
    @PostMapping("init-wallet")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract pair - add wallets")
    public MessageResult generateWallet(@RequestParam("contractId") Long contractId) {
        ContractCoinCoin coin = contractCoinCoinService.findOne(contractId);
        if (coin == null) {
            return MessageResult.error(messageSource.getMessage("CONTRACT_CURRENCY_CONFIGURATION_NOT_FOUND"));
        }
        memberContractWalletFeign.initWallet(contractId);
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }

    /**
     * Targeted liquidation
     * @param contractId contract id
     * @param price      price
     * @return result
     */
    @RequiresPermissions("coinswap-coin:blast")
    @PostMapping("blast")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract - targeted liquidation")
    public MessageResult blast(@RequestParam("contractId") Long contractId, @RequestParam("price") BigDecimal price) {
        ContractCoinCoin coin = contractCoinCoinService.findOne(contractId);
        if (coin == null) {
            return MessageResult.error(messageSource.getMessage("CONTRACT_CURRENCY_CONFIGURATION_NOT_FOUND"));
        }

        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }

    /**
     * Poke once
     * @param contractId contract id
     * @param price      price
     * @return result
     */
    @RequiresPermissions("coinswap-coin:poke")
    @PostMapping("poke")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Coin-margined contract - poke")
    public MessageResult poke(@RequestParam("contractId") Long contractId, @RequestParam("price") BigDecimal price) {
        ContractCoinCoin coin = contractCoinCoinService.findOne(contractId);
        if (coin == null) {
            return MessageResult.error(messageSource.getMessage("CONTRACT_CURRENCY_CONFIGURATION_NOT_FOUND"));
        }
        JSONObject msg = new JSONObject();
        msg.put("price", price);
        msg.put("symbol", coin.getSymbol());
        rocketMQTemplate.convertAndSend("admin-save-coin-swap-poke", JSON.toJSONString(msg));
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }
}
