package com.wikex.wikex.user.deposit.service;

import com.wikex.wikex.user.deposit.dto.DepositPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.wikex.wikex.user.entity.Addressext;
import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.entity.MemberFundingWallet;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.service.AddressextService;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.service.EmailService;
import com.wikex.wikex.user.service.MemberDepositService;
import com.wikex.wikex.user.service.MemberFundingWalletService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.service.RechargeService;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.TransactionType;

@Slf4j
@Service
public class DepositService {

    @Autowired
    private AddressextService addressextService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MemberDepositService memberDepositService;

    @Autowired
    private MemberFundingWalletService memberFundingWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private MemberService memberService;

    public void handleDepositDetected(DepositPayload payload) {
        log.info("[DEPOSIT.DETECTED] TxHash={}, Amount={} {}, Address={}, Status={}",
                payload.getTxHash(), payload.getAmount(), payload.getAssetSymbol(),
                payload.getAddress(), payload.getStatus());
        try {
            Addressext addressExt = addressextService.findByAddress(payload.getAddress());
            if (addressExt == null) {
                log.warn("Cannot find user for deposit address: {}", payload.getAddress());
                return;
            }
            Long userId = addressExt.getMemberId();
            String lang = "en_US";
            try {
                MemberDeposit existing = memberDepositService.findDeposit(payload.getAddress(), payload.getTxHash(),
                        payload.getLogIndex());
                BigDecimal amount = payload.getAmount() != null ? payload.getAmount()
                        : BigDecimal.ZERO;

                if (existing == null) {
                    MemberDeposit deposit = new MemberDeposit();
                    deposit.setAddress(payload.getAddress());
                    deposit.setAmount(amount);
                    deposit.setMemberId(userId);
                    deposit.setTxid(payload.getTxHash());
                    deposit.setAssetSymbol(payload.getAssetSymbol());
                    deposit.setCreateTime(LocalDateTime.now());
                    deposit.setStatus(0);
                    deposit.setConfirmations(payload.getConfirmations() != null ? payload.getConfirmations() : 0);
                    deposit.setAssetContract(payload.getAssetContract());
                    deposit.setBlockchain(payload.getBlockchain());
                    deposit.setChainKey(payload.getChainKey());
                    deposit.setAmountRaw(payload.getAmountRaw());
                    deposit.setDecimals(payload.getDecimals() != null ? payload.getDecimals() : null);
                    deposit.setLogIndex(payload.getLogIndex());
                    deposit.setBlockNumber(payload.getBlockNumber());
                    Object metaObj = payload.getMeta();
                    if (metaObj instanceof Map) {
                        Object fromObj = ((Map) metaObj).get("from");

                        if (fromObj != null)
                            deposit.setFromAddress(String.valueOf(fromObj));
                    }

                    memberDepositService.save(deposit);
                }
                // Send email to user
                Member member = memberService.getById(userId);
                if (member != null && member.getEmail() != null && !member.getEmail().isEmpty()) {
                    emailService.sentEmailDepositCreated(member.getEmail(), lang);
                }
            } catch (Exception e) {
                log.error("Error saving deposit.detected: {}", payload.getTxHash(), e);
            }
        } catch (Exception e) {
            log.error("Error handling deposit.detected: {}", payload.getTxHash(), e);
        }
    }

    public void handleDepositConfirmed(DepositPayload payload) {
        try {
            Addressext addressExt = addressextService.findByAddress(payload.getAddress());
            if (addressExt == null) {
                log.warn("Cannot find user for deposit address: {}", payload.getAddress());
                return;
            }
            MemberDeposit existing = memberDepositService.findDeposit(payload.getAddress(), payload.getTxHash(),
                    payload.getLogIndex());
            if (existing == null) {
                log.warn("Deposit record not found for CONFIRMED (won't create): address={}, txHash={}, logIndex={}",
                        payload.getAddress(), payload.getTxHash(), payload.getLogIndex());
                return;
            }
            // update confirmations and status
            existing.setConfirmations(payload.getConfirmations());
            existing.setStatus(1);
            memberDepositService.saveOrUpdate(existing);

        } catch (Exception e) {
            log.error("Error handling deposit.confirmed: {}", payload.getTxHash(), e);
        }
    }

