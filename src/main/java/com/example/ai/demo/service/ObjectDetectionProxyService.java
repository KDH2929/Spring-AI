package com.example.ai.demo.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class ObjectDetectionProxyService {

    private static final Duration DETECTION_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient objectDetectionWebClient;

    public ObjectDetectionProxyService(@Qualifier("objectDetectionWebClient") WebClient objectDetectionWebClient) {
        this.objectDetectionWebClient = objectDetectionWebClient;
    }

    public Mono<String> detect(String message, String responseType, MultipartFile file) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("message", message);
        bodyBuilder.part("responseType", responseType);
        bodyBuilder.part("file", file.getResource())
                .filename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload")
                .contentType(file.getContentType() != null
                        ? MediaType.parseMediaType(file.getContentType())
                        : MediaType.APPLICATION_OCTET_STREAM);

        return objectDetectionWebClient.post()
                .uri("/detect")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(DETECTION_TIMEOUT);
    }
}
