package com.genspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationRequestDTO {
    
    @NotBlank(message = "Prompt cannot be blank")
    @Size(max = 1000, message = "Prompt cannot exceed 1000 characters")
    private String prompt;
    
    private String model; // DALL-E model version
    
    private String size; // 256x256, 512x512, 1024x1024, etc.
    
    private String quality; // standard, hd
    
    private String style; // vivid, natural
    
    private Integer n; // Number of images to generate
    
    private String userId;
    
    public ImageGenerationRequestDTO(String prompt) {
        this.prompt = prompt;
        this.size = "1024x1024";
        this.quality = "standard";
        this.style = "vivid";
        this.n = 1;
    }
}