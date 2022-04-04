package com.sadoon.cbotback.executor;

import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {
    private ExecutorService executorService;
    private Logger logger;

    @Bean
    public ExecutorService executorService() {
        executorService = Executors.newCachedThreadPool();
        return executorService;
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn(String.format("Executor service was interrupted on shutdown: %s", e.getMessage()));
            executorService.shutdownNow();
        }
    }
}