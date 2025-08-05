package com.genspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// Request DTOs
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequestDTO {
    @NotBlank(message = "Prompt cannot be blank")
    @Size(max = 2000, message = "Prompt cannot exceed 2000 characters")
    private String prompt;
    
    private String model;
    private Integer maxTokens;
    private Double temperature;
    private String userId;

    public AIRequestDTO(String prompt) {
        this.prompt = prompt;
    }
}

// Response DTOs - MAKE THIS PUBLIC
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDTO {  // Changed from 'class' to 'public class'
    private String id;
    private String response;
    private String model;
    private LocalDateTime timestamp;
    private Integer tokensUsed;
    private String status;
    private String error;

    public AIResponseDTO(String response) {
        this.response = response;
        this.timestamp = LocalDateTime.now();
        this.status = "success";
    }
}

// OpenAI API DTOs - MAKE THESE PUBLIC
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequestDTO {  // Changed from 'class' to 'public class'
    private String model;
    private List<Message> messages;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;

    public OpenAIRequestDTO(String model, String prompt, Integer maxTokens, Double temperature) {
        this.model = model;
        this.messages = List.of(new Message("user", prompt));
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIResponseDTO {  // Changed from 'class' to 'public class'
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Integer index;
        private OpenAIRequestDTO.Message message;
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}