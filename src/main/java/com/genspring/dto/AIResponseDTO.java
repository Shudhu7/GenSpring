package com.genspring.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDTO {
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