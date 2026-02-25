package com.wikex.wikex.second.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.constant.WalletType;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.feign.*;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.wikex.wikex.constant.SysConstant.SESSION_MEMBER;


@Api(tags = "Member Second-Contract Wallet")
@Slf4j
@RestController
@RequestMapping("/wallet")
public class MemberSecondWalletController {
    @Autowired
    private MemberSecondWalletFeign memberSecondWalletService;
    @Autowired
    private MemberWalletFeign walletService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberFeign memberService;
    @Autowired
    private CoinFeign coinService;
    @Autowired
    private WalletTransRecordFeign walletTransRecordService;
    @Autowired
    private MemberTransactionFeign memberTransactionService;

    /**
     * Query all second-contract accounts
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Query all second-contract accounts")
    @PermissionOperation
    @RequestMapping("list")
    public MessageResult getWalletList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.findMemberById(user.getId());
        if(member == null) {
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        List<MemberSecondWallet> list = memberSecondWalletService.findAllByMemberId(user.getId());
        List<String> coins = new ArrayList<>();
        coins.add("USDT");
        coins.add("BTC");
        coins.add("ETH");
        coins.add("TRX");
        if(list != null && list.size()>0) {
            for(MemberSecondWallet secondWallet:list){
                coins.remove(secondWallet.getCoinId());
            }
        }
        // completion
        if(coins.size()>0){
            for(String unit : coins){
                MemberSecondWallet wallet = new MemberSecondWallet();
                wallet.setFrozenBalance(BigDecimal.ZERO);
                wallet.setCoinId(unit);
                wallet.setBalance(BigDecimal.ZERO);
                wallet.setMemberId(user.getId());
                list.add(wallet);
            }
        }
        MessageResult result = MessageResult.success("success");
        result.setData(list);
        return result;
    }

    /**
     * Increase balance
     * @param unit
     * @param from
     * @param to
     * @param amount
     * @return
     */
    @ApiOperation(value = "Increase Balance")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "Transfer Coin Unit"),
            @ApiImplicitParam(name = "from", value = "Wallet type to transfer from"),
            @ApiImplicitParam(name = "to", value = "Wallet type to transfer to"),
            @ApiImplicitParam(name = "amount", value = "Transfer Amount"),
    })
    @PermissionOperation
    @RequestMapping("trans")
    public MessageResult transWallet(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                     @RequestParam(value = "unit") String unit,// Transfer coin unit
                                     @RequestParam(value = "from") WalletType from,// Wallet type to transfer from
                                     @RequestParam(value = "to") WalletType to,// Wallet type to transfer to
                                     @RequestParam(value = "amount") BigDecimal amount// Transfer amount
                                     ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member1 = memberService.findMemberById(user.getId());
        if(member1 == null) {
            return MessageResult.error(msService.getMessage("REQUEST_OF_ILLEGAL"));
        }
        if(from != WalletType.SPOT && from != WalletType.SECOND) {
            return MessageResult.error(msService.getMessage("WALLET_NOT_OUT"));
        }
        if(to != WalletType.SPOT && to != WalletType.SECOND) {
            return MessageResult.error(msService.getMessage("WALLET_NOT_IN"));
        }

        if(from == WalletType.SPOT && to == WalletType.SECOND) {// From spot wallet to options wallet
            MemberWallet walletFrom = walletService.findByCoinUnitAndMemberId(unit, user.getId());
            MemberSecondWallet walletTo = memberSecondWalletService.findByCoinUnitAndMemberId(unit,user.getId());

            if(walletFrom == null || walletTo == null) {
                return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
            }
            if(walletFrom.getBalance().compareTo(amount) < 0) {
                return MessageResult.error(msService.getMessage("WALLET_NOT_AMOUNT_OUT"));
            }
            walletService.deductBalance(walletFrom.getId(), amount);
            memberSecondWalletService.increaseBalance(walletTo.getId(), amount);
            // Record transaction
            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(amount);
            transaction.setSymbol(unit);
            transaction.setMemberId(user.getId());
            transaction.setType(TransactionType.TRANSFER_OUT.getCode());
            transaction.setFee(BigDecimal.ZERO);
            transaction.setDiscountFee("0");
            transaction.setRealFee("0");
            transaction.setCreateTime(new Date());

            memberTransactionService.save(transaction);

            MemberTransaction transaction1 = new MemberTransaction();
            transaction1.setAmount(amount);
            transaction1.setSymbol(unit);
            transaction1.setMemberId(user.getId());
            transaction1.setType(TransactionType.TRANSFER_IN_SECOND.getCode());
            transaction1.setFee(BigDecimal.ZERO);
            transaction1.setDiscountFee("0");
            transaction1.setRealFee("0");
            transaction1.setCreateTime(new Date());
            memberTransactionService.save(transaction1);

        }else if(from == WalletType.SECOND && to == WalletType.SPOT) {// From options wallet to spot wallet
            MemberSecondWallet walletFrom = memberSecondWalletService.findByCoinUnitAndMemberId(unit,user.getId());
            MemberWallet walletTo = walletService.findByCoinUnitAndMemberId(unit, user.getId());
            if(walletFrom == null || walletTo == null) {
                return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
            }
//            if(unit.equals("USDT")) {
            if (walletFrom.getBalance().compareTo(amount) < 0) {
                return MessageResult.error(msService.getMessage("WALLET_NOT_AMOUNT_OUT"));
            }
            memberSecondWalletService.decreaseBalance(walletFrom.getId(), amount);
            walletService.increaseBalance(walletTo.getId(), amount);

            // Record transaction
            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(amount);
            transaction.setSymbol(unit);
            transaction.setMemberId(user.getId());
            transaction.setType(TransactionType.TRANSFER_IN.getCode());
            transaction.setFee(BigDecimal.ZERO);
            transaction.setDiscountFee("0");
            transaction.setRealFee("0");
            transaction.setCreateTime(new Date());

            memberTransactionService.save(transaction);

            MemberTransaction transaction1 = new MemberTransaction();
            transaction1.setAmount(amount);
            transaction1.setSymbol(unit);
            transaction1.setMemberId(user.getId());
            transaction1.setType(TransactionType.TRANSFER_OUT_SECOND.getCode());
            transaction1.setFee(BigDecimal.ZERO);
            transaction1.setDiscountFee("0");
            transaction1.setRealFee("0");
            transaction1.setCreateTime(new Date());
            memberTransactionService.save(transaction1);
//            }
//            // Notify wallet change
//            JSONObject jsonObj = new JSONObject();
//            jsonObj.put("symbol", walletFrom.getContractCoin().getSymbol());
//            jsonObj.put("walletId", walletFrom.getId());
//            kafkaTemplate.send("member-wallet-change", JSON.toJSONString(jsonObj));

        }else{
            return MessageResult.error(msService.getMessage("NO_IN_OUT_WALLET"));
        }
        // Add transfer record
        WalletTransRecord record = new WalletTransRecord();
        record.setAmount(amount);
        record.setUnit(unit);
        record.setSource(from);
        record.setTarget(to);
        record.setMemberId(user.getId());
        walletTransRecordService.save(record);
        return MessageResult.success();
    }

    /**
     * Query options wallet balance
     * @param authMember
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Query options wallet balance")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
    })
    @PermissionOperation
    @RequestMapping("balance/{symbol}")
    public MessageResult findWalletBySymbol(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, @PathVariable String symbol) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        MessageResult mr = MessageResult.success("success");
        MemberSecondWallet wallet = memberSecondWalletService.findByCoinUnitAndMemberId(symbol, member.getId());
        if(wallet!=null){
            mr.setData(wallet.getBalance());
        }else {
            mr.setData(BigDecimal.ZERO);
        }
        return mr;
    }

}
