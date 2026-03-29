package com.javis.learn_hub.support.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "evaluationExecutor")
    public ThreadPoolTaskExecutor evaluationExecutor(
            @Value("${async.executors.evaluation.core-pool-size:48}") int corePoolSize,
            @Value("${async.executors.evaluation.max-pool-size:96}") int maxPoolSize,
            @Value("${async.executors.evaluation.queue-capacity:10}") int queueCapacity
    ) {
        return threadPoolTaskExecutor(corePoolSize, maxPoolSize, queueCapacity, "eval-");
    }

    @Bean(name = "nextQuestionExecutor")
    public ThreadPoolTaskExecutor nextQuestionExecutor(
            @Value("${async.executors.next-question.core-pool-size:8}") int corePoolSize,
            @Value("${async.executors.next-question.max-pool-size:24}") int maxPoolSize,
            @Value("${async.executors.next-question.queue-capacity:10}") int queueCapacity
    ) {
        return threadPoolTaskExecutor(corePoolSize, maxPoolSize, queueCapacity, "nextq-");
    }

    @Bean(name = "messageExecutor")
    public ThreadPoolTaskExecutor messageExecutor(
            @Value("${async.executors.message.core-pool-size:4}") int corePoolSize,
            @Value("${async.executors.message.max-pool-size:12}") int maxPoolSize,
            @Value("${async.executors.message.queue-capacity:10}") int queueCapacity
    ) {
        return threadPoolTaskExecutor(corePoolSize, maxPoolSize, queueCapacity, "msg-");
    }

    private ThreadPoolTaskExecutor threadPoolTaskExecutor(int corePoolSize,
                                                          int maxPoolSize,
                                                          int queueCapacity,
                                                          String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
