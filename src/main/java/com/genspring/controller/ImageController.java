package com.genspring.controller;

import com.genspring.dto.ImageAnalysisRequestDTO;
import com.genspring.dto.ImageGenerationRequestDTO;
import com.genspring.dto.ImageResponseDTO;
import com.genspring.service.ImageService;
import com.genspring.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/image")
@Tag(name = "Image Processing", description = "Image analysis and generation endpoints")
@CrossOrigin(origins = "*")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private ImageService imageService;

    @Autowired
    private RateLimitService rateLimitService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze image", description = "Analyze an image using AI vision capabilities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully analyzed image"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> analyzeImage(
            @Valid @RequestBody ImageAnalysisRequestDTO request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received image analysis request from user: {}", userId);
        
        // Set user ID if provided in header
        if (userId != null) {
            request.setUserId(userId);
        }
        
        // Check rate limit
        if (!rateLimitService.isAllowed(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("message", "Too many requests. Please try again later.");
            errorResponse.put("resetTime", rateLimitService.getResetTime(userId));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        try {
            ImageResponseDTO response = imageService.analyzeImage(request);
            
            // Add rate limit headers
            return ResponseEntity.ok()
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitService.getRemainingRequests(userId)))
                    .header("X-RateLimit-Reset", rateLimitService.getResetTime(userId).toString())
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Error analyzing image", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "Failed to analyze image. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/analyze/upload")
    @Operation(summary = "Analyze uploaded image", description = "Upload and analyze an image file")
    public ResponseEntity<?> analyzeUploadedImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "model", required = false) String model,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received image upload analysis request from user: {}", userId);
        
        // Check rate limit
        if (!rateLimitService.isAllowed(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("resetTime", rateLimitService.getResetTime(userId));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File must be an image"));
            }
            
            // Convert to base64
            byte[] fileBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            
            // Create request
            ImageAnalysisRequestDTO request = new ImageAnalysisRequestDTO(base64Image, "base64");
            request.setPrompt(prompt);
            request.setModel(model);
            request.setUserId(userId);
            
            ImageResponseDTO response = imageService.analyzeImage(request);
            
            return ResponseEntity.ok()
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitService.getRemainingRequests(userId)))
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Error analyzing uploaded image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to analyze uploaded image"));
        }
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate image", description = "Generate an image using AI based on text prompt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated image"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> generateImage(
            @Valid @RequestBody ImageGenerationRequestDTO request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received image generation request from user: {}", userId);
        
        // Set user ID if provided in header
        if (userId != null) {
            request.setUserId(userId);
        }
        
        // Check rate limit
        if (!rateLimitService.isAllowed(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("message", "Too many requests. Please try again later.");
            errorResponse.put("resetTime", rateLimitService.getResetTime(userId));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        try {
            ImageResponseDTO response = imageService.generateImage(request);
            
            // Add rate limit headers
            return ResponseEntity.ok()
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitService.getRemainingRequests(userId)))
                    .header("X-RateLimit-Reset", rateLimitService.getResetTime(userId).toString())
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Error generating image", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "Failed to generate image. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate/variations")
    @Operation(summary = "Generate image variations", description = "Generate variations of an existing image")
    public ResponseEntity<?> generateImageVariations(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "n", defaultValue = "1") Integer n,
            @RequestParam(value = "size", defaultValue = "1024x1024") String size,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received image variations request from user: {}", userId);
        
        // Check rate limit
        if (!rateLimitService.isAllowed(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("resetTime", rateLimitService.getResetTime(userId));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
            }
            
            // For now, return a placeholder response as variations require different API endpoint
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image variations feature coming soon");
            response.put("uploaded_file", file.getOriginalFilename());
            response.put("requested_variations", n);
            response.put("size", size);
            
            return ResponseEntity.ok(response);
                    
        } catch (Exception e) {
            logger.error("Error generating image variations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate image variations"));
        }
    }
}