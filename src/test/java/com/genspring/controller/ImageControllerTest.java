package com.genspring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.genspring.dto.ImageAnalysisRequestDTO;
import com.genspring.dto.ImageGenerationRequestDTO;
import com.genspring.dto.ImageResponseDTO;
import com.genspring.service.ImageService;
import com.genspring.service.RateLimitService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @MockBean
    private RateLimitService rateLimitService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testAnalyzeImage_Success() throws Exception {
        // Given
        ImageAnalysisRequestDTO request = new ImageAnalysisRequestDTO(
            "https://example.com/image.jpg", 
            "url"
        );
        request.setPrompt("Analyze this image");
        
        ImageResponseDTO response = new ImageResponseDTO("This image shows...", "analysis");
        response.setId("test-id");
        response.setModel("gpt-4-vision-preview");
        response.setTokensUsed(150);
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(any())).thenReturn(59);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));
        when(imageService.analyzeImage(any(ImageAnalysisRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/v1/image/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("This image shows..."))
                .andExpect(jsonPath("$.type").value("analysis"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    @Test
    void testAnalyzeUploadedImage_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test-image.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );
        
        ImageResponseDTO response = new ImageResponseDTO("Analysis of uploaded image", "analysis");
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(any())).thenReturn(59);
        when(imageService.analyzeImage(any(ImageAnalysisRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/v1/image/analyze/upload")
                .file(file)
                .param("prompt", "Describe this image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Analysis of uploaded image"));
    }

    @Test
    void testGenerateImage_Success() throws Exception {
        // Given
        ImageGenerationRequestDTO request = new ImageGenerationRequestDTO("A beautiful sunset");
        request.setSize("1024x1024");
        request.setQuality("hd");
        
        ImageResponseDTO response = new ImageResponseDTO();
        response.setId("test-id");
        response.setType("generation");
        response.setResponse("Successfully generated 1 image(s)");
        response.setImageUrls(List.of("https://example.com/generated-image.jpg"));
        response.setModel("dall-e-3");
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(any())).thenReturn(59);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));
        when(imageService.generateImage(any(ImageGenerationRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/v1/image/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("generation"))
                .andExpect(jsonPath("$.imageUrls").isArray())
                .andExpect(jsonPath("$.imageUrls[0]").value("https://example.com/generated-image.jpg"));
    }

    @Test
    void testAnalyzeImage_RateLimitExceeded() throws Exception {
        // Given
        ImageAnalysisRequestDTO request = new ImageAnalysisRequestDTO(
            "https://example.com/image.jpg", 
            "url"
        );
        when(rateLimitService.isAllowed(any())).thenReturn(false);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));

        // When & Then
        mockMvc.perform(post("/v1/image/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Rate limit exceeded"));
    }

    @Test
    void testGenerateImage_ValidationError() throws Exception {
        // Given - Empty prompt should trigger validation error
        ImageGenerationRequestDTO request = new ImageGenerationRequestDTO("");

        // When & Then
        mockMvc.perform(post("/v1/image/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAnalyzeUploadedImage_EmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "empty.jpg", 
            "image/jpeg", 
            new byte[0]
        );
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(multipart("/v1/image/analyze/upload")
                .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No file uploaded"));
    }

    @Test
    void testAnalyzeUploadedImage_InvalidFileType() throws Exception {
        // Given
        MockMultipartFile textFile = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "This is not an image".getBytes()
        );
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(multipart("/v1/image/analyze/upload")
                .file(textFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File must be an image"));
    }
}