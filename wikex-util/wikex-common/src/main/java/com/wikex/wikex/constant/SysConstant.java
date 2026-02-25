package com.wikex.wikex.constant;

public class SysConstant {

    public static final String SESSION_ADMIN = "ADMIN_MEMBER";

    public static final String SESSION_MEMBER = "API_MEMBER";

    public static final String TOKEN_MEMBER = "TOKEN_MEMBER_";

    public static final String PHONE_WITHDRAW_MONEY_CODE_PREFIX = "PHONE_WITHDRAW_MONEY_CODE_PREFIX_";
    public static final String EMAIL_WITHDRAW_MONEY_CODE_PREFIX = "EMAIL_WITHDRAW_MONEY_CODE_";

    public static final String EMAIL_UP_PWD_CODE_PREFIX = "EMAIL_UP_PWD_CODE_";

    public static final String EMAIL_TRANSACTION_CODE_PREFIX = "EMAIL_TRANSACTION_CODE_";

    public static final String PHONE_CTC_TRADE_CODE_PREFIX = "PHONE_CTC_TRADE_CODE_PREFIX_";

    public static final String PHONE_REG_CODE_PREFIX = "PHONE_REG_CODE_";
    public static final String EMAIL_REG_CODE_PREFIX = "EMAIL_REG_CODE_";

    public static final String PHONE_RESET_TRANS_CODE_PREFIX = "PHONE_RESET_TRANS_CODE_";

    public static final String PHONE_BIND_CODE_PREFIX = "PHONE_BIND_CODE_";

    public static final String PHONE_UPDATE_PASSWORD_PREFIX = "PHONE_UPDATE_PASSWORD_";

    public static final String PHONE_ADD_ADDRESS_PREFIX = "PHONE_ADD_ADDRESS_";

    public static final String PHONE_RECEIVE_ENVELOPE_PREFIX = "PHONE_RECEIVE_ENVELOPE_PREFIX"; // Receive red envelope

    public static final String PHONE_ATTEND_ACTIVITY_PREFIX = "PHONE_ADD_ADDRESS_";

    public static final String EMAIL_BIND_CODE_PREFIX = "EMAIL_BIND_CODE_";

    public static final String EMAIL_UNTIE_CODE_PREFIX = "EMAIL_UNTIE_CODE_";

    public static final String EMAIL_UPDATE_CODE_PREFIX = "EMAIL_UPDATE_CODE_";

    public static final String ADD_ADDRESS_CODE_PREFIX = "ADD_ADDRESS_CODE_";
    public static final String CHANGE_PASSWORD_CODE_PREFIX = "CHANGE_PASSWORD_CODE_";
    public static final String RESET_PASSWORD_CODE_PREFIX = "RESET_PASSWORD_CODE_";
    public static final String SETUP_PASSWORD_CODE_PREFIX = "SETUP_PASSWORD_CODE_";
    public static final String PHONE_CHANGE_CODE_PREFIX = "PHONE_CHANGE_CODE_";
    public static final String BIND_GOOGLE_CODE_PREFIX = "BIND_GOOGLE_CODE_";
    public static final String BIND_APPLE_CODE_PREFIX = "BIND_APPLE_CODE_";
    public static final String UNBIND_GOOGLE_CODE_PREFIX = "UNBIND_GOOGLE_CODE_";
    public static final String UNBIND_APPLE_CODE_PREFIX = "UNBIND_APPLE_CODE_";
    public static final String ENABLE_2FA_CODE_PREFIX = "ENABLE_2FA_CODE_";
    public static final String DISABLE_2FA_CODE_PREFIX = "DISABLE_2FA_CODE_";

    public static final String ADMIN_LOGIN_PHONE_PREFIX = "ADMIN_LOGIN_PHONE_PREFIX_";

    public static final String ADMIN_COIN_REVISE_PHONE_PREFIX = "ADMIN_COIN_REVISE_PHONE_PREFIX_";
    public static final String ADMIN_COIN_TRANSFER_COLD_PREFIX = "ADMIN_COIN_TRANSFER_COLD_PREFIX_";
    public static final String ADMIN_EXCHANGE_COIN_SET_PREFIX = "ADMIN_EXCHANGE_COIN_SET_PREFIX_";

    public static final String API_BIND_CODE_PREFIX = "API_BIND_CODE_PREFIX_";

    /** Anti-attack verification */
    public static final String ANTI_ATTACK_ = "ANTI_ATTACK_";

