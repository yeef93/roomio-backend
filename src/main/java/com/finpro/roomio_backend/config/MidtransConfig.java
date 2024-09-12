package com.finpro.roomio_backend.config;

import com.midtrans.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class MidtransConfig {
    @Value("${midtrans.server.key}")
    private String serverKey;

    @Value("${midtrans.client.key}")
    private String clientKey;

    @Value("${midtrans.is.production}")
    private boolean isProduction;

    @Bean
    public Config midtransConfiguration() {
        return new Config(
                serverKey,
                clientKey,
                isProduction,
                false, // enabledLog
                10000, // connectionTimeout
                10000, // readTimeout
                10000, // writeTimeout
                10,    // maxConnectionPool
                300,   // keepAliveDuration
                TimeUnit.MILLISECONDS, // httpClientTimeUnit
                null,  // irisIdempotencyKey
                null,  // paymentIdempotencyKey
                null,  // xAppendNotification
                null,  // xOverrideNotification
                null,  // proxyConfig
                Collections.emptyMap() // customHeaders
        );
    }
}
