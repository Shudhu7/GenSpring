package com.genspring.service;

import com.genspring.entity.AIUsageStats;
import com.genspring.repository.AIUsageStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class UsageStatsService {

    private static final Logger logger = LoggerFactory.getLogger(UsageStatsService.class);

    @Autowired
    private AIUsageStatsRepository usageStatsRepository;

    public void updateStats(String userId, int requests, int tokens, boolean success, long processingTime) {
        try {
            LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            
            Optional<AIUsageStats> existingStats = usageStatsRepository.findByUserIdAndDate(userId, today);
            
            AIUsageStats stats;
            if (existingStats.isPresent()) {
                stats = existingStats.get();
                stats.setRequestsCount(stats.getRequestsCount() + requests);
                stats.setTokensUsed(stats.getTokensUsed() + tokens);
                
                if (success) {
                    stats.setSuccessfulRequests(stats.getSuccessfulRequests() + 1);
                } else {
                    stats.setFailedRequests(stats.getFailedRequests() + 1);
                }
                
                // Update average processing time
                double totalRequests = stats.getSuccessfulRequests() + stats.getFailedRequests();
                double currentAvg = stats.getAvgProcessingTime();
                stats.setAvgProcessingTime(((currentAvg * (totalRequests - 1)) + processingTime) / totalRequests);
                
            } else {
                stats = new AIUsageStats(userId);
                stats.setDate(today);
                stats.setRequestsCount(requests);
                stats.setTokensUsed(tokens);
                stats.setSuccessfulRequests(success ? 1 : 0);
                stats.setFailedRequests(success ? 0 : 1);
                stats.setAvgProcessingTime((double) processingTime);
            }
            
            usageStatsRepository.save(stats);
            logger.debug("Updated usage stats for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error updating usage stats for user: {}", userId, e);
        }
    }

    public List<AIUsageStats> getUserStats(String userId) {
        return usageStatsRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<AIUsageStats> getRecentStats(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return usageStatsRepository.findByDateAfter(fromDate);
    }

    public Long getTotalRequests(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return usageStatsRepository.getTotalRequestsAfterDate(fromDate).orElse(0L);
    }

    public Long getTotalTokens(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return usageStatsRepository.getTotalTokensAfterDate(fromDate).orElse(0L);
    }

    public List<Object[]> getTopUsers(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return usageStatsRepository.getTopUsersByRequests(fromDate);
    }
}