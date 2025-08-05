package com.genspring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genspring.dto.AIRequestDTO;
import com.genspring.dto.AIResponseDTO;
import com.genspring.dto.OpenAIRequestDTO;
import com.genspring.dto.OpenAIResponseDTO;
import com.genspring.entity.AIConversation;
import com.genspring.repository.AIConversationRepository;
import com.genspring.service.AIService;
import com.genspring.service.RateLimitService;
import com.genspring.service.UsageStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    }

    @Test
    void testGenerateText_Success() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO("Test prompt");
        AIResponseDTO response = new AIResponseDTO("Generated response");
        
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
                .andExpected(header().exists("X-RateLimit-Remaining"));
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
        // Given
        AIRequestDTO request = new AIRequestDTO(""); // Empty prompt

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

// Service Test
@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AIConversationRepository conversationRepository;

    @Mock
    private UsageStatsService usageStatsService;

    @InjectMocks
    private AIService aiService;

    @Test
    void testGenerateText_Success() {
        // Given
        AIRequestDTO request = new AIRequestDTO("Test prompt");
        request.setUserId("testUser");
        
        AIConversation conversation = new AIConversation("testUser", "Test prompt", "gpt-3.5-turbo");
        conversation.setId(1L);
        when(conversationRepository.save(any(AIConversation.class))).thenReturn(conversation);

        // Mock OpenAI response
        OpenAIResponseDTO openAIResponse = createMockOpenAIResponse();
        ResponseEntity<OpenAIResponseDTO> responseEntity = new ResponseEntity<>(openAIResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(OpenAIResponseDTO.class)))
                .thenReturn(responseEntity);

        // When
        AIResponseDTO result = aiService.generateText(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getResponse());
        assertEquals("Generated response", result.getResponse());
        verify(conversationRepository, times(2)).save(any(AIConversation.class));
        verify(usageStatsService).updateStats(eq("testUser"), eq(1), eq(100), eq(true), anyLong());
    }

    @Test
    void testGenerateText_OpenAIError() {
        // Given
        AIRequestDTO request = new AIRequestDTO("Test prompt");
        request.setUserId("testUser");
        
        AIConversation conversation = new AIConversation("testUser", "Test prompt", "gpt-3.5-turbo");
        conversation.setId(1L);
        when(conversationRepository.save(any(AIConversation.class))).thenReturn(conversation);

        // Mock OpenAI error
        when(restTemplate.postForEntity(anyString(), any(), eq(OpenAIResponseDTO.class)))
                .thenThrow(new RuntimeException("OpenAI API error"));

        // When
        AIResponseDTO result = aiService.generateText(request);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getStatus());
        assertNotNull(result.getError());
        verify(conversationRepository, times(2)).save(any(AIConversation.class));
        verify(usageStatsService).updateStats(eq("testUser"), eq(1), eq(0), eq(false), anyLong());
    }

    @Test
    void testGenerateSummary() {
        // Given
        AIRequestDTO request = new AIRequestDTO("Text to summarize");
        request.setUserId("testUser");
        
        AIConversation conversation = new AIConversation("testUser", "Please provide a concise summary of the following text:\n\nText to summarize", "gpt-3.5-turbo");
        conversation.setId(1L);
        when(conversationRepository.save(any(AIConversation.class))).thenReturn(conversation);

        OpenAIResponseDTO openAIResponse = createMockOpenAIResponse();
        ResponseEntity<OpenAIResponseDTO> responseEntity = new ResponseEntity<>(openAIResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(OpenAIResponseDTO.class)))
                .thenReturn(responseEntity);

        // When
        AIResponseDTO result = aiService.generateSummary(request);

        // Then
        assertNotNull(result);
        assertEquals("Generated response", result.getResponse());
    }

    @Test
    void testGetUserConversations() {
        // Given
        String userId = "testUser";
        AIConversation conversation = new AIConversation(userId, "Test prompt", "gpt-3.5-turbo");
        when(conversationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(conversation));

        // When
        List<AIConversation> result = aiService.getUserConversations(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }

    private OpenAIResponseDTO createMockOpenAIResponse() {
        OpenAIResponseDTO response = new OpenAIResponseDTO();
        OpenAIResponseDTO.Choice choice = new OpenAIResponseDTO.Choice();
        OpenAIRequestDTO.Message message = new OpenAIRequestDTO.Message("assistant", "Generated response");
        choice.setMessage(message);
        response.setChoices(List.of(choice));
        
        OpenAIResponseDTO.Usage usage = new OpenAIResponseDTO.Usage();
        usage.setTotalTokens(100);
        response.setUsage(usage);
        
        return response;
    }
}

// Rate Limit Service Test
@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
        // Use reflection to set private fields for testing
        try {
            java.lang.reflect.Field requestsPerMinuteField = RateLimitService.class.getDeclaredField("requestsPerMinute");
            requestsPerMinuteField.setAccessible(true);
            requestsPerMinuteField.set(rateLimitService, 5);
            
            java.lang.reflect.Field rateLimitEnabledField = RateLimitService.class.getDeclaredField("rateLimitEnabled");
            rateLimitEnabledField.setAccessible(true);
            rateLimitEnabledField.set(rateLimitService, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    void testRateLimit_AllowedRequests() {
        String userId = "testUser";
        
        // First 5 requests should be allowed
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitService.isAllowed(userId), "Request " + (i + 1) + " should be allowed");
        }
        
        // 6th request should be denied
        assertFalse(rateLimitService.isAllowed(userId), "6th request should be denied");
    }

    @Test
    void testRemainingRequests() {
        String userId = "testUser";
        
        assertEquals(5, rateLimitService.getRemainingRequests(userId));
        
        rateLimitService.isAllowed(userId);
        assertEquals(4, rateLimitService.getRemainingRequests(userId));
        
        rateLimitService.isAllowed(userId);
        assertEquals(3, rateLimitService.getRemainingRequests(userId));
    }

    @Test
    void testRateLimitDisabled() {
        try {
            java.lang.reflect.Field rateLimitEnabledField = RateLimitService.class.getDeclaredField("rateLimitEnabled");
            rateLimitEnabledField.setAccessible(true);
            rateLimitEnabledField.set(rateLimitService, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to disable rate limiting", e);
        }
        
        String userId = "testUser";
        
        // Should allow unlimited requests when disabled
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimitService.isAllowed(userId));
        }
    }

    @Test
    void testAnonymousUser() {
        // Test with null userId (anonymous user)
        assertTrue(rateLimitService.isAllowed(null));
        assertEquals(4, rateLimitService.getRemainingRequests(null));
    }

    @Test
    void testCleanup() {
        String userId = "testUser";
        rateLimitService.isAllowed(userId);
        
        // Should not throw exception
        assertDoesNotThrow(() -> rateLimitService.cleanup());
    }
}