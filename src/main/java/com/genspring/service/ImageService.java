package com.genspring.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

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

    @Value("${ai.openai.vision-model}")
    private String defaultVisionModel;

    @Value("${ai.openai.image-model}")
    private String defaultImageModel;

    @Value("${ai.max-tokens}")
    private Integer defaultMaxTokens;

    public ImageResponseDTO analyzeImage(ImageAnalysisRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        // Save conversation to database
        AIConversation conversation = new AIConversation(
            request.getUserId() != null ? request.getUserId() : "anonymous",
            "Image Analysis: " + (request.getPrompt() != null ? request.getPrompt() : "Analyze this image"),
            request.getModel() != null ? request.getModel() : defaultVisionModel
        );
        conversation.setTemperature(request.getTemperature() != null ? request.getTemperature() : 0.7);
        conversation.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens);
        conversation = conversationRepository.save(conversation);

        try {
            // Prepare Vision API request
            OpenAIVisionRequestDTO visionRequest = createVisionRequest(request, conversation);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<OpenAIVisionRequestDTO> entity = new HttpEntity<>(visionRequest, headers);

            // Make API call
            logger.info("Making OpenAI Vision API call for conversation ID: {}", conversation.getId());
            ResponseEntity<OpenAIResponseDTO> response = restTemplate.postForEntity(
                openaiBaseUrl + "/chat/completions",
                entity,
                OpenAIResponseDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return handleSuccessfulVisionResponse(response.getBody(), conversation, startTime);
            } else {
                throw new RuntimeException("OpenAI Vision API returned non-200 status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return handleErrorResponse(e, conversation, startTime, "analysis");
        }
    }

    public ImageResponseDTO generateImage(ImageGenerationRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        // Save conversation to database
        AIConversation conversation = new AIConversation(
            request.getUserId() != null ? request.getUserId() : "anonymous",
            "Image Generation: " + request.getPrompt(),
            request.getModel() != null ? request.getModel() : defaultImageModel
        );
        conversation = conversationRepository.save(conversation);

        try {
            // Prepare DALL-E request
            OpenAIImageGenerationRequestDTO imageRequest = new OpenAIImageGenerationRequestDTO(
                request.getPrompt(),
                conversation.getModel(),
                request.getSize(),
                request.getQuality(),
                request.getStyle(),
                request.getN()
            );

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<OpenAIImageGenerationRequestDTO> entity = new HttpEntity<>(imageRequest, headers);

            // Make API call
            logger.info("Making OpenAI Image Generation API call for conversation ID: {}", conversation.getId());
            ResponseEntity<OpenAIImageGenerationResponseDTO> response = restTemplate.postForEntity(
                openaiBaseUrl + "/images/generations",
                entity,
                OpenAIImageGenerationResponseDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return handleSuccessfulImageGenerationResponse(response.getBody(), conversation, startTime, request.getPrompt());
            } else {
                throw new RuntimeException("OpenAI Image API returned non-200 status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return handleErrorResponse(e, conversation, startTime, "generation");
        }
    }

    private OpenAIVisionRequestDTO createVisionRequest(ImageAnalysisRequestDTO request, AIConversation conversation) {
        // Create content list
        List<OpenAIVisionRequestDTO.Content> contentList = new ArrayList<>();
        
        // Add text prompt
        String textPrompt = request.getPrompt() != null ? request.getPrompt() : 
            "Please analyze this image in detail, describing what you see, including objects, people, colors, composition, and any notable features.";
        contentList.add(new OpenAIVisionRequestDTO.Content("text", textPrompt, null));
        
        // Add image
        OpenAIVisionRequestDTO.ImageUrl imageUrl = new OpenAIVisionRequestDTO.ImageUrl();
        if ("base64".equals(request.getImageType())) {
            imageUrl.setUrl("data:image/jpeg;base64," + request.getImageData());
        } else {
            imageUrl.setUrl(request.getImageData());
        }
        imageUrl.setDetail("high");
        
        contentList.add(new OpenAIVisionRequestDTO.Content("image_url", null, imageUrl));
        
        // Create message
        OpenAIVisionRequestDTO.VisionMessage message = new OpenAIVisionRequestDTO.VisionMessage(
            "user", contentList
        );
        
        return new OpenAIVisionRequestDTO(
            conversation.getModel(),
            List.of(message),
            conversation.getMaxTokens(),
            conversation.getTemperature()
        );
    }

    private ImageResponseDTO handleSuccessfulVisionResponse(OpenAIResponseDTO openAIResponse, 
                                                           AIConversation conversation, 
                                                           long startTime) {
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
        ImageResponseDTO imageResponseDTO = new ImageResponseDTO(aiResponse, "analysis");
        imageResponseDTO.setId(UUID.randomUUID().toString());
        imageResponseDTO.setModel(conversation.getModel());
        imageResponseDTO.setTokensUsed(openAIResponse.getUsage().getTotalTokens());
        
        logger.info("Successfully analyzed image for conversation ID: {}", conversation.getId());
        return imageResponseDTO;
    }

    private ImageResponseDTO handleSuccessfulImageGenerationResponse(OpenAIImageGenerationResponseDTO openAIResponse,
                                                                    AIConversation conversation,
                                                                    long startTime,
                                                                    String originalPrompt) {
        List<String> imageUrls = openAIResponse.getData().stream()
            .map(OpenAIImageGenerationResponseDTO.ImageData::getUrl)
            .collect(Collectors.toList());
        
        String revisedPrompt = openAIResponse.getData().get(0).getRevisedPrompt();
        
        // Update conversation with success
        long processingTime = System.currentTimeMillis() - startTime;
        conversation.setResponse("Generated " + imageUrls.size() + " image(s)");
        conversation.setStatus("success");
        conversation.setProcessingTimeMs(processingTime);
        conversationRepository.save(conversation);

        // Update usage stats (no tokens for image generation)
        usageStatsService.updateStats(
            conversation.getUserId(),
            1,
            0,
            true,
            processingTime
        );

        // Create response
        ImageResponseDTO imageResponseDTO = new ImageResponseDTO();
        imageResponseDTO.setId(UUID.randomUUID().toString());
        imageResponseDTO.setType("generation");
        imageResponseDTO.setResponse("Successfully generated " + imageUrls.size() + " image(s)");
        imageResponseDTO.setImageUrls(imageUrls);
        imageResponseDTO.setModel(conversation.getModel());
        imageResponseDTO.setTimestamp(LocalDateTime.now());
        imageResponseDTO.setStatus("success");
        imageResponseDTO.setRevisedPrompt(revisedPrompt);
        
        logger.info("Successfully generated {} image(s) for conversation ID: {}", imageUrls.size(), conversation.getId());
        return imageResponseDTO;
    }

    private ImageResponseDTO handleErrorResponse(Exception e, AIConversation conversation, long startTime, String type) {
        logger.error("Error in image {} for conversation ID: {}", type, conversation.getId(), e);
        
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
        ImageResponseDTO errorResponse = new ImageResponseDTO();
        errorResponse.setId(UUID.randomUUID().toString());
        errorResponse.setType(type);
        errorResponse.setStatus("error");
        errorResponse.setError("Failed to " + (type.equals("analysis") ? "analyze image" : "generate image") + ": " + e.getMessage());
        errorResponse.setTimestamp(LocalDateTime.now());
        
        return errorResponse;
    }
}