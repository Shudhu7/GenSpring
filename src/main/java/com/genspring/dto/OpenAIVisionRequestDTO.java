package com.genspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIVisionRequestDTO {
    private String model;
    private List<VisionMessage> messages;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisionMessage {
        private String role;
        private List<Content> content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String type; // "text" or "image_url"
        private String text; // For text content
        @JsonProperty("image_url")
        private ImageUrl imageUrl; // For image content
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUrl {
        private String url;
        private String detail; // "low", "high", or "auto"
    }
}