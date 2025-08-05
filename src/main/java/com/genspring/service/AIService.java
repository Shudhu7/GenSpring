package com.genspring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genspring.dto.*;
import com.genspring.entity.AIConversation;
import com.genspring.repository.AIConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIConversationRepository conversationRepository;

    @Autowired
    private UsageStatsService usageStatsService;

    @Value("${ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${ai.openai.base-url}")
    private String openaiBaseUrl;

    @Value("${ai.openai.model}")
    private String defaultModel;

    @Value("${ai.max-tokens}")
    private Integer defaultMaxTokens;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIResponseDTO generateText(AIRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        // Save conversation to database
        AIConversation conversation = new AIConversation(
            request.getUserId() != null ? request.getUserId() : "anonymous",
            request.getPrompt(),
            request.getModel() != null ? request.getModel() : defaultModel
        );
        conversation.setTemperature(request.getTemperature() != null ? request.getTemperature() : 0.7);
        conversation.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens);
        conversation = conversationRepository.save(conversation);

        try {
            // Prepare OpenAI request
            OpenAIRequestDTO openAIRequest = new OpenAIRequestDTO(
                conversation.getModel(),
                request.getPrompt(),
                conversation.getMaxTokens(),
                conversation.getTemperature()
            );

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<OpenAIRequestDTO> entity = new HttpEntity<>(openAIRequest, headers);

            // Make API call
            logger.info("Making OpenAI API call for conversation ID: {}", conversation.getId());
            ResponseEntity<OpenAIResponseDTO> response = restTemplate.postForEntity(
                openaiBaseUrl + "/chat/completions",
                entity,
                OpenAIResponseDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                OpenAIResponseDTO openAIResponse = response.getBody();
                String aiResponse = openAIResponse.getChoices().get(0).getMessage().getContent();
                
                // Update conversation with success
                long processingTime = System.currentTimeMillis() - startTime;
                conversation.setResponse(aiResponse);
                conversation.setStatus("success");
                conversation.setTokensUsed(openAIResponse.getUsage().getTotalTokens());
                conversation.setProcessingTimeMs(processingTime);
                conversationRepository.save(conversation);

                // Update usage stats
                usageStatsService.updateStats(
                    conversation.getUserId(),
                    1,
                    openAIResponse.getUsage().getTotalTokens(),
                    true,
                    processingTime
                );

                // Create response
                AIResponseDTO aiResponseDTO = new AIResponseDTO(aiResponse);
                aiResponseDTO.setId(UUID.randomUUID().toString());
                aiResponseDTO.setModel(conversation.getModel());
                aiResponseDTO.setTokensUsed(openAIResponse.getUsage().getTotalTokens());
                
                logger.info("Successfully generated AI response for conversation ID: {}", conversation.getId());
                return aiResponseDTO;
            } else {
                throw new RuntimeException("OpenAI API returned non-200 status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error generating AI response for conversation ID: {}", conversation.getId(), e);
            
            // Update conversation with error
            long processingTime = System.currentTimeMillis() - startTime;
            conversation.setStatus("error");
            conversation.setErrorMessage(e.getMessage());
            conversation.setProcessingTimeMs(processingTime);
            conversationRepository.save(conversation);

            // Update usage stats for failed request
            usageStatsService.updateStats(
                conversation.getUserId(),
                1,
                0,
                false,
                processingTime
            );

            // Create error response
            AIResponseDTO errorResponse = new AIResponseDTO();
            errorResponse.setId(UUID.randomUUID().toString());
            errorResponse.setStatus("error");
            errorResponse.setError("Failed to generate AI response: " + e.getMessage());
            errorResponse.setTimestamp(LocalDateTime.now());
            
            return errorResponse;
        }
    }

    public AIResponseDTO generateSummary(AIRequestDTO request) {
        String summaryPrompt = "Please provide a concise summary of the following text:\n\n" + request.getPrompt();
        AIRequestDTO summaryRequest = new AIRequestDTO(summaryPrompt);
        summaryRequest.setUserId(request.getUserId());
        summaryRequest.setModel(request.getModel());
        summaryRequest.setMaxTokens(300); // Shorter for summaries
        summaryRequest.setTemperature(0.3); // Lower temperature for more focused summaries
        
        return generateText(summaryRequest);
    }

    public AIResponseDTO generateCreativeText(AIRequestDTO request) {
        String creativePrompt = "Be creative and imaginative in your response to: " + request.getPrompt();
        AIRequestDTO creativeRequest = new AIRequestDTO(creativePrompt);
        creativeRequest.setUserId(request.getUserId());
        creativeRequest.setModel(request.getModel());
        creativeRequest.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 800);
        creativeRequest.setTemperature(0.9); // Higher temperature for creativity
        
        return generateText(creativeRequest);
    }

    public AIResponseDTO analyzeText(AIRequestDTO request) {
        String analysisPrompt = "Please analyze the following text in detail, including tone, themes, and key insights:\n\n" + request.getPrompt();
        AIRequestDTO analysisRequest = new AIRequestDTO(analysisPrompt);
        analysisRequest.setUserId(request.getUserId());
        analysisRequest.setModel(request.getModel());
        analysisRequest.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 600);
        analysisRequest.setTemperature(0.2); // Lower temperature for analytical responses
        
        return generateText(analysisRequest);
    }

    public List<AIConversation> getUserConversations(String userId) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AIConversation> getRecentConversations(int limit) {
        return conversationRepository.findByStatusOrderByCreatedAtDesc("success")
                .stream()
                .limit(limit)
                .toList();
    }

    public AIConversation getConversationById(Long id) {
        return conversationRepository.findById(id).orElse(null);
    }
}