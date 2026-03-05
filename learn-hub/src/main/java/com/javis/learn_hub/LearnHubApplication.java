package com.javis.learn_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableRetry
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class LearnHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnHubApplication.class, args);
	}

}
