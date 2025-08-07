package com.genspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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