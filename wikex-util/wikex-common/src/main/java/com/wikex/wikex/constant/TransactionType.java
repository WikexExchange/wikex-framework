package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum TransactionType implements Serializable {
    RECHARGE(0,"Recharge"),
    WITHDRAW(1,"Withdraw"),
    TRANSFER_ACCOUNTS(2,"Transfer"),
    EXCHANGE(3,"Spot Trading"),
    OTC_BUY(4,"Fiat Buy"),
    OTC_SELL(5,"Fiat Sell"),
    ACTIVITY_AWARD(6,"Activity Reward"),
    PROMOTION_AWARD(7,"Invite Friends Reward"),
    DIVIDEND(8,"Dividend"),
    VOTE(9,"Vote"),
    ADMIN_RECHARGE(10,"Manual Recharge"),
    MATCH(11,"Matching"),
    ACTIVITY_BUY(12,"Activity Exchange"),
    CTC_BUY(13,"CTC Buy"),
    CTC_SELL(14,"CTC Sell"),
    RED_OUT(15,"Red Packet Sent"),
    RED_IN(16,"Red Packet Received"),
    WITHDRAWCODE_OUT(17,"Withdraw via Code"),
    WITHDRAWCODE_IN(18,"Recharge via Code"),
    CONTRACT_FEE(19,"Perpetual Contract Fee"),
    CONTRACT_PROFIT(20,"Perpetual Contract Profit"),
    CONTRACT_LOSS(21,"Perpetual Contract Loss"),
    OPTION_FAIL(22,"Options Contract Failed"),
    OPTION_FEE(23,"Options Contract Fee"),
    OPTION_REWARD(24,"Options Contract Bonus"),
    CONTRACT_AWARD(25,"Contract Rebate"),
    LEVEL_AWARD(26,"Peer-level Reward"),
    PLATFORM_FEE_AWARD(27,"Platform Fee Income"),
    SECOND_FAIL(28,"Second Contract Failed"),
    SECOND_REWARD(29,"Second Contract Bonus"),
    FINANCE_REWARD(30,"Financial Interest"),
    PAY_CHARGE_FEE(31,"Funding Fee Paid"),
    GET_CHARGE_FEE(32,"Funding Fee Received"),
    AUTO_INVEST_BUY(33,"Auto-Invest Buy"),
    AUTO_INVEST_SELL(34,"Auto-Invest Sell"),
    LOCKED_SAVING_BUY(35,"Locked Savings Purchase"),
    LOCKED_SAVING_SELL(36,"Locked Savings Redemption"),
    TRANSFER_IN_COIN(37,"Coin-Margin Contract Transfer In"),
    TRANSFER_OUT_COIN(38,"Coin-Margin Contract Transfer Out"),
    TRANSFER_IN_USDT(39,"USDT-Margin Contract Transfer In"),
    TRANSFER_OUT_USDT(40,"USDT-Margin Contract Transfer Out"),
    TRANSFER_IN_SECOND(41,"Second Contract Transfer In"),
    TRANSFER_OUT_SECOND(42,"Second Contract Transfer Out"),
    TRANSFER_IN(43,"Spot Transfer In"),
    TRANSFER_OUT(44,"Spot Transfer Out"),

    CONTRACT_BLAST_PROFIT(45,"Liquidation Surplus (Platform)"),

    CONTRACT_BLAST_LOSS(46,"Liquidation Loss Compensation (Platform)"),
    COMMISION_INVITER_FEE(47,"Commision Fee Inviter"),
    CAMPAIGN_CLAIM(48,"Campaign Claim");

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    TransactionType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static TransactionType creator(Object v) {
        if(v instanceof String){
            for (TransactionType value : TransactionType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (TransactionType value : TransactionType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }

    public static TransactionType valueOfOrdinal(int ordinal){
        switch (ordinal){
            case 0:return RECHARGE;
            case 1:return WITHDRAW;
            case 2:return TRANSFER_ACCOUNTS;
            case 3:return EXCHANGE;
            case 4:return OTC_BUY;
            case 5:return OTC_SELL;
            case 6:return ACTIVITY_AWARD;
            case 7:return PROMOTION_AWARD;
            case 8:return DIVIDEND;
            case 9:return VOTE;
            case 10:return ADMIN_RECHARGE;
            case 11:return MATCH;
            case 12:return ACTIVITY_BUY;
            case 13:return CTC_BUY;
            case 14:return CTC_SELL;
            case 15:return RED_OUT;
            case 16:return RED_IN;
            case 17:return WITHDRAWCODE_OUT;
            case 18:return WITHDRAWCODE_IN;
            case 19:return CONTRACT_FEE;
            case 20:return CONTRACT_PROFIT;
            case 21:return CONTRACT_LOSS;
            case 22:return OPTION_FAIL;
            case 23:return OPTION_FEE;
            case 24:return OPTION_REWARD;
            case 25:return CONTRACT_AWARD;
            case 26:return LEVEL_AWARD;
            case 27:return PLATFORM_FEE_AWARD;
            case 28:return SECOND_FAIL;
            case 29:return SECOND_REWARD;
            case 30:return FINANCE_REWARD;
            case 31:return PAY_CHARGE_FEE;
            case 32:return GET_CHARGE_FEE;
            case 33:return AUTO_INVEST_BUY;
            case 34:return AUTO_INVEST_SELL;
            case 35:return LOCKED_SAVING_BUY;
            case 36:return LOCKED_SAVING_SELL;
            case 37:return TRANSFER_IN_COIN;
            case 38:return TRANSFER_OUT_COIN;
            case 39:return TRANSFER_IN_USDT;
            case 40:return TRANSFER_OUT_USDT;
            case 41:return TRANSFER_IN_SECOND;
            case 42:return TRANSFER_OUT_SECOND;
            case 43:return TRANSFER_IN;
            case 44:return TRANSFER_OUT;
            case 45:return CONTRACT_BLAST_PROFIT;
            case 46:return CONTRACT_BLAST_LOSS;
            case 47:return COMMISION_INVITER_FEE;
            case 48:return CAMPAIGN_CLAIM;
            default:return null;
        }
    }
    public static int parseOrdinal(TransactionType ordinal) {
        if (TransactionType.RECHARGE.equals(ordinal)) {
            return 0;
        } else if (TransactionType.WITHDRAW.equals(ordinal)) {
            return 1;
        } else if (TransactionType.TRANSFER_ACCOUNTS.equals(ordinal)) {
            return 2;
        } else if (TransactionType.EXCHANGE.equals(ordinal)) {
            return 3;
        } else if (TransactionType.OTC_BUY.equals(ordinal)) {
            return 4;
        } else if (TransactionType.OTC_SELL.equals(ordinal)) {
            return 5;
        } else if (TransactionType.ACTIVITY_AWARD.equals(ordinal)) {
            return 6;
        }else if (TransactionType.PROMOTION_AWARD.equals(ordinal)) {
            return 7;
        }else if (TransactionType.DIVIDEND.equals(ordinal)) {
            return 8;
        }else if (TransactionType.VOTE.equals(ordinal)) {
            return 9;
        }else if (TransactionType.ADMIN_RECHARGE.equals(ordinal)) {
            return 10;
        }else if (TransactionType.MATCH.equals(ordinal)) {
            return 11;
        }else if (TransactionType.ACTIVITY_BUY.equals(ordinal)) {
            return 12;
        }else if (TransactionType.CTC_BUY.equals(ordinal)) {
            return 13;
        }else if (TransactionType.CTC_SELL.equals(ordinal)) {
            return 14;
        }else if (TransactionType.RED_OUT.equals(ordinal)) {
            return 15;
        }else if (TransactionType.RED_IN.equals(ordinal)) {
            return 16;
        }else if (TransactionType.WITHDRAWCODE_OUT.equals(ordinal)){
            return 17;
        }else if (TransactionType.WITHDRAWCODE_IN.equals(ordinal)){
            return 18;
        }else if(TransactionType.CONTRACT_FEE.equals(ordinal)){
            return 19;
        }else if(TransactionType.CONTRACT_PROFIT.equals(ordinal)){
            return 20;
        }else if(TransactionType.CONTRACT_LOSS.equals(ordinal)){
            return 21;
        }else if(TransactionType.OPTION_FAIL.equals(ordinal)){
            return 22;
        }else if(TransactionType.OPTION_FEE.equals(ordinal)){
            return 23;
        }else if(TransactionType.OPTION_REWARD.equals(ordinal)){
            return 24;
        }else if(TransactionType.CONTRACT_AWARD.equals(ordinal)){
            return 25;
        }else if(TransactionType.LEVEL_AWARD.equals(ordinal)){
            return 26;
        }else if(TransactionType.PLATFORM_FEE_AWARD.equals(ordinal)){
            return 27;
        }else if(TransactionType.SECOND_FAIL.equals(ordinal)){
            return 28;
        }else if(TransactionType.SECOND_REWARD.equals(ordinal)){
            return 29;
        }else if(TransactionType.FINANCE_REWARD.equals(ordinal)){
            return 30;
        }else if(TransactionType.PAY_CHARGE_FEE.equals(ordinal)){
            return 31;
        }else if(TransactionType.GET_CHARGE_FEE.equals(ordinal)){
            return 32;
        }else if(TransactionType.AUTO_INVEST_BUY.equals(ordinal)){
            return 33;
        }else if(TransactionType.AUTO_INVEST_SELL.equals(ordinal)){
            return 34;
        }else if(TransactionType.LOCKED_SAVING_BUY.equals(ordinal)){
            return 35;
        }else if(TransactionType.LOCKED_SAVING_SELL.equals(ordinal)){
            return 36;
        }else if(TransactionType.TRANSFER_IN_COIN.equals(ordinal)){
            return 37;
        }else if(TransactionType.TRANSFER_OUT_COIN.equals(ordinal)){
            return 38;
        }else if(TransactionType.TRANSFER_IN_USDT.equals(ordinal)){
            return 39;
        }else if(TransactionType.TRANSFER_OUT_USDT.equals(ordinal)){
            return 40;
        }else if(TransactionType.TRANSFER_IN_SECOND.equals(ordinal)){
            return 41;
        }else if(TransactionType.TRANSFER_OUT_SECOND.equals(ordinal)){
            return 42;
        }else if(TransactionType.TRANSFER_IN.equals(ordinal)){
            return 43;
        }else if(TransactionType.TRANSFER_OUT.equals(ordinal)){
            return 44;
        }else if(TransactionType.CONTRACT_BLAST_PROFIT.equals(ordinal)){
            return 45;
        }else if(TransactionType.CONTRACT_BLAST_LOSS.equals(ordinal)){
            return 46;
        }else if(TransactionType.COMMISION_INVITER_FEE.equals(ordinal)){
            return 47;
        }else if(TransactionType.CAMPAIGN_CLAIM.equals(ordinal)){
            return 48;
        }else {
            return 49;
        }
    }

}
