package com.example.ai.demo.controller;

import com.example.ai.demo.service.ObjectDetectionProxyService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import reactor.core.publisher.Mono;

@RestController
public class ObjectDetectionProxyController {

    private final ObjectDetectionProxyService objectDetectionProxyService;

    public ObjectDetectionProxyController(ObjectDetectionProxyService objectDetectionProxyService) {
        this.objectDetectionProxyService = objectDetectionProxyService;
    }

    @PostMapping(
        value = "/api/detect/proxy",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<String> service(
        @RequestParam("message") String message,
        @RequestParam("responseType") String responseType,
        @RequestPart("file") MultipartFile file
    ) {
        return objectDetectionProxyService.detect(message, responseType, file);
    }
}
