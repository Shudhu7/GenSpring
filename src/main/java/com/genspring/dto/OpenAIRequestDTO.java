package com.genspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequestDTO {
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