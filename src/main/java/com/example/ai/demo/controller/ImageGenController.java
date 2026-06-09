package com.example.ai.demo.controller;

import com.example.ai.demo.model.GenerateImageRequest;
import com.example.ai.demo.model.GeneratedImage;
import com.example.ai.demo.service.ImageGenService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
public class ImageGenController {

    private final ImageGenService imageGenService;

    public ImageGenController(ImageGenService imageGenService) {
        this.imageGenService = imageGenService;
    }

    @PostMapping(
        value = "/generate",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GeneratedImage generate(@RequestBody GenerateImageRequest request) {
        return imageGenService.generate(request);
    }
}
