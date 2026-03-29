package com.javis.learn_hub.support.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Value("${async.executors.evaluation.core-pool-size:48}")
    private int evaluationCorePoolSize;

    @Value("${async.executors.evaluation.max-pool-size:96}")
    private int evaluationMaxPoolSize;

    @Value("${async.executors.evaluation.queue-capacity:10}")
    private int evaluationQueueCapacity;

    @Value("${async.executors.next-question.core-pool-size:8}")
    private int nextQuestionCorePoolSize;

    @Value("${async.executors.next-question.max-pool-size:24}")
    private int nextQuestionMaxPoolSize;

    @Value("${async.executors.next-question.queue-capacity:10}")
    private int nextQuestionQueueCapacity;

    @Value("${async.executors.message.core-pool-size:4}")
    private int messageCorePoolSize;

    @Value("${async.executors.message.max-pool-size:12}")
    private int messageMaxPoolSize;

    @Value("${async.executors.message.queue-capacity:10}")
    private int messageQueueCapacity;

    @Bean(name = "evaluationExecutor")
    public ThreadPoolTaskExecutor evaluationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("eval-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "nextQuestionExecutor")
    public ThreadPoolTaskExecutor nextQuestionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(nextQuestionCorePoolSize);
        executor.setMaxPoolSize(nextQuestionMaxPoolSize);
        executor.setQueueCapacity(nextQuestionQueueCapacity);
        executor.setThreadNamePrefix("nextq-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "messageExecutor")
    public ThreadPoolTaskExecutor messageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("msg-");
        executor.initialize();
        return executor;
    }
}
