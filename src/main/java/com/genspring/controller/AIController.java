package com.genspring.controller;

import com.genspring.dto.AIRequestDTO;
import com.genspring.dto.AIResponseDTO;
import com.genspring.entity.AIConversation;
import com.genspring.service.AIService;
import com.genspring.service.RateLimitService;
import com.genspring.service.UsageStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/ai")
@Tag(name = "AI Generation", description = "AI text generation and processing endpoints")
@CrossOrigin(origins = "*")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private AIService aiService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private UsageStatsService usageStatsService;

    @PostMapping("/generate")
    @Operation(summary = "Generate AI text", description = "Generate text using AI based on the provided prompt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated text"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> generateText(
            @Valid @RequestBody AIRequestDTO request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received text generation request from user: {}", userId);
        
        // Set user ID if provided in header
        if (userId != null) {
            request.setUserId(userId);
        }
        
        // Check rate limit
        if (!rateLimitService.isAllowed(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("message", "Too many requests. Please try again later.");
            errorResponse.put("resetTime", rateLimitService.getResetTime(userId));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        try {
            AIResponseDTO response = aiService.generateText(request);
            
            // Add rate limit headers
            return ResponseEntity.ok()
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitService.getRemainingRequests(userId)))
                    .header("X-RateLimit-Reset", rateLimitService.getResetTime(userId).toString())
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Error generating text", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "Failed to generate text. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/summarize")
    @Operation(summary = "Summarize text", description = "Generate a concise summary of the provided text")
    public ResponseEntity<?> summarizeText(
            @Valid @RequestBody AIRequestDTO request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received summarization request from user: {}", userId);
        
        if (userId != null) {
            request.setUserId(userId);
        }
        
        if (!rateLimitService.isAllowed(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("resetTime", rateLimitService.getResetTime(userId));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        try {
            AIResponseDTO response = aiService.generateSummary(request);
            return ResponseEntity.ok()
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitService.getRemainingRequests(userId)))
                    .body(response);
        } catch (Exception e) {
            logger.error("Error summarizing text", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to summarize text"));
        }
    }

    @PostMapping("/creative")
    @Operation(summary = "Generate creative text", description = "Generate creative content with higher temperature settings")
    public ResponseEntity<?> generateCreativeText(
            @Valid @RequestBody AIRequestDTO request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received creative generation request from user: {}", userId);
        
        if (userId != null) {
            request.setUserId(userId);
        }
        
        if (!rateLimitService.isAllowed(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("resetTime", rateLimitService.getResetTime(userId));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        try {
            AIResponseDTO response = aiService.generateCreativeText(request);
            return ResponseEntity.ok()
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitService.getRemainingRequests(userId)))
                    .body(response);
        } catch (Exception e) {
            logger.error("Error generating creative text", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate creative text"));
        }
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze text", description = "Perform detailed analysis of the provided text")
    public ResponseEntity<?> analyzeText(
            @Valid @RequestBody AIRequestDTO request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received text analysis request from user: {}", userId);
        
        if (userId != null) {
            request.setUserId(userId);
        }
        
        if (!rateLimitService.isAllowed(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("resetTime", rateLimitService.getResetTime(userId));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        try {
            AIResponseDTO response = aiService.analyzeText(request);
            return ResponseEntity.ok()
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitService.getRemainingRequests(userId)))
                    .body(response);
        } catch (Exception e) {
            logger.error("Error analyzing text", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to analyze text"));
        }
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get user conversations", description = "Retrieve conversation history for a user")
    public ResponseEntity<List<AIConversation>> getUserConversations(
            @RequestHeader(value = "X-User-ID", required = false) String userId,
            @Parameter(description = "User ID to get conversations for") 
            @RequestParam(required = false) String user) {
        
        String targetUserId = user != null ? user : userId;
        if (targetUserId == null) {
            targetUserId = "anonymous";
        }
        
        logger.info("Retrieving conversations for user: {}", targetUserId);
        
        try {
            List<AIConversation> conversations = aiService.getUserConversations(targetUserId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            logger.error("Error retrieving conversations for user: {}", targetUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get conversation by ID", description = "Retrieve a specific conversation by its ID")
    public ResponseEntity<AIConversation> getConversation(@PathVariable Long id) {
        logger.info("Retrieving conversation with ID: {}", id);
        
        try {
            AIConversation conversation = aiService.getConversationById(id);
            if (conversation != null) {
                return ResponseEntity.ok(conversation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving conversation with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the AI service is healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "GenSpring AI Service");
        return ResponseEntity.ok(health);
    }
}