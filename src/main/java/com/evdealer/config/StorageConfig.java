package com.evdealer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @PostConstruct
    public void init() {
        try {
            // Create upload directories
            createUploadDirectories();
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }
    
    private void createUploadDirectories() throws Exception {
        String[] categories = {
            "vehicles", "brands", "models", "variants", "colors", 
            "inventory/main", "inventory/interior", "inventory/exterior",
            "feedback", "customer-documents/id_card", "customer-documents/driver_license",
            "customer-documents/other", "general"
        };
        
        for (String category : categories) {
            Path categoryPath = Paths.get(uploadDir, category);
            if (!Files.exists(categoryPath)) {
                Files.createDirectories(categoryPath);
            }
        }
    }
    
    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // Serve uploaded files statically
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // Cache for 1 hour
        
        // Serve thumbnails
        registry.addResourceHandler("/uploads/thumbnails/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(7200); // Cache thumbnails for 2 hours
    }
    
    @Bean
    public String uploadDirectory() {
        return uploadDir;
    }
}
