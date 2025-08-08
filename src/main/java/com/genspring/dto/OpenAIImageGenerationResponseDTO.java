package com.genspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIImageGenerationResponseDTO {
    private Long created;
    private List<ImageData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageData {
        private String url;
        @JsonProperty("b64_json")
        private String b64Json;
        @JsonProperty("revised_prompt")
        private String revisedPrompt;
    }
}