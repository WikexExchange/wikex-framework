// package com.wikex.wikex.user.config;

// import lombok.Data;
// import lombok.NoArgsConstructor;
// import org.redisson.config.TransportMode;
// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.stereotype.Component;

// /**
//  * Redisson configuration properties
//  *
//  * @author Lion Li
//  */
// @Data
// @Component
// @ConfigurationProperties(prefix = "redisson")
// public class RedissonProperties {

//     /**
//      * Number of threads, default value = number of CPU cores * 2
//      */
//     private int threads;

//     /**
//      * Number of Netty threads, default value = number of CPU cores * 2
//      */
//     private int nettyThreads;

//     /**
//      * Transport mode
//      */
//     private TransportMode transportMode;

//     /**
//      * Single server configuration
//      */
//     private SingleServerConfig singleServerConfig;

//     @Data
//     @NoArgsConstructor
//     public static class SingleServerConfig {

//         /**
//          * Client name
//          */
//         private String clientName;

//         /**
//          * Minimum number of idle connections
//          */
//         private int connectionMinimumIdleSize;

//         /**
//          * Connection pool size
//          */
//         private int connectionPoolSize;

//         /**
//          * Connection idle timeout, in milliseconds
//          */
//         private int idleConnectionTimeout;

//         /**
//          * Command waiting timeout, in milliseconds
//          */
//         private int timeout;

//         /**
//          * If a send attempt succeeds within this limit, timeout countdown begins
//          */
//         private int retryAttempts;

//         /**
//          * Interval between retrying commands, in milliseconds
//          */
//         private int retryInterval;

//         /**
//          * Minimum number of idle connections for publish/subscribe
//          */
//         private int subscriptionConnectionMinimumIdleSize;

//         /**
//          * Publish/subscribe connection pool size
//          */
//         private int subscriptionConnectionPoolSize;

//         /**
//          * Maximum number of subscriptions per connection
//          */
//         private int subscriptionsPerConnection;

//         /**
//          * DNS monitoring interval, in milliseconds
//          */
//         private int dnsMonitoringInterval;
//     }
// }
