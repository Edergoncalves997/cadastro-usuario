package com.EderProject.cadastro_usuario.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/images")
public class ImageProxyController {
    
    private final RestTemplate restTemplate;
    
    public ImageProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @GetMapping("/proxy")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            // Fazer a requisição para a URL da imagem
            byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
            
            if (imageBytes == null || imageBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }
            
            // Determinar o tipo de conteúdo baseado na URL
            String contentType = determineContentType(url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);
            headers.setCacheControl("public, max-age=3600"); // Cache por 1 hora
            headers.set("Access-Control-Allow-Origin", "*");
            headers.set("Access-Control-Allow-Methods", "GET");
            headers.set("Access-Control-Allow-Headers", "Content-Type");
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private String determineContentType(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.contains(".png")) {
            return "image/png";
        } else if (lowerUrl.contains(".gif")) {
            return "image/gif";
        } else if (lowerUrl.contains(".webp")) {
            return "image/webp";
        } else {
            // Para URLs do Google Books, assumir JPEG
            return "image/jpeg";
        }
    }
}
