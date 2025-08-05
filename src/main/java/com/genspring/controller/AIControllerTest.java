package com.genspring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genspring.dto.AIRequestDTO;
import com.genspring.dto.AIResponseDTO;
import com.genspring.service.AIService;
import com.genspring.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
        when(aiService.generateText(any(AIRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/v1/ai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.response").value("Generated response"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    @Test
    void testGenerateText_RateLimitExceeded() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO("Test prompt");
        when(rateLimitService.isAllowed(any())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/v1/ai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpected(jsonPath("$.error").value("Rate limit exceeded"));
    }

    @Test
    void testGenerateText_ValidationError() throws Exception {
        // Given
        AIRequestDTO request = new AIRequestDTO(""); // Empty prompt

        // When & Then
        mockMvc.perform(post("/v1/ai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpected(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/v1/ai/health"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.status").value("UP"))
                .andExpected(jsonPath("$.service").value("GenSpring AI Service"));
    }
}

// Service Test
package com.genspring.service;

import com.genspring.dto.AIRequestDTO;
import com.genspring.dto.AIResponseDTO;
import com.genspring.entity.AIConversation;
import com.genspring.repository.AIConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        assertEquals("success", result.getStatus());
        assertEquals("Generated response", result.getResponse());
        verify(conversationRepository, times(2)).save(any(AIConversation.class));
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
package com.genspring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
        ReflectionTestUtils.setField(rateLimitService, "requestsPerMinute", 5);
        ReflectionTestUtils.setField(rateLimitService, "rateLimitEnabled", true);
    }

    @Test
    void testRateLimit_AllowedRequests() {
        String userId = "testUser";
        
        // First 5 requests should be allowed
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitService.isAllowed(userId));
        }
        
        // 6th request should be denied
        assertFalse(rateLimitService.isAllowed(userId));
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
        ReflectionTestUtils.setField(rateLimitService, "rateLimitEnabled", false);
        
        String userId = "testUser";
        
        // Should allow unlimited requests when disabled
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimitService.isAllowed(userId));
        }
    }
}