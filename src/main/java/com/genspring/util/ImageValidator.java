package com.genspring.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class ImageValidator {

    @Value("${image.allowed-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedTypesString;

    @Value("${image.max-file-size:10485760}")
    private long maxFileSize;

    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        List<String> allowedTypes = Arrays.asList(allowedTypesString.split(","));
        return allowedTypes.contains(contentType.toLowerCase());
    }

    public String getValidationError(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "No file provided";
        }

        if (file.getSize() > maxFileSize) {
            return "File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB";
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypesString.split(",")).contains(contentType.toLowerCase())) {
            return "Invalid file type. Allowed types: " + allowedTypesString;
        }

        return null;
    }
}