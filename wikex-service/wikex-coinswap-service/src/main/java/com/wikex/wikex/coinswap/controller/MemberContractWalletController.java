package com.wikex.wikex.coinswap.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.coinswap.engine.ContractCoinMatch;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.constant.WalletType;
import com.wikex.wikex.market.feign.ExchangeRateFeign;
import com.wikex.wikex.screen.MemberContractWalletCoinScreen;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(tags = "Member Perpetual Contract Wallet")
@Slf4j
@RestController
@RequestMapping("/wallet")
public class MemberContractWalletController {
    @Autowired
    private MemberContractWalletCoinService memberContractWalletService;

    @Autowired
    private MemberWalletFeign memberWalletFeign;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private ExchangeRateFeign exchangeRateFeign;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractCoinCoinService contractCoinService;

    @Autowired
    private WalletTransRecordFeign walletTransRecordFeign;

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;

    @Resource
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

        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) {
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        List<MemberContractWalletCoin> list = memberContractWalletService.findAllByMemberId(member.getId());
        List<ContractCoinCoin> coins = contractCoinService.list();

        if (list == null || list.size() == 0) {
            for (ContractCoinCoin coin : coins) {
                MemberContractWalletCoin wallet = createMemberContractWallet(member, coin);
                memberContractWalletService.save(wallet);
            }
            list = memberContractWalletService.findAllByMemberId(member.getId());
        } else if (coins.size() > list.size()) {
            for (ContractCoinCoin coin : coins) {
                boolean ishas = false;
                for (MemberContractWalletCoin mw : list) {
                    if (mw.getContractId().equals(coin.getId())) {
                        ishas = true;
                    }
                }
                if (!ishas) {
                    MemberContractWalletCoin wallet = createMemberContractWallet(member, coin);
                    memberContractWalletService.save(wallet);
                }
            }
            list = memberContractWalletService.findAllByMemberId(member.getId());
        }

        BigDecimal rate = BigDecimal.valueOf(6.98);
        BigDecimal rateByFeign = exchangeRateFeign.getUsdCnyRate4Feign("cny");
        if (rateByFeign != null && rateByFeign.compareTo(BigDecimal.ZERO) == 1) {
            rate = rateByFeign;
        }

        List<MemberContractWalletCoin> list1 = new ArrayList<>();

        for (MemberContractWalletCoin wallet : list) {
            for (ContractCoinCoin coin : coins) {
                if (wallet.getContractId().equals(coin.getId())) {
                    wallet.setContractCoin(coin);
                }
            }
            if (wallet.getContractCoin().getEnable() != 1) {
                continue;
            }
            BigDecimal currentPrice = contractCoinMatchFactory
                    .getContractCoinMatch(wallet.getContractCoin().getSymbol()).getNowPrice();

            BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;

            if (wallet.getCoinBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
                usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(currentPrice
                        .divide(wallet.getCoinBuyPrice(), 8, BigDecimal.ROUND_HALF_DOWN).subtract(BigDecimal.ONE)
                        .multiply(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()))
                        .multiply(wallet.getCoinShareNumber()));
            }

            if (wallet.getCoinSellPrice().compareTo(BigDecimal.ZERO) > 0) {
                usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(BigDecimal.ONE
                        .subtract(currentPrice.divide(wallet.getCoinSellPrice(), 8, BigDecimal.ROUND_HALF_DOWN))
                        .multiply(wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()))
                        .multiply(wallet.getCoinShareNumber()));
            }

            wallet.setCoinTotalProfitAndLoss(usdtTotalProfitAndLoss);
            wallet.getContractCoin().setCurrentPrice(currentPrice);
            wallet.getContractCoin().setUsdtRate(rate);
            wallet.getContractCoin()
                    .setName(wallet.getContractCoin().getCoinSymbol() + " " + msService.getMessage("SWAP_NAME"));
            list1.add(wallet);
        }

        MessageResult result = MessageResult.success("success");
        result.setData(list1);
        return result;
    }

    private MemberContractWalletCoin createMemberContractWallet(Member member, ContractCoinCoin coin) {
        MemberContractWalletCoin wallet = new MemberContractWalletCoin();
        wallet.setCoinPattern(ContractOrderPattern.FIXED);
        wallet.setCoinBuyPosition(BigDecimal.ZERO);
        wallet.setCoinTotalProfitAndLoss(BigDecimal.ZERO);
        wallet.setCoinBalance(BigDecimal.ZERO);
        wallet.setCoinBuyLeverage(BigDecimal.TEN);
        wallet.setCoinBuyPosition(BigDecimal.ZERO);
        wallet.setCoinBuyPrice(BigDecimal.ZERO);
        wallet.setCoinBuyPrincipalAmount(BigDecimal.ZERO);
        wallet.setCoinFrozenBalance(BigDecimal.ZERO);
        wallet.setCoinFrozenBuyPosition(BigDecimal.ZERO);
        wallet.setCoinFrozenSellPosition(BigDecimal.ZERO);
        wallet.setCoinPattern(ContractOrderPattern.FIXED);
        wallet.setCoinSellLeverage(BigDecimal.TEN);
        wallet.setCoinSellPosition(BigDecimal.ZERO);
        wallet.setCoinSellPrice(BigDecimal.ZERO);
        wallet.setCoinSellPrincipalAmount(BigDecimal.ZERO);
        wallet.setCoinShareNumber(coin.getShareNumber());
        wallet.setCoinTotalProfitAndLoss(BigDecimal.ZERO);
        wallet.setContractCoin(coin);
        wallet.setContractId(coin.getId());
        wallet.setMemberId(member.getId());
        wallet.setCoinBalance(BigDecimal.ZERO);
        wallet.setCoinBuyLeverage(BigDecimal.TEN);
        wallet.setCoinBuyPrice(BigDecimal.ZERO);
        wallet.setCoinBuyPrincipalAmount(BigDecimal.ZERO);
        wallet.setCoinFrozenBalance(BigDecimal.ZERO);
        wallet.setCoinFrozenBuyPosition(BigDecimal.ZERO);
        wallet.setCoinFrozenSellPosition(BigDecimal.ZERO);
        wallet.setCoinSellLeverage(BigDecimal.TEN);
        wallet.setCoinSellPosition(BigDecimal.ZERO);
        wallet.setCoinSellPrice(BigDecimal.ZERO);
        wallet.setCoinSellPrincipalAmount(BigDecimal.ZERO);
        wallet.setCoinShareNumber(coin.getShareNumber());
        return wallet;
    }

    /**
     * Get user's wallet information for the specified contract coin
     * 
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Get user's wallet information for the specified contract coin")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract Trading Pair")
    })
    @PermissionOperation
    @RequestMapping("detail")
    public MessageResult getContractWallet(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId) {
        ContractCoinCoin coin = contractCoinService.getById(contractCoinId);
        if (coin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }

        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());

        if (member == null) {
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        MemberContractWalletCoin wallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(),
                coin);
        if (wallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }

        ContractCoinMatch contractCoinMatch = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
        BigDecimal currentPrice = BigDecimal.ZERO;
        if (contractCoinMatch != null) {
            currentPrice = contractCoinMatch.getNowPrice();
        }

        BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;

        if (wallet.getCoinBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
            usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(currentPrice
                    .divide(wallet.getCoinBuyPrice(), 8, BigDecimal.ROUND_HALF_DOWN).subtract(BigDecimal.ONE)
                    .multiply(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()))
                    .multiply(wallet.getCoinShareNumber()));
        }

        if (wallet.getCoinSellPrice().compareTo(BigDecimal.ZERO) > 0) {
            usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(BigDecimal.ONE
                    .subtract(currentPrice.divide(wallet.getCoinSellPrice(), 8, BigDecimal.ROUND_HALF_DOWN))
                    .multiply(wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()))
                    .multiply(wallet.getCoinShareNumber()));
        }
        BigDecimal totalBuyPosition = wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition());
        BigDecimal totalSellPosition = wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition());

        if (wallet.getCoinPattern() == ContractOrderPattern.FIXED) {
            if (totalBuyPosition.compareTo(BigDecimal.ZERO) == 1) {
                BigDecimal valueUsdt = totalBuyPosition.multiply(wallet.getCoinShareNumber());
                BigDecimal num = totalBuyPosition.multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(),
                        8, BigDecimal.ROUND_HALF_DOWN);

                BigDecimal principalAmount = wallet.getCoinBuyPrincipalAmount();

                BigDecimal mm = num.multiply(coin.getMaintenanceMarginRate());
                BigDecimal forcePrice = valueUsdt.divide(num.add(principalAmount.subtract(mm)), 8,
                        BigDecimal.ROUND_HALF_DOWN);

                if (forcePrice.compareTo(BigDecimal.ZERO) == -1) {
                    forcePrice = BigDecimal.ZERO;
                }
                wallet.setBuyForcePrice(forcePrice);
            }
            if (totalSellPosition.compareTo(BigDecimal.ZERO) == 1) {

                BigDecimal valueUsdt = totalSellPosition.multiply(wallet.getCoinShareNumber());
                BigDecimal num = totalSellPosition.multiply(wallet.getCoinShareNumber())
                        .divide(wallet.getCoinSellPrice(), 8, BigDecimal.ROUND_HALF_DOWN);

                BigDecimal principalAmount = wallet.getCoinSellPrincipalAmount();

                BigDecimal mm = num.multiply(coin.getMaintenanceMarginRate());
                BigDecimal forcePrice = valueUsdt.divide(num.subtract(principalAmount.subtract(mm)), 8,
                        BigDecimal.ROUND_HALF_DOWN);

                if (forcePrice.compareTo(BigDecimal.ZERO) == -1) {
                    forcePrice = BigDecimal.ZERO;
                }
                wallet.setSellForcePrice(forcePrice);
            }
        } else {
            if (totalBuyPosition.compareTo(BigDecimal.ZERO) == 1) {

                BigDecimal valueUsdt = totalBuyPosition.multiply(wallet.getCoinShareNumber());
                BigDecimal num = totalBuyPosition.multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(),
                        8, BigDecimal.ROUND_HALF_DOWN);

                BigDecimal principalAmount = wallet.getCoinBuyPrincipalAmount().add(wallet.getCoinBalance());

                BigDecimal mm = num.multiply(coin.getMaintenanceMarginRate());
                BigDecimal forcePrice = valueUsdt.divide(num.add(principalAmount.subtract(mm)), 8,
                        BigDecimal.ROUND_HALF_DOWN);

                if (forcePrice.compareTo(BigDecimal.ZERO) == -1) {
                    forcePrice = BigDecimal.ZERO;
                }
                wallet.setBuyForcePrice(forcePrice);
            }
            if (totalSellPosition.compareTo(BigDecimal.ZERO) == 1) {

                BigDecimal valueUsdt = totalSellPosition.multiply(wallet.getCoinShareNumber());
                BigDecimal num = totalSellPosition.multiply(wallet.getCoinShareNumber())
                        .divide(wallet.getCoinSellPrice(), 8, BigDecimal.ROUND_HALF_DOWN);

                BigDecimal principalAmount = wallet.getCoinSellPrincipalAmount().add(wallet.getCoinBalance());

                BigDecimal mm = num.multiply(coin.getMaintenanceMarginRate());
                BigDecimal forcePrice = valueUsdt.divide(num.subtract(principalAmount.subtract(mm)), 8,
                        BigDecimal.ROUND_HALF_DOWN);

                if (forcePrice.compareTo(BigDecimal.ZERO) == -1) {
                    forcePrice = BigDecimal.ZERO;
                }
                wallet.setSellForcePrice(forcePrice);
            }
        }

        wallet.setCoinTotalProfitAndLoss(usdtTotalProfitAndLoss);
        wallet.setContractCoin(coin);
        wallet.getContractCoin().setCurrentPrice(currentPrice);

        MessageResult result = MessageResult.success("success");
        result.setData(wallet);
        return result;
    }

    /**
     * Fund transfer
     * 
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult transWallet(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "unit") String unit,
            @RequestParam(value = "from") WalletType from,
            @RequestParam(value = "to") WalletType to,
            @RequestParam(value = "fromWalletId", required = false) Long fromWalletId,
            @RequestParam(value = "toWalletId", required = false) Long toWalletId,
            @RequestParam(value = "amount") BigDecimal amount) {

        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member1 = memberFeign.findMemberById(user.getId());
        if (member1 == null) {
            return MessageResult.error(msService.getMessage("REQUEST_OF_ILLEGAL"));
        }
        if (fromWalletId.equals(toWalletId)) {
            return MessageResult.error(msService.getMessage("WALLET_NOT_SELECT"));
        }
        if (from != WalletType.SPOT && from != WalletType.SWAP) {
            return MessageResult.error(msService.getMessage("WALLET_NOT_OUT"));
        }
        if (to != WalletType.SPOT && to != WalletType.SWAP) {
            return MessageResult.error(msService.getMessage("WALLET_NOT_IN"));
        }

        if (from == WalletType.SPOT && to == WalletType.SWAP) {
            MemberWallet walletFrom = memberWalletFeign.findByCoinUnitAndMemberId(unit, member1.getId());
            MemberContractWalletCoin walletTo = memberContractWalletService.getById(toWalletId);

            if (walletFrom == null || walletTo == null) {
                return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
            }
            if (walletFrom.getBalance().compareTo(amount) < 0) {
                return MessageResult.error(msService.getMessage("WALLET_NOT_AMOUNT_OUT"));
            }

            memberWalletFeign.deductBalance(walletFrom.getId(), amount);
            memberContractWalletService.increaseCoinBalance(walletTo.getId(), amount);

            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(amount);
            transaction.setSymbol(unit);
            transaction.setMemberId(member1.getId());
            transaction.setType(TransactionType.TRANSFER_OUT.getCode());
            transaction.setFee(BigDecimal.ZERO);
            transaction.setDiscountFee("0");
            transaction.setRealFee("0");
            transaction.setCreateTime(new Date());
            memberTransactionService.save(transaction);

            MemberTransaction transaction1 = new MemberTransaction();
            transaction1.setAmount(amount);
            transaction1.setSymbol(unit);
            transaction1.setMemberId(member1.getId());
            transaction1.setType(TransactionType.TRANSFER_IN_COIN.getCode());
            transaction1.setFee(BigDecimal.ZERO);
            transaction1.setDiscountFee("0");
            transaction1.setRealFee("0");
            transaction1.setCreateTime(new Date());
            memberTransactionService.save(transaction1);

            ContractCoinCoin contractCoin = contractCoinService.getById(walletTo.getContractId());

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", walletTo.getId());
            rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));

        } else if (from == WalletType.SWAP && to == WalletType.SPOT) {
            MemberContractWalletCoin walletFrom = memberContractWalletService.getById(fromWalletId);
            MemberWallet walletTo = memberWalletFeign.findByCoinUnitAndMemberId(unit, member1.getId());
            if (walletFrom == null || walletTo == null) {
                return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
            }

            if (walletFrom.getCoinBalance().compareTo(amount) < 0) {
                return MessageResult.error(msService.getMessage("WALLET_NOT_AMOUNT_OUT"));
            }
            memberContractWalletService.decreaseCoinBalance(walletFrom.getId(), amount);
            memberWalletFeign.increaseBalance(walletTo.getId(), amount);

            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(amount);
            transaction.setSymbol(unit);
            transaction.setMemberId(member1.getId());
            transaction.setType(TransactionType.TRANSFER_IN.getCode());
            transaction.setFee(BigDecimal.ZERO);
            transaction.setDiscountFee("0");
            transaction.setRealFee("0");
            transaction.setCreateTime(new Date());
            memberTransactionService.save(transaction);

            MemberTransaction transaction1 = new MemberTransaction();
            transaction1.setAmount(amount);
            transaction1.setSymbol(unit);
            transaction1.setMemberId(member1.getId());
            transaction1.setType(TransactionType.TRANSFER_OUT_COIN.getCode());
            transaction1.setFee(BigDecimal.ZERO);
            transaction1.setDiscountFee("0");
            transaction1.setRealFee("0");
            transaction1.setCreateTime(new Date());
            memberTransactionService.save(transaction1);

            ContractCoinCoin contractCoin = contractCoinService.getById(walletFrom.getContractId());

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", walletFrom.getId());
            rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));

        } else if (from == WalletType.SWAP && to == WalletType.SWAP) {

        } else {
            return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
        }

        WalletTransRecord record = new WalletTransRecord();
        record.setAmount(amount);
        record.setUnit(unit);
        record.setSource(from);
        record.setTarget(to);
        record.setMemberId(member1.getId());
        walletTransRecordFeign.save(record);
        return MessageResult.success();
    }

    @PermissionOperation
    @RequestMapping("all")
    public MessageResult getContractWallet(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            String symbol) {
        List<MemberContractWalletCoin> list = contractCoinMatchFactory.getContractCoinMatch(symbol)
                .getMemberContractWalletList();
        MessageResult result = MessageResult.success("success");
        result.setData(list);
        return result;
    }
}
