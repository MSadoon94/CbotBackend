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
    private ExecutorService entryScannerExecutor;

    @Bean("entryScannerExecutor")
    public ExecutorService entryScannerExecutor() {
        entryScannerExecutor = Executors.newCachedThreadPool();
        return entryScannerExecutor;
    }

    @PreDestroy
    public void shutdown(Logger logger) {
        entryScannerExecutor.shutdown();
        try {
            if (!entryScannerExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                entryScannerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn(String.format("Executor service was interrupted on shutdown: %s", e.getMessage()));
            entryScannerExecutor.shutdownNow();
        }
    }
}