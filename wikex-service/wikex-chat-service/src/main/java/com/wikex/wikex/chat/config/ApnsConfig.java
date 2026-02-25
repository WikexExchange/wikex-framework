package com.wikex.wikex.chat.config;

// @Configuration
public class ApnsConfig {
/*
    @Bean
    public ApnsService apnsServiceConfig(@Value("${apns.cert-file-path}") String certFile,
                                         @Value("${apns.cert-file-password}") String password,
                                         @Value("${apns.bundle-id}") String bundleId,
                                         @Value("${apns.dev-env:true}") Boolean isDevEnv) throws FileNotFoundException {

        InputStream is = new FileInputStream(certFile);
        System.out.println("password:" + password);
        com.cdeer.apns.http2.core.model.ApnsConfig config = new com.cdeer.apns.http2.core.model.ApnsConfig();
        config.setName("bitrade"); // Push service name
        config.setDevEnv(isDevEnv); // Whether it is a development environment
        config.setKeyStore(is); // Certificate
        config.setPassword(password); // Certificate password
        config.setPoolSize(5); // Thread pool size
        config.setTimeout(3000); // TCP connection timeout
        config.setTopic(bundleId); // Title, i.e. the certificate's bundle ID
        return ApnsServiceManager.createService(config);
    }
    */
}
