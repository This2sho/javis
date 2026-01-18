package com.javis.learn_hub.support.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(WithMockEventPublisher.MockEventPublisherConfig.class)
public @interface WithMockEventPublisher {

    @TestConfiguration
    class MockEventPublisherConfig {

        @Bean
        @Primary
        public ApplicationEventPublisher eventPublisher() {
            return Mockito.mock(ApplicationEventPublisher.class, Mockito.RETURNS_DEEP_STUBS);
        }
    }
}