    @Transactional
    public void handleDepositCredited(DepositPayload payload) {
        log.info("[DEPOSIT.CREDITED] TxHash={}, Address={}, LogIndex={}",
                payload.getTxHash(), payload.getAddress(), payload.getLogIndex());
        try {
            Addressext addr = addressextService.findByAddress(payload.getAddress());
            if (addr == null) {
                log.warn("Cannot find user for deposit address: {}", payload.getAddress());
                return;
            }
            MemberDeposit deposit = memberDepositService.findDepositForUpdate(
                    payload.getAddress(),
                    payload.getTxHash(),
                    payload.getLogIndex());

            if (deposit == null || deposit.getStatus() >= 2) {
                log.warn(
                        "Deposit record not found or already credited for CREDITED: address={}, txHash={}, logIndex={}",
                        payload.getAddress(), payload.getTxHash(), payload.getLogIndex());
                return;
            }
            Long userId = addr.getMemberId();

            BigDecimal amount = payload.getAmount() != null ? payload.getAmount() : BigDecimal.ZERO;
            deposit.setStatus(2);
            deposit.setConfirmations(payload.getConfirmations() != null ? payload.getConfirmations() : 0);
            memberDepositService.updateById(deposit);

            // check coin existence
            Coin coin = coinService.findByName(payload.getAssetSymbol());

            if (coin == null) {
                Coin newCoin = new Coin();
                // newCoin.setId();
                newCoin.setName(payload.getAssetSymbol());
                newCoin.setUnit(payload.getAssetSymbol());
                newCoin.setIconUrl(
                        "https://wikex-exchange.sgp1.digitaloceanspaces.com/18d9a42f-d353-4093-bed1-aa417a8cf321.png");
                newCoin.setHasLegal(false);
                newCoin.setMaxTxFee(BigDecimal.ZERO);
                newCoin.setMinTxFee(BigDecimal.ZERO);
                newCoin.setSort(0);
                newCoin.setIsApproved(BooleanEnum.IS_FALSE);
                newCoin.setCanAutoWithdraw(BooleanEnum.IS_FALSE.getCode());
                newCoin.setCanRecharge(BooleanEnum.IS_TRUE.getCode());
                newCoin.setCanWithdraw(BooleanEnum.IS_FALSE.getCode());
                newCoin.setCanTransfer(BooleanEnum.IS_FALSE.getCode());
                newCoin.setCnyRate(BigDecimal.ZERO);
                newCoin.setIsPlatformCoin(0);
                newCoin.setMaxWithdrawAmount(BigDecimal.ZERO);
                newCoin.setStatus(CommonStatus.NORMAL);
                newCoin.setWithdrawScale(4);
                newCoin.setMinRechargeAmount(BigDecimal.ZERO);
                newCoin.setMinWithdrawAmount(BigDecimal.ZERO);
                newCoin.setUsdRate(BigDecimal.ZERO);
                coinService.save(newCoin);
                coin = newCoin;
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Invalid deposit amount for user {}: {}", userId, amount);
                return;
            }
            MemberFundingWallet wallet = memberFundingWalletService.findByCoinUnitAndMemberId(
                    payload.getAssetSymbol(), userId);

            if (wallet == null) {
                wallet = new MemberFundingWallet();
                wallet.setMemberId(userId);
                wallet.setCoinId(coin.getId().toString());
                wallet.setBalance(amount);
                wallet.setFrozenBalance(BigDecimal.ZERO);
                memberFundingWalletService.save(wallet);
            } else {
                memberFundingWalletService.increaseBalance(wallet.getId(), amount);
            }

            MemberTransaction tx = new MemberTransaction();
            tx.setAmount(amount);
            tx.setSymbol(payload.getAssetSymbol());
            tx.setAddress(payload.getAddress());
            tx.setMemberId(userId);
            tx.setType(TransactionType.RECHARGE.getCode());
            tx.setFee(BigDecimal.ZERO);
            tx.setDiscountFee("0");
            tx.setRealFee("0");
            tx.setCreateTime(DateUtil.getCurrentDate());
            memberTransactionService.save(tx);

        } catch (Exception e) {
            log.error("Error handling deposit.credited: {}", payload.getTxHash(), e);
        }
    }
}
