//package com.wikex;
//
//import exchange.core2.core.ExchangeApi;
//import exchange.core2.core.common.CoreSymbolSpecification;
//import exchange.core2.core.common.SymbolType;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SystemInitializer {
//
//    @Bean
//    public ApplicationRunner initializeSystem(ExchangeApi exchangeApi,
//                                              UserService userService,
//                                              OrderService orderService) {
//        return args -> {
//            System.out.println("System startup: begin initialization");
//
//            // 1. Initialize trading pairs
//            initializeSymbols(exchangeApi);
//
//            // 2. Initialize users and balances
//            initializeUsersAndBalances(exchangeApi, userService);
//
//            // 3. Restore pending orders
//            restorePendingOrders(exchangeApi, orderService);
//
//            System.out.println("System initialization completed");
//        };
//    }
//
//    // Initialize trading pairs
//    private void initializeSymbols(ExchangeApi exchangeApi) {
//        CoreSymbolSpecification symbolSpec = CoreSymbolSpecification.builder()
//            .symbolId(1)  // Trading pair ID
//            .type(SymbolType.CURRENCY_EXCHANGE_PAIR)
//            .baseCurrency(1001)  // Currency ID for BTC
//            .quoteCurrency(1002) // Currency ID for LTC
//            .baseScaleK(1_000_000L)  // 1 lot = 0.01 BTC
//            .quoteScaleK(10_000L)    // Price tick size = 0.0001 LTC
//            .takerFee(1900L)         // Taker fee
//            .makerFee(700L)          // Maker fee
//            .build();
//
//        exchangeApi.submitCommandAsync(cmd -> cmd.addSymbol(symbolSpec));
//        System.out.println("Trading pair BTC/LTC initialized");
//    }
//
//    // Initialize users and balances
//    private void initializeUsersAndBalances(ExchangeApi exchangeApi, UserService userService) {
//        userService.getAllUsers().forEach(user -> {
//            // Add user to the matching engine
//            exchangeApi.submitCommandAsync(ApiAddUser.builder()
//                .uid(user.getId()).build());
//
//            // Set the user's initial balances
//            user.getBalances().forEach((currency, amount) -> {
//                exchangeApi.submitCommandAsync(ApiAdjustBalance.builder()
//                    .uid(user.getId())
//                    .currency(currency)
//                    .amount(amount)
//                    .build());
//            });
//
//            System.out.println("User " + user.getId() + " and balances initialized");
//        });
//    }
//
//    // Restore pending orders
//    private void restorePendingOrders(ExchangeApi exchangeApi, OrderService orderService) {
//        orderService.getPendingOrders().forEach(order -> {
//            exchangeApi.submitCommandAsync(ApiPlaceOrder.builder()
//                .uid(order.getUserId())
//                .orderId(order.getOrderId())
//                .symbol(order.getSymbolId())
//                .price(order.getPrice())
//                .size(order.getSize())
//                .action(order.getOrderAction())
//                .orderType(order.getOrderType())
//                .build());
//
//            System.out.println("Pending order " + order.getOrderId() + " restored");
//        });
//    }
//}
