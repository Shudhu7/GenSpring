package com.genspring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    @Value("${rate-limit.requests-per-minute}")
    private int requestsPerMinute;

    @Value("${rate-limit.enabled}")
    private boolean rateLimitEnabled;

    private final ConcurrentMap<String, UserRateLimit> rateLimitMap = new ConcurrentHashMap<>();

    public boolean isAllowed(String userId) {
        if (!rateLimitEnabled) {
            return true;
        }

        String key = userId != null ? userId : "anonymous";
        LocalDateTime now = LocalDateTime.now();
        
        rateLimitMap.compute(key, (k, userLimit) -> {
            if (userLimit == null) {
                userLimit = new UserRateLimit();
            }
            
            // Reset if a minute has passed
            if (userLimit.windowStart == null || 
                ChronoUnit.MINUTES.between(userLimit.windowStart, now) >= 1) {
                userLimit.windowStart = now;
                userLimit.requestCount = 0;
            }
            
            return userLimit;
        });

        UserRateLimit userLimit = rateLimitMap.get(key);
        boolean allowed = userLimit.requestCount < requestsPerMinute;
        
        if (allowed) {
            userLimit.requestCount++;
            logger.debug("Rate limit check passed for user: {} ({}/{})", 
                        key, userLimit.requestCount, requestsPerMinute);
        } else {
            logger.warn("Rate limit exceeded for user: {} ({}/{})", 
                       key, userLimit.requestCount, requestsPerMinute);
        }
        
        return allowed;
    }

    public int getRemainingRequests(String userId) {
        if (!rateLimitEnabled) {
            return Integer.MAX_VALUE;
        }

        String key = userId != null ? userId : "anonymous";
        UserRateLimit userLimit = rateLimitMap.get(key);
        
        if (userLimit == null) {
            return requestsPerMinute;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (userLimit.windowStart == null || 
            ChronoUnit.MINUTES.between(userLimit.windowStart, now) >= 1) {
            return requestsPerMinute;
        }
        
        return Math.max(0, requestsPerMinute - userLimit.requestCount);
    }

    public LocalDateTime getResetTime(String userId) {
        if (!rateLimitEnabled) {
            return null;
        }

        String key = userId != null ? userId : "anonymous";
        UserRateLimit userLimit = rateLimitMap.get(key);
        
        if (userLimit == null || userLimit.windowStart == null) {
            return null;
        }
        
        return userLimit.windowStart.plusMinutes(1);
    }

    // Clean up old entries periodically
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(2);
        rateLimitMap.entrySet().removeIf(entry -> {
            UserRateLimit limit = entry.getValue();
            return limit.windowStart != null && limit.windowStart.isBefore(cutoff);
        });
    }

    private static class UserRateLimit {
        LocalDateTime windowStart;
        int requestCount;
        
        UserRateLimit() {
            this.windowStart = null;
            this.requestCount = 0;
        }
    }
}