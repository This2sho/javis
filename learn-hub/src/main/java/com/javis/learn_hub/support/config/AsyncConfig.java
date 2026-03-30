package com.javis.learn_hub.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "evaluationExecutor")
    public ThreadPoolTaskExecutor evaluationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(300);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("eval-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "nextQuestionExecutor")
    public ThreadPoolTaskExecutor nextQuestionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("nextq-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "messageExecutor")
    public ThreadPoolTaskExecutor messageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("msg-");
        executor.initialize();
        return executor;
    }
}
