package com.wikex.wikex.constant;

import java.util.EnumSet;
import java.util.Set;

public final class TransactionFlow {
    // CASHFLOW IN:
    private static final Set<TransactionType> CASHFLOW_IN = EnumSet.of(
            // Deposit
            TransactionType.RECHARGE,
            TransactionType.ADMIN_RECHARGE,
            TransactionType.WITHDRAWCODE_IN,

            // Fiat/OTC buy (user buys crypto)
            TransactionType.OTC_BUY);

    // CASHFLOW OUT:
    private static final Set<TransactionType> CASHFLOW_OUT = EnumSet.of(
            // Withdrawal
            TransactionType.WITHDRAW,
            TransactionType.WITHDRAWCODE_OUT,

            // Fiat/OTC sell (user loses crypto)
            TransactionType.OTC_SELL);

    private static final Set<TransactionType> NON_CASHFLOW = EnumSet.of(
            // Spot / Trade
            TransactionType.EXCHANGE,
            TransactionType.MATCH,
            TransactionType.CTC_BUY,
            TransactionType.CTC_SELL,

            // Transfer internal
            TransactionType.TRANSFER_ACCOUNTS,
            TransactionType.TRANSFER_IN,
            TransactionType.TRANSFER_OUT,
            TransactionType.TRANSFER_IN_COIN,
            TransactionType.TRANSFER_OUT_COIN,
            TransactionType.TRANSFER_IN_USDT,
            TransactionType.TRANSFER_OUT_USDT,
            TransactionType.TRANSFER_IN_SECOND,
            TransactionType.TRANSFER_OUT_SECOND,

            // Saving / Investment
            TransactionType.AUTO_INVEST_BUY,
            TransactionType.AUTO_INVEST_SELL,
            TransactionType.LOCKED_SAVING_BUY,
            TransactionType.LOCKED_SAVING_SELL,
            TransactionType.ACTIVITY_BUY,

            // Reward / Interest / Bonus
            TransactionType.ACTIVITY_AWARD,
            TransactionType.PROMOTION_AWARD,
            TransactionType.DIVIDEND,
            TransactionType.VOTE,
            TransactionType.OPTION_REWARD,
            TransactionType.CONTRACT_AWARD,
            TransactionType.LEVEL_AWARD,
            TransactionType.PLATFORM_FEE_AWARD,
            TransactionType.SECOND_REWARD,
            TransactionType.FINANCE_REWARD,
            TransactionType.COMMISION_INVITER_FEE,
            TransactionType.CAMPAIGN_CLAIM,

            // Fee / Funding
            TransactionType.CONTRACT_FEE,
            TransactionType.OPTION_FEE,
            TransactionType.PAY_CHARGE_FEE,
            TransactionType.GET_CHARGE_FEE,

            // Contract / Derivatives PnL
            TransactionType.CONTRACT_PROFIT,
            TransactionType.CONTRACT_LOSS,
            TransactionType.OPTION_FAIL,
            TransactionType.SECOND_FAIL,
            TransactionType.CONTRACT_BLAST_PROFIT,
            TransactionType.CONTRACT_BLAST_LOSS,

            // Red packet
            TransactionType.RED_IN,
            TransactionType.RED_OUT);

    private TransactionFlow() {
    }

    public static boolean isCashflowIn(TransactionType type) {
        return CASHFLOW_IN.contains(type);
    }

    public static boolean isCashflowOut(TransactionType type) {
        return CASHFLOW_OUT.contains(type);
    }

    public static boolean isCashFlow(TransactionType type) {
        return isCashflowIn(type) || isCashflowOut(type);
    }
}
