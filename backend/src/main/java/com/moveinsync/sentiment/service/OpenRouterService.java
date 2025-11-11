package com.moveinsync.sentiment.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * OpenRouter AI Service for Sentiment Analysis
 * 
 * Uses OpenRouter API with free models to analyze sentiment.
 * Provides more accurate sentiment analysis than keyword-based approach.
 */
@Slf4j
@Service
public class OpenRouterService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${openrouter.api.key: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}")
    private String apiKey;
    
    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${openrouter.model:openai/gpt-4o-mini}")
    private String model;

    public OpenRouterService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        // model value is injected by Spring; avoid logging a not-yet-initialized value in constructor
        log.debug("OpenRouterService constructed; model property will be logged at call time");
    }

    @PostConstruct
    public void init() {
        log.info("🤖 ========== OPENROUTER SERVICE INITIALIZED ==========");
        log.info("   API URL: {}", apiUrl);
        log.info("   Model: {}", model);
        log.info("   API Key: {}...{}", 
                apiKey != null ? apiKey.substring(0, 15) : "NULL", 
                apiKey != null ? apiKey.substring(apiKey.length() - 10) : "NULL");
        log.info("   RestTemplate: {}", restTemplate != null ? "Available" : "NULL");
        log.info("=====================================================");
    }

    /**
     * Analyze sentiment using OpenRouter AI model  
     * 
     * @param feedbackText Text to analyze
     * @return Sentiment analysis result
     */
    // Backwards-compatible analyze method
    public SentimentAnalysisResult analyzeSentiment(String feedbackText) {
        return analyzeSentiment(feedbackText, null);
    }

    /**
     * Analyze sentiment with optional rating (1..5).
     * If rating is null, prompt will indicate rating is not provided.
     */
    public SentimentAnalysisResult analyzeSentiment(String feedbackText, Integer rating) {
        log.info("OpenRouter analyzing sentiment for text: '{}' using model: {}", 
                feedbackText == null ? "<null>" : feedbackText.substring(0, Math.min(50, feedbackText.length())), model);

        if (feedbackText == null || feedbackText.trim().isEmpty()) {
            log.warn("Empty feedback text provided for sentiment analysis");
            return new SentimentAnalysisResult(0.0, 0.5, "NEUTRAL", "Empty text");
        }

        try {
            String prompt = createSentimentPrompt(feedbackText, rating);
            OpenRouterResponse response = callOpenRouter(prompt);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String aiResponse = response.getChoices().get(0).getMessage().getContent();
                return parseSentimentResponse(aiResponse, feedbackText);
            } else {
                log.warn("Empty response from OpenRouter for text: {}", feedbackText.substring(0, Math.min(50, feedbackText.length())));
                return fallbackAnalysis(feedbackText);
            }

        } catch (Exception e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage(), e);
            return fallbackAnalysis(feedbackText);
        }
    }

    /**
     * Create sentiment analysis prompt for the AI model
     */
    private String createSentimentPrompt(String feedbackText, Integer rating) {
        // rating may be null; include 'N/A' when not present
        String ratingText = rating == null ? "N/A" : rating.toString();
        return String.format("""
            Analyze the sentiment of the following user feedback and the provided rating (if any).

            Feedback: "%s"
            Rating: %s

            Respond ONLY with a JSON object and nothing else, using this exact structure:
            {
              "sentiment": "POSITIVE|NEGATIVE|NEUTRAL",
              "score": number,   // 1 (most negative) .. 5 (most positive) 
              "confidence": number, // 0.0 .. 1.0
              "reasoning": "brief explanation"
            }

            Instructions:
            - If you use a 1..5 score, 1 is most negative and 5 is most positive.
            - Prefer the 1..5 scale when a numeric rating is present, otherwise -1..1 is acceptable.
            - Use only the three labels: POSITIVE, NEGATIVE, NEUTRAL.
            - Keep reasoning short (1-2 sentences).
            """, feedbackText.replace("\"", "'"), ratingText);
    }

    /**
     * Call OpenRouter API
     */
    private OpenRouterResponse callOpenRouter(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "http://localhost:8080");
            headers.set("X-Title", "MoveInSync Sentiment Engine");

            OpenRouterRequest request = new OpenRouterRequest();
            request.setModel(model);
            request.setMessages(List.of(
                new OpenRouterMessage("user", prompt)
            ));
            request.setMaxTokens(150);
            request.setTemperature(0.1);

            HttpEntity<OpenRouterRequest> entity = new HttpEntity<>(request, headers);
            
            log.debug("Calling OpenRouter API with model: {}", model);
            ResponseEntity<OpenRouterResponse> response = restTemplate.postForEntity(
                apiUrl, entity, OpenRouterResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("OpenRouter API returned status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Failed to call OpenRouter API: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse AI response to extract sentiment data
     */
    private SentimentAnalysisResult parseSentimentResponse(String aiResponse, String originalText) {
        try {
            log.debug("Raw AI response: {}", aiResponse);
            
            // Clean response - extract JSON if wrapped in other text
            String jsonResponse = extractJsonFromResponse(aiResponse);
            log.debug("Extracted JSON: {}", jsonResponse);
            
            Map<String, Object> result = objectMapper.readValue(jsonResponse, Map.class);
            
            String sentiment = (String) result.get("sentiment");
            Double score = parseDouble(result.get("score"));
            Double confidence = parseDouble(result.get("confidence"));

            // Validate and normalize values
            sentiment = normalizeSentiment(sentiment);
            // If model returned score on 1..5 scale, convert to -1..1 (1 -> -1, 3 -> 0, 5 -> 1).
            if (score != null) {
                if (score >= 1.0 && score <= 5.0) {
                    score = (score - 3.0) / 2.0;
                } else {
                    // assume already -1..1
                    score = Math.max(-1.0, Math.min(1.0, score));
                }
            } else {
                score = 0.0;
            }
            confidence = Math.max(0.0, Math.min(1.0, confidence != null ? confidence : 0.8));
            
            log.info("✅ OpenRouter analysis: text='{}...' → sentiment={}, score={}, confidence={}", 
                    originalText.substring(0, Math.min(30, originalText.length())), 
                    sentiment, score, confidence);
            
            return new SentimentAnalysisResult(score, confidence, sentiment, "AI Analysis");
            
        } catch (Exception e) {
            log.warn("❌ Failed to parse OpenRouter response: '{}', using fallback. Error: {}", 
                    aiResponse, e.getMessage());
            return fallbackAnalysis(originalText);
        }
    }

    /**
     * Extract JSON from AI response that might contain extra text
     */
    private String extractJsonFromResponse(String response) {
        // Find JSON object in response
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        // If no JSON found, try to extract values manually
        return response;
    }

    /**
     * Normalize sentiment label
     */
    private String normalizeSentiment(String sentiment) {
        if (sentiment == null) return "NEUTRAL";
        
        String upper = sentiment.toUpperCase();
        return switch (upper) {
            case "POSITIVE" -> "POSITIVE";
            case "NEGATIVE" -> "NEGATIVE"; 
            case "NEUTRAL" -> "NEUTRAL";
            // Legacy mappings for backward compatibility
            case "VERY_POSITIVE", "VERY POSITIVE", "VERYPOSITIVE" -> "POSITIVE";
            case "VERY_NEGATIVE", "VERY NEGATIVE", "VERYNEGATIVE" -> "NEGATIVE";
            default -> "NEUTRAL";
        };
    }

    /**
     * Parse double from various input types
     */
    private Double parseDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Fallback analysis when AI call fails
     */
    private SentimentAnalysisResult fallbackAnalysis(String text) {
        log.info("Using fallback sentiment analysis for: {}", text.substring(0, Math.min(30, text.length())));
        
        String lowerText = text.toLowerCase();
        double score = 0.0;
        
        // Simple keyword-based fallback
        if (lowerText.contains("excellent") || lowerText.contains("amazing") || lowerText.contains("fantastic")) {
            score = 0.8;
        } else if (lowerText.contains("good") || lowerText.contains("great") || lowerText.contains("nice")) {
            score = 0.5;
        } else if (lowerText.contains("bad") || lowerText.contains("poor") || lowerText.contains("terrible")) {
            score = -0.5;
        } else if (lowerText.contains("awful") || lowerText.contains("horrible") || lowerText.contains("worst")) {
            score = -0.8;
        }
        
        String sentiment = score > 0.6 ? "VERY_POSITIVE" : 
                          score > 0.2 ? "POSITIVE" : 
                          score > -0.2 ? "NEUTRAL" : 
                          score > -0.6 ? "NEGATIVE" : "VERY_NEGATIVE";
        
        return new SentimentAnalysisResult(score, 0.6, sentiment, "Fallback analysis");
    }

    // Data classes for OpenRouter API
    @Data
    public static class OpenRouterRequest {
        private String model;
        private List<OpenRouterMessage> messages;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
    }

    @Data
    public static class OpenRouterMessage {
        private String role;
        private String content;
        
        public OpenRouterMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    public static class OpenRouterResponse {
        private List<Choice> choices;
        
        @Data
        public static class Choice {
            private OpenRouterMessage message;
        }
    }

    @Data
    public static class SentimentAnalysisResult {
        private final double score;
        private final double confidence;
        private final String sentiment;
        private final String reasoning;
    }
}