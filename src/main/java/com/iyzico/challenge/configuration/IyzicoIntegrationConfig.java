package com.iyzico.challenge.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.iyzipay.Options;

@Configuration
public class IyzicoIntegrationConfig {

    @Value("${iyzico.url}")
    private String baseUrl;

    @Value("${iyzico.api-key}")
    private String apiKey;

    @Value("${iyzico.secret-key}")
    private String secretKey;

    @Bean
    public Options iyzicoOptions() {
        Options options = new Options();
        options.setApiKey(apiKey);
        options.setSecretKey(secretKey);
        options.setBaseUrl(baseUrl);
        return options;
    }
}
