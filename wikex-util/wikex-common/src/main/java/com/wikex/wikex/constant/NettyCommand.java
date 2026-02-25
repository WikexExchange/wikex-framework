package com.wikex.wikex.constant;

public class NettyCommand {
    public static final int COMMANDS_VERSION = 1;
    public static final short SUBSCRIBE_SYMBOL_THUMB = 20001;
    public static final short UNSUBSCRIBE_SYMBOL_THUMB = 20002;
    public static final short PUSH_SYMBOL_THUMB = 20003;

    public static final short SUBSCRIBE_EXCHANGE = 20021;
    public static final short UNSUBSCRIBE_EXCHANGE = 20022;
    public static final short PUSH_EXCHANGE_TRADE = 20023;
    public static final short PUSH_EXCHANGE_PLATE = 20024;
    public static final short PUSH_EXCHANGE_KLINE = 20025;
    public static final short PUSH_EXCHANGE_ORDER_COMPLETED = 20026;
    public static final short PUSH_EXCHANGE_ORDER_CANCELED = 20027;
    public static final short PUSH_EXCHANGE_ORDER_TRADE = 20028;
    public static final short PUSH_EXCHANGE_DEPTH = 20029;

    public static final short SUBSCRIBE_CHAT = 20031;
    public static final short UNSUBSCRIBE_CHAT = 20032;
    public static final short PUSH_CHAT = 20033;

    public static final short SEND_CHAT = 20034;
    public static final short SUBSCRIBE_GROUP_CHAT = 20035;
    public static final short UNSUBSCRIBE_GROUP_CHAT = 20036;
    public static final short SUBSCRIBE_APNS = 20037;
    public static final short UNSUBSCRIBE_APNS = 20038;
    public static final short PUSH_GROUP_CHAT = 20039;

    // Contract-related
    public static final short CONTRACT_SUBSCRIBE_SYMBOL_THUMB = 30001; // Command: Subscribe to market quotes
    public static final short CONTRACT_UNSUBSCRIBE_SYMBOL_THUMB = 30002; // Command: Unsubscribe from market quotes
    public static final short CONTRACT_PUSH_SYMBOL_THUMB = 30003; // Command: Push symbol quotes

    public static final short CONTRACT_SUBSCRIBE_EXCHANGE = 30021; // Command: Subscribe to trading info (order book, K-line, trade details)
    public static final short CONTRACT_UNSUBSCRIBE_EXCHANGE = 30022;  // Command: Unsubscribe from trading info
    public static final short CONTRACT_PUSH_EXCHANGE_TRADE = 30023;  // Command: Push trade details
    public static final short CONTRACT_PUSH_EXCHANGE_PLATE = 30024;  // Command: Push order book data
    public static final short CONTRACT_PUSH_EXCHANGE_KLINE = 30025; // Command: Push K-line data
    public static final short CONTRACT_PUSH_EXCHANGE_ORDER_COMPLETED = 30026;  // Command: Push order completed info (specific user)
    public static final short CONTRACT_PUSH_EXCHANGE_ORDER_CANCELED = 30027; // Command: Push order canceled info (specific user)
    public static final short CONTRACT_PUSH_EXCHANGE_ORDER_TRADE = 30028; // Command: Push order trade info (specific user)
    public static final short CONTRACT_PUSH_EXCHANGE_DEPTH = 30029; // Command: Push order book depth
}
