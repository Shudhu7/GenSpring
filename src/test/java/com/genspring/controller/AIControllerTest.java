package com.genspring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.genspring.dto.AIRequestDTO;
import com.genspring.dto.AIResponseDTO;
import com.genspring.entity.AIConversation;
import com.genspring.service.AIService;
import com.genspring.service.RateLimitService;
import com.genspring.service.UsageStatsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AIController.class)
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIService aiService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private UsageStatsService usageStatsService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGenerateText_Success() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO("Test prompt");
        AIResponseDTO response = new AIResponseDTO("Generated response");
        response.setId("test-id");
        response.setModel("gpt-3.5-turbo");
        response.setTokensUsed(50);
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(any())).thenReturn(59);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));
        when(aiService.generateText(any(AIRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/v1/ai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Generated response"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    @Test
    void testGenerateTextWithUserId_Success() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO("Test prompt");
        AIResponseDTO response = new AIResponseDTO("Generated response");
        response.setId("test-id");
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(any())).thenReturn(59);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));
        when(aiService.generateText(any(AIRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/v1/ai/generate")
                .header("X-User-ID", "testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Generated response"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    @Test
    void testGenerateText_RateLimitExceeded() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO("Test prompt");
        when(rateLimitService.isAllowed(any())).thenReturn(false);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));

        // When & Then
        mockMvc.perform(post("/v1/ai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Rate limit exceeded"));
    }

    @Test
    void testGenerateText_ValidationError() throws Exception {
        // Given - Empty prompt should trigger validation error
        AIRequestDTO request = new AIRequestDTO("");

        // When & Then
        mockMvc.perform(post("/v1/ai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSummarizeText_Success() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO("Text to summarize");
        AIResponseDTO response = new AIResponseDTO("Summary response");
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(any())).thenReturn(59);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));
        when(aiService.generateSummary(any(AIRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/v1/ai/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Summary response"));
    }

    @Test
    void testGenerateCreativeText_Success() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO("Creative prompt");
        AIResponseDTO response = new AIResponseDTO("Creative response");
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(any())).thenReturn(59);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));
        when(aiService.generateCreativeText(any(AIRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/v1/ai/creative")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Creative response"));
    }

    @Test
    void testAnalyzeText_Success() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO("Text to analyze");
        AIResponseDTO response = new AIResponseDTO("Analysis response");
        
        when(rateLimitService.isAllowed(any())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(any())).thenReturn(59);
        when(rateLimitService.getResetTime(any())).thenReturn(LocalDateTime.now().plusMinutes(1));
        when(aiService.analyzeText(any(AIRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/v1/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Analysis response"));
    }

    @Test
    void testGetUserConversations_Success() throws Exception {
        // Given
        AIConversation conversation = new AIConversation("testUser", "Test prompt", "gpt-3.5-turbo");
        when(aiService.getUserConversations("testUser")).thenReturn(List.of(conversation));

        // When & Then
        mockMvc.perform(get("/v1/ai/conversations")
                .header("X-User-ID", "testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value("testUser"));
    }

    @Test
    void testGetConversationById_Success() throws Exception {
        // Given
        AIConversation conversation = new AIConversation("testUser", "Test prompt", "gpt-3.5-turbo");
        when(aiService.getConversationById(1L)).thenReturn(conversation);

        // When & Then
        mockMvc.perform(get("/v1/ai/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("testUser"));
    }

    @Test
    void testGetConversationById_NotFound() throws Exception {
        // Given
        when(aiService.getConversationById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/v1/ai/conversations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/v1/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("GenSpring AI Service"));
    }
}