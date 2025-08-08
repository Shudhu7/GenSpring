package com.genspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisRequestDTO {
    
    @NotBlank(message = "Image URL or base64 data cannot be blank")
    private String imageData; // Can be URL or base64 encoded image
    
    private String imageType; // "url" or "base64"
    
    private String prompt; // Optional additional prompt for analysis
    
    private String model; // Vision model to use
    
    private String userId;
    
    private Integer maxTokens;
    
    private Double temperature;
    
    public ImageAnalysisRequestDTO(String imageData, String imageType) {
        this.imageData = imageData;
        this.imageType = imageType != null ? imageType : "url";
    }
}