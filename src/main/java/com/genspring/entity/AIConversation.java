package com.genspring.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIConversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
    
    @Column(name = "model")
    private String model;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "max_tokens")
    private Integer maxTokens;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    public AIConversation(String userId, String prompt, String model) {
        this.userId = userId;
        this.prompt = prompt;
        this.model = model;
        this.createdAt = LocalDateTime.now();
        this.status = "pending";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "pending";
        }
    }
}

@Entity
@Table(name = "ai_usage_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIUsageStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "date")
    private LocalDateTime date;
    
    @Column(name = "requests_count")
    private Integer requestsCount;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @Column(name = "successful_requests")
    private Integer successfulRequests;
    
    @Column(name = "failed_requests")
    private Integer failedRequests;
    
    @Column(name = "avg_processing_time")
    private Double avgProcessingTime;

    public AIUsageStats(String userId) {
        this.userId = userId;
        this.date = LocalDateTime.now();
        this.requestsCount = 0;
        this.tokensUsed = 0;
        this.successfulRequests = 0;
        this.failedRequests = 0;
        this.avgProcessingTime = 0.0;
    }

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (requestsCount == null) {
            requestsCount = 0;
        }
        if (tokensUsed == null) {
            tokensUsed = 0;
        }
        if (successfulRequests == null) {
            successfulRequests = 0;
        }
        if (failedRequests == null) {
            failedRequests = 0;
        }
        if (avgProcessingTime == null) {
            avgProcessingTime = 0.0;
        }
    }
}