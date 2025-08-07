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
    
    @Column(name = "error_message", columnDefinition = "TEXT")  // Changed from VARCHAR(255) to TEXT
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
