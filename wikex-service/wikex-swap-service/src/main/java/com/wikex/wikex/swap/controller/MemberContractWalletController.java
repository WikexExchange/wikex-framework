package com.wikex.wikex.swap.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.market.feign.ExchangeRateFeign;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.MemberContractPosition;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.MemberContractPositionService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.entity.WalletTransRecord;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.feign.WalletTransRecordFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Api(tags = "Member Perpetual Contract Wallet")
@Slf4j
@RestController
@RequestMapping("/wallet")
public class MemberContractWalletController {
    @Autowired
    private MemberContractWalletService memberContractWalletService;

    @Autowired
    private MemberWalletFeign memberWalletFeign;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private ExchangeRateFeign exchangeRateFeign;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractCoinService contractCoinService;

    @Autowired
    private MemberContractPositionService memberContractPositionService;

    @Autowired
    private WalletTransRecordFeign walletTransRecordFeign;

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory; // Contract engine factory

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private MemberTransactionFeign memberTransactionService;

    /**
     * Query all contract accounts
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Query all contract accounts")
    @PermissionOperation
    @RequestMapping("list")
    public MessageResult getWalletList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if(member == null) {
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        List<MemberContractWallet> list = memberContractWalletService.findAllByMemberId(member.getId());
        List<ContractCoin> coins =  contractCoinService.list();
        ContractCoin coin = coins.get(0);
        // Add completion logic
        if(list == null || list.size()==0) {
            // Take the first coin
            MemberContractWallet wallet = createMemberContractWallet(member, coin);
            memberContractWalletService.save(wallet);
            list = memberContractWalletService.findAllByMemberId(member.getId());
        }

        BigDecimal rate = BigDecimal.valueOf(6.98);
        BigDecimal rateByFeign = exchangeRateFeign.getUsdCnyRate4Feign("cny");
        if(rateByFeign!=null && rateByFeign.compareTo(BigDecimal.ZERO)==1){
            rate = rateByFeign;
        }
        coin.setUsdtRate(rate);
        // Floating profit and loss calculation for all
        BigDecimal usdtTotalProfitAndLoss = memberContractWalletService.usdtTotalProfitAndLoss(member.getId(), coins);

        MemberContractWallet wallet = list.get(0);
        // Calculate USDT-based equity (long + short)
        wallet.setUsdtTotalProfitAndLoss(usdtTotalProfitAndLoss);
        wallet.setContractCoin(coin);
        MessageResult result = MessageResult.success("success");
        result.setData(list);
        return result;
    }

    private MemberContractWallet createMemberContractWallet(Member member, ContractCoin coin) {
        MemberContractWallet wallet = new MemberContractWallet();
        wallet.setUsdtPattern(ContractOrderPattern.CROSSED);
        wallet.setUsdtBuyPosition(BigDecimal.ZERO);
        wallet.setUsdtTotalProfitAndLoss(BigDecimal.ZERO);
        wallet.setCoinTotalProfitAndLoss(BigDecimal.ZERO);
        wallet.setMemberId(member.getId());
        wallet.setUsdtBalance(BigDecimal.ZERO);
        wallet.setUsdtBuyLeverage(BigDecimal.TEN);
        wallet.setUsdtBuyPrice(BigDecimal.ZERO);
        wallet.setUsdtBuyPrincipalAmount(BigDecimal.ZERO);
        wallet.setUsdtFrozenBalance(BigDecimal.ZERO);
        wallet.setUsdtFrozenBuyPosition(BigDecimal.ZERO);
        wallet.setUsdtFrozenSellPosition(BigDecimal.ZERO);
        wallet.setUsdtSellLeverage(BigDecimal.TEN);
        wallet.setUsdtSellPosition(BigDecimal.ZERO);
        wallet.setUsdtSellPrice(BigDecimal.ZERO);
        wallet.setUsdtSellPrincipalAmount(BigDecimal.ZERO);
        wallet.setUsdtShareNumber(coin.getShareNumber());
        return wallet;
    }

    /**
     * Get user's wallet information for the specified contract coin
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Get user's wallet information for the specified contract coin")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract Trading Pair")
    })
    @PermissionOperation
    @RequestMapping("detail")
    public MessageResult getContractWallet(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, Long contractCoinId) {
        ContractCoin coin = contractCoinService.getById(contractCoinId);
        if(coin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if(member == null) {
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        MemberContractWallet wallet = memberContractWalletService.findByMemberId(member.getId());
        if(wallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }
        BigDecimal rate = BigDecimal.valueOf(6.98);
        List<ContractCoin> coins =  contractCoinService.list();
        // Calculate USDT-based equity (long + short)
        Map<String,BigDecimal> plMap = memberContractWalletService.getTotalProfitAndLossAndPrincipalAmount(member.getId(), coins,false,null);
        BigDecimal profitAndLoss = plMap.get("profitAndLoss");
        BigDecimal principalAmount = plMap.get("principalAmount");
        wallet.setUsdtTotalProfitAndLoss(profitAndLoss);
        BigDecimal qy = wallet.getUsdtBalance().add(wallet.getUsdtFrozenBalance()).add(principalAmount).add(profitAndLoss);
        wallet.setUserQY(qy);
        coin.setUsdtRate(rate);
        wallet.setContractCoin(coin);
        // Calculate available balance
        if(wallet.getUsdtPattern().equals(ContractOrderPattern.CROSSED)){
            // Cross mode
            wallet.setCanUse(wallet.getUsdtBalance().add(profitAndLoss));
        }else {
            wallet.setCanUse(wallet.getUsdtBalance());
        }
        String riskRate = "--";
        if(wallet.getUsdtPattern().equals(ContractOrderPattern.CROSSED) && principalAmount.compareTo(BigDecimal.ZERO)!=0){
            riskRate = qy.divide(principalAmount,8,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)).setScale(2,BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString()+"%";
        }
        wallet.setRiskRate(riskRate);
        MessageResult result = MessageResult.success("success");
        result.setData(wallet);
        return result;
    }

    /**
     * Fund transfer
     * @param unit
     * @param from
     * @param to
     * @param amount
     * @return
     */
    @ApiOperation(value = "Fund Transfer")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "Transfer Coin Unit"),
            @ApiImplicitParam(name = "from", value = "Wallet type to transfer from"),
            @ApiImplicitParam(name = "to", value = "Wallet type to transfer to"),
            @ApiImplicitParam(name = "fromWalletId", value = "From Wallet ID"),
            @ApiImplicitParam(name = "toWalletId", value = "To Wallet ID"),
            @ApiImplicitParam(name = "amount", value = "Transfer Amount")
    })
    @PermissionOperation
    @RequestMapping("trans")
    public MessageResult transWallet(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                     @RequestParam(value = "unit") String unit,// Transfer coin unit
                                     @RequestParam(value = "from") WalletType from,// Wallet type to transfer from
                                     @RequestParam(value = "to") WalletType to,// Wallet type to transfer to
                                     @RequestParam(value = "fromWalletId", required = false) Long fromWalletId,// From Wallet ID
                                     @RequestParam(value = "toWalletId", required = false) Long toWalletId,// To Wallet ID
                                     @RequestParam(value = "amount") BigDecimal amount// Transfer Amount
    ) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if(member == null) {
            return MessageResult.error(msService.getMessage("REQUEST_OF_ILLEGAL"));
        }
        if(fromWalletId == toWalletId) {
            return MessageResult.error(msService.getMessage("WALLET_NOT_SELECT"));
        }
        if(from != WalletType.SPOT && from != WalletType.SWAP) {
            return MessageResult.error(msService.getMessage("WALLET_NOT_OUT"));
        }
        if(to != WalletType.SPOT && to != WalletType.SWAP) {
            return MessageResult.error(msService.getMessage("WALLET_NOT_IN"));
        }
        if(from == WalletType.SPOT && to == WalletType.SWAP) {// From spot wallet to contract wallet
            
            MemberWallet walletFrom = memberWalletFeign.findByCoinUnitAndMemberId(unit, member.getId());
            MemberContractWallet walletTo = memberContractWalletService.getById(toWalletId);

            if(walletFrom == null || walletTo == null) {
                return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
            }
            if(walletFrom.getBalance().compareTo(amount) < 0) {
                return MessageResult.error(msService.getMessage("WALLET_NOT_AMOUNT_OUT"));
            }
            if(unit.equals("USDT")) {
                memberWalletFeign.deductBalance(walletFrom.getId(), amount);
                memberContractWalletService.increaseUsdtBalance(walletTo.getId(), amount);
                
                // Record transaction
                MemberTransaction transaction = new MemberTransaction();
                transaction.setAmount(amount);
                transaction.setSymbol(unit);
                transaction.setMemberId(member.getId());
                transaction.setType(TransactionType.TRANSFER_OUT.getCode());
                transaction.setFee(BigDecimal.ZERO);
                transaction.setDiscountFee("0");
                transaction.setRealFee("0");
                transaction.setCreateTime(new Date());
                MessageResult save = memberTransactionService.save(transaction);
                
                MemberTransaction transaction1 = new MemberTransaction();
                transaction1.setAmount(amount);
                transaction1.setSymbol(unit);
                transaction1.setMemberId(member.getId());
                transaction1.setType(TransactionType.TRANSFER_IN_USDT.getCode());
                transaction1.setFee(BigDecimal.ZERO);
                transaction1.setDiscountFee("0");
                transaction1.setRealFee("0");
                transaction1.setCreateTime(new Date());
                MessageResult save1 = memberTransactionService.save(transaction1);
                
            }else{
                // TODO
                
                // Transfer coin-margined, not yet developed
            }
//            ContractCoin contractCoin = contractCoinService.getById(walletTo.getContractId());
            // Notify wallet change
            JSONObject jsonObj = new JSONObject();
//            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", walletTo.getId());
            rocketMQTemplate.convertAndSend("member-wallet-change", JSON.toJSONString(jsonObj));

        }else if(from == WalletType.SWAP && to == WalletType.SPOT) {// From contract wallet to spot wallet
            
            MemberContractWallet walletFrom = memberContractWalletService.getById(fromWalletId);
            MemberWallet walletTo =  memberWalletFeign.findByCoinUnitAndMemberId(unit, member.getId());
            if(walletFrom == null || walletTo == null) {
                return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
            }
            if(unit.equals("USDT")) {
                if (walletFrom.getUsdtBalance().compareTo(amount) < 0) {
                    return MessageResult.error(msService.getMessage("WALLET_NOT_AMOUNT_OUT"));
                }
                memberContractWalletService.decreaseUsdtBalance(walletFrom.getId(), amount);
                memberWalletFeign.increaseBalance(walletTo.getId(), amount);

                // Record transaction
                MemberTransaction transaction = new MemberTransaction();
                transaction.setAmount(amount);
                transaction.setSymbol(unit);
                transaction.setMemberId(member.getId());
                transaction.setType(TransactionType.TRANSFER_IN.getCode());
                transaction.setFee(BigDecimal.ZERO);
                transaction.setDiscountFee("0");
                transaction.setRealFee("0");
                transaction.setCreateTime(new Date());
                memberTransactionService.save(transaction);

                MemberTransaction transaction1 = new MemberTransaction();
                transaction1.setAmount(amount);
                transaction1.setSymbol(unit);
                transaction1.setMemberId(member.getId());
                transaction1.setType(TransactionType.TRANSFER_OUT_USDT.getCode());
                transaction1.setFee(BigDecimal.ZERO);
                transaction1.setDiscountFee("0");
                transaction1.setRealFee("0");
                transaction1.setCreateTime(new Date());
                memberTransactionService.save(transaction1);

            }
//            ContractCoin contractCoin = contractCoinService.getById(walletFrom.getContractId());
            // Notify wallet change
            JSONObject jsonObj = new JSONObject();
//            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", walletFrom.getId());
            rocketMQTemplate.convertAndSend("member-wallet-change", JSON.toJSONString(jsonObj));

        }else{
            return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
        }
        // Add transfer record
        WalletTransRecord record = new WalletTransRecord();
        record.setAmount(amount);
        record.setUnit(unit);
        record.setSource(from);
        record.setTarget(to);
        record.setMemberId(member.getId());
        walletTransRecordFeign.save(record);
        return MessageResult.success();
    }

    @ApiOperation(value = "Get member perpetual contract wallet by trading pair symbol")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
    })
    @PermissionOperation
    @RequestMapping("all")
    public MessageResult getContractWallet(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String symbol) {
        List<MemberContractWallet> list = contractCoinMatchFactory.getContractCoinMatch(symbol).getMemberContractWalletList();
        MessageResult result = MessageResult.success("success");
        result.setData(list);
        return result;
    }

}