    public static final String ANTI_ROBOT_REGISTER = "ANTI_ROBOT_REGISTER_";
    // Safety and Development Fund Association
    public static final String SAFETY_AND_DEVELOPMENT_FUND_ASSOCIATION = "SAFETY_AND_DEVELOPMENT_FUND_ASSOCIATION";
    // Yesterday's total mining output
    public static final String YESTERDAY_MINE_AMOUNT_FOR_BHB = "YESTERDAY_MINE_AMOUNT_FOR_BHB";
    // Yesterday's holding dividends
    public static final String YESTERDAY_CASH_DIVIDENDS_AMOUNT_FOR_ETH = "YESTERDAY_CASH_DIVIDENDS_AMOUNT_FOR_ETH";
    // Safety fund: yesterday’s total mining, yesterday’s holding dividends;
    // expiration time set to 8 hours (28800s)
    public static final int SAFETH_AND_MINE_AND_DIVIDENDS_EXPIRE_TIME = 28800;

    public static final String BHB_AMOUNT = "BHB_AMOUNT";
    public static final int BHB_AMOUNT_EXPIRE_TIME = 900;

    public static final String NOTICE_DETAIL = "notice_detail_";
    public static final int NOTICE_DETAIL_EXPIRE_TIME = 300;

    public static final String SYS_HELP = "SYS_HELP";
    public static final int SYS_HELP_EXPIRE_TIME = 300;

    public static final String SYS_HELP_CATE = "SYS_HELP_CATE_";
    public static final int SYS_HELP_CATE_EXPIRE_TIME = 300;

    public static final String SYS_HELP_DETAIL = "SYS_HELP_DETAIL_";
    public static final int SYS_HELP_DETAIL_EXPIRE_TIME = 300;

    public static final String SYS_HELP_TOP = "SYS_HELP_TOP_";
    public static final int SYS_HELP_TOP_EXPIRE_TIME = 300;

    public static final String MARKET_HISTORY_LAST_KLINE_RESOLUTION = "MARKET_HISTORY_LAST_KLINE_RESOLUTION_";

    // Data dictionary cache
    public static final String DATA_DICTIONARY_BOUND_KEY = "data_dictionary_bound_key_";
    public static final int DATA_DICTIONARY_BOUND_EXPIRE_TIME = 604800;

    // Order book (market depth) data
    public static final String EXCHANGE_INIT_PLATE_SYMBOL_KEY = "EXCHANGE_INIT_PLATE_SYMBOL_KEY_";
    public static final int EXCHANGE_INIT_PLATE_SYMBOL_EXPIRE_TIME = 18000;

    // Cached number of bets (period + type ID)
    public static final String ALREADY_ORDER = "ALREADY_ORDER_";
    public static final int ALREADY_ORDER_EXPIRE_TIME = 900;
    // Coin quiz type cache
    public static final String QUIZ_TYPE = "QUIZ_TYPE_";
    public static final int QUIZ_TYPE_EXPIRE_TIME = 18000;
    // Coin quiz top 3 winners cache
    public static final String QUIZ_WIN = "QUIZ_WIN_";
    public static final int QUIZ_WIN_EXPIRE_TIME = 18000;
    // Coin quiz summary cache
    public static final String QUIZ_SUMMARY = "QUIZ_SUMMARY_";
    public static final int QUIZ_SUMMARY_EXPIRE_TIME = 18000;

    public static final String USER_ADD_EXCHANGE_ORDER_TIME_LIMIT = "USER_ADD_EXCHANGE_ORDER_TIME_LIMIT_";
    public static final int USER_ADD_EXCHANGE_ORDER_TIME_LIMIT_EXPIRE_TIME = 20;

    public static final String MEMBER_PROMOTION_TOP_RANK = "MEMBER_PROMOTION_TOP_RANK_";
    public static final String MEMBER_PROMOTION_TOP_RANK_DAY = "MEMBER_PROMOTION_TOP_RANK_DAY_";
    public static final String MEMBER_PROMOTION_TOP_RANK_WEEK = "MEMBER_PROMOTION_TOP_RANK_WEEK_";
    public static final String MEMBER_PROMOTION_TOP_RANK_MONTH = "MEMBER_PROMOTION_TOP_RANK_MONTH_";
    public static final int MEMBER_PROMOTION_TOP_RANK_EXPIRE_TIME = 129600; // Ranking cache for 1.5 days (36 hours)
    public static final int MEMBER_PROMOTION_TOP_RANK_EXPIRE_TIME_DAY = 129600; // Ranking cache for 1.5 days (36 hours)
    public static final int MEMBER_PROMOTION_TOP_RANK_EXPIRE_TIME_WEEK = 691200; // Ranking cache for 8 days
    public static final int MEMBER_PROMOTION_TOP_RANK_EXPIRE_TIME_MONTH = 2764800; // Ranking cache for 32 days

    public static final String CURRENCY = "CURRENCY";
    public static final int CURRENCY_HALF_HOUR = 1800;
}
