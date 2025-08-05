package com.genspring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private RateLimitService rateLimitService;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupRateLimitData() {
        logger.debug("Performing rate limit cleanup");
        try {
            rateLimitService.cleanup();
        } catch (Exception e) {
            logger.error("Error during rate limit cleanup", e);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void dailyMaintenance() {
        logger.info("Performing daily maintenance tasks");
        try {
            // Add any daily maintenance tasks here
            // e.g., archiving old conversations, generating reports, etc.
            logger.info("Daily maintenance completed successfully");
        } catch (Exception e) {
            logger.error("Error during daily maintenance", e);
        }
    }
}