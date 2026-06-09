package com.example.ai.demo.model;

public record GenerateImageRequest (    
    String prompt,
    Integer width,
    Integer height,
    String quality,
    Integer n
) {}
