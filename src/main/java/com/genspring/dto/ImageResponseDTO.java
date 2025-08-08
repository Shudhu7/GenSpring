package com.genspring.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDTO {
    private String id;
    private String type; // "analysis" or "generation"
    private String response; // Analysis text or generation status
    private List<String> imageUrls; // For generated images
    private String model;
    private LocalDateTime timestamp;
    private Integer tokensUsed;
    private String status;
    private String error;
    private String revisedPrompt; // For DALL-E 3

    public ImageResponseDTO(String response, String type) {
        this.response = response;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.status = "success";
    }
}