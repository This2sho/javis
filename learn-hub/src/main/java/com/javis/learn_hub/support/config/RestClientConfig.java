package com.javis.learn_hub.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setConnectionRequestTimeout(3000);
        factory.setReadTimeout(5000);

        return builder
                .requestFactory(factory)
                .messageConverters(converters -> {
                    converters.add(new MappingJackson2HttpMessageConverter());
                })
                .build();
    }
}
