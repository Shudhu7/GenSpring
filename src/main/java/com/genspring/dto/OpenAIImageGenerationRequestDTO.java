package com.genspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIImageGenerationRequestDTO {
    private String model;
    private String prompt;
    private Integer n;
    private String size;
    private String quality;
    private String style;
    @JsonProperty("response_format")
    private String responseFormat; // "url" or "b64_json"

    public OpenAIImageGenerationRequestDTO(String prompt, String model, String size, String quality, String style, Integer n) {
        this.prompt = prompt;
        this.model = model != null ? model : "dall-e-3";
        this.size = size != null ? size : "1024x1024";
        this.quality = quality != null ? quality : "standard";
        this.style = style != null ? style : "vivid";
        this.n = n != null ? n : 1;
        this.responseFormat = "url";
    }
}