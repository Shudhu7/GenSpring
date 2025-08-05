package com.genspring.controller;

import com.genspring.entity.AIUsageStats;
import com.genspring.service.UsageStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/stats")
@Tag(name = "Statistics", description = "Usage statistics and analytics endpoints")
@CrossOrigin(origins = "*")
public class StatsController {

    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);

    @Autowired
    private UsageStatsService usageStatsService;

    @GetMapping("/user")
    @Operation(summary = "Get user statistics", description = "Retrieve usage statistics for a specific user")
    public ResponseEntity<List<AIUsageStats>> getUserStats(
            @RequestHeader(value = "X-User-ID", required = false) String userId,
            @Parameter(description = "User ID to get stats for") 
            @RequestParam(required = false) String user) {
        
        String targetUserId = user != null ? user : userId;
        if (targetUserId == null) {
            targetUserId = "anonymous";
        }
        
        logger.info("Retrieving stats for user: {}", targetUserId);
        
        try {
            List<AIUsageStats> stats = usageStatsService.getUserStats(targetUserId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving stats for user: {}", targetUserId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent statistics", description = "Retrieve recent usage statistics across all users")
    public ResponseEntity<List<AIUsageStats>> getRecentStats(
            @Parameter(description = "Number of days to look back") 
            @RequestParam(defaultValue = "7") int days) {
        
        logger.info("Retrieving recent stats for {} days", days);
        
        try {
            List<AIUsageStats> stats = usageStatsService.getRecentStats(days);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving recent stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "Get statistics summary", description = "Get aggregated statistics summary")
    public ResponseEntity<Map<String, Object>> getStatsSummary(
            @Parameter(description = "Number of days to summarize") 
            @RequestParam(defaultValue = "7") int days) {
        
        logger.info("Retrieving stats summary for {} days", days);
        
        try {
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRequests", usageStatsService.getTotalRequests(days));
            summary.put("totalTokens", usageStatsService.getTotalTokens(days));
            summary.put("topUsers", usageStatsService.getTopUsers(days));
            summary.put("period", days + " days");
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error retrieving stats summary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/top-users")
    @Operation(summary = "Get top users", description = "Get users with most requests in the specified period")
    public ResponseEntity<List<Object[]>> getTopUsers(
            @Parameter(description = "Number of days to analyze") 
            @RequestParam(defaultValue = "30") int days) {
        
        logger.info("Retrieving top users for {} days", days);
        
        try {
            List<Object[]> topUsers = usageStatsService.getTopUsers(days);
            return ResponseEntity.ok(topUsers);
        } catch (Exception e) {
            logger.error("Error retrieving top users", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}