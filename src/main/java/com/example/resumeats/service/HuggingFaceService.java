package com.example.resumeats.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Hugging Face Inference API ko call karta hai.
 * Model: sentence-transformers/all-MiniLM-L6-v2 (pipeline: sentence-similarity)
 * Yeh model job-description ko "source_sentence" aur resume-text ko "sentences" list
 * ke roop me lekar, seedha cosine-similarity score (0 to 1) return karta hai —
 * isliye humein khud embeddings compute / cosine similarity nikaalne ki zaroorat nahi.
 */
@Service
public class HuggingFaceService {

    private final WebClient webClient;

    @Value("${huggingface.model.similarity:sentence-transformers/all-MiniLM-L6-v2}")
    private String similarityModel;

    public HuggingFaceService(WebClient huggingFaceWebClient) {
        this.webClient = huggingFaceWebClient;
    }

    /**
     * @return 0.0 se 1.0 ke beech semantic similarity score
     */
    public double computeSimilarity(String jobDescription, String resumeText) {
        Map<String, Object> payload = Map.of(
                "inputs", Map.of(
                        "source_sentence", truncate(jobDescription),
                        "sentences", List.of(truncate(resumeText))
                ),
                "options", Map.of("wait_for_model", true)
        );

        try {
            List<Double> scores = webClient.post()
                    .uri("/hf-inference/models/{model}", similarityModel)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<Double>>() {})
                    .timeout(java.time.Duration.ofSeconds(30))
                    .block();

            if (scores == null || scores.isEmpty()) {
                throw new IllegalStateException("Received an empty response from Hugging Face.");
            }
            return scores.get(0);

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            // This is the real HTTP error from Hugging Face (401 = bad token, 404 = wrong model name,
            // 429 = rate limited, 503 = model still loading) — the body has the exact reason.
            throw new RuntimeException("Hugging Face API error (HTTP " + e.getStatusCode().value() + "): "
                    + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Hugging Face similarity call failed: " + e.getMessage(), e);
        }
    }

    // HF free tier request size limit ke andar rehne ke liye text ko trim karte hain
    private String truncate(String text) {
        int maxChars = 2000;
        return text.length() > maxChars ? text.substring(0, maxChars) : text;
    }
}
