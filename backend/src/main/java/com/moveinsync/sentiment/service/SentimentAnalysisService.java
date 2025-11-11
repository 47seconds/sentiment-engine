package com.moveinsync.sentiment.service;

import com.moveinsync.sentiment.model.Feedback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Sentiment Analysis Service
 * 
 * Analyzes feedback text to determine sentiment polarity and confidence.
 * Now uses OpenRouter AI models for accurate sentiment analysis with 
 * keyword-based fallback for reliability.
 */
@Slf4j
@Service
public class SentimentAnalysisService {

    private final OpenRouterService openRouterService;
    
    @Value("${sentiment.analysis.use-ai:true}")
    private boolean useAI;

    @Autowired(required = false)
    public SentimentAnalysisService(OpenRouterService openRouterService) {
        this.openRouterService = openRouterService;
        log.info("SentimentAnalysisService initialized. OpenRouter service available: {}", openRouterService != null);
    }

    // Positive keywords and their weights
    private static final Map<String, Double> POSITIVE_KEYWORDS = Map.ofEntries(
        Map.entry("excellent", 1.0),
        Map.entry("amazing", 1.0),
        Map.entry("fantastic", 1.0),
        Map.entry("outstanding", 1.0),
        Map.entry("superb", 1.0),
        Map.entry("brilliant", 1.0),
        Map.entry("perfect", 0.9),
        Map.entry("wonderful", 0.9),
        Map.entry("awesome", 0.9),
        Map.entry("best", 0.9),
        Map.entry("top", 0.8),
        Map.entry("great", 0.8),
        Map.entry("friendly", 0.8),
        Map.entry("safe", 0.8),
        Map.entry("happy", 0.8),
        Map.entry("love", 0.9),
        Map.entry("recommend", 0.8),
        Map.entry("good", 0.7),
        Map.entry("nice", 0.6),
        Map.entry("pleasant", 0.7),
        Map.entry("helpful", 0.7),
        Map.entry("professional", 0.7),
        Map.entry("polite", 0.7),
        Map.entry("courteous", 0.7),
        Map.entry("clean", 0.6),
        Map.entry("comfortable", 0.6),
        Map.entry("punctual", 0.7),
        Map.entry("satisfied", 0.7),
        Map.entry("smooth", 0.6),
        Map.entry("efficient", 0.7),
        Map.entry("reliable", 0.7),
        Map.entry("trustworthy", 0.8),
        Map.entry("skilled", 0.7),
        Map.entry("experienced", 0.6),
        Map.entry("appreciate", 0.7),
        Map.entry("thanks", 0.6),
        Map.entry("thank", 0.6),
        Map.entry("pleased", 0.7),
        Map.entry("impressive", 0.8)
    );

    // Negative keywords and their weights
    private static final Map<String, Double> NEGATIVE_KEYWORDS = Map.ofEntries(
        Map.entry("terrible", -1.0),
        Map.entry("awful", -1.0),
        Map.entry("horrible", -1.0),
        Map.entry("worst", -1.0),
        Map.entry("disgusting", -1.0),
        Map.entry("pathetic", -1.0),
        Map.entry("useless", -0.9),
        Map.entry("disappointing", -0.8),
        Map.entry("unacceptable", -0.9),
        Map.entry("inappropriate", -0.8),
        Map.entry("rude", -0.9),
        Map.entry("unprofessional", -0.8),
        Map.entry("aggressive", -0.9),
        Map.entry("angry", -0.8),
        Map.entry("dangerous", -1.0),
        Map.entry("unsafe", -0.9),
        Map.entry("irresponsible", -0.8),
        Map.entry("careless", -0.7),
        Map.entry("bad", -0.7),
        Map.entry("poor", -0.7),
        Map.entry("annoying", -0.7),
        Map.entry("frustrating", -0.7),
        Map.entry("dissatisfied", -0.7),
        Map.entry("unhappy", -0.8),
        Map.entry("complain", -0.8),
        Map.entry("complaint", -0.8),
        Map.entry("hate", -0.9),
        Map.entry("dislike", -0.7),
        Map.entry("slow", -0.6),
        Map.entry("late", -0.6),
        Map.entry("dirty", -0.7),
        Map.entry("uncomfortable", -0.6),
        Map.entry("unreliable", -0.8),
        Map.entry("dishonest", -0.9),
        Map.entry("problem", -0.6),
        Map.entry("issue", -0.5),
        Map.entry("wrong", -0.6)
    );

    // Negation words that flip sentiment
    private static final Set<String> NEGATION_WORDS = Set.of(
        "not", "no", "never", "neither", "nobody", "nothing", "nowhere",
        "hardly", "scarcely", "barely", "doesn't", "isn't", "wasn't",
        "shouldn't", "wouldn't", "couldn't", "won't", "can't", "don't"
    );

    // Intensifiers that amplify sentiment
    private static final Map<String, Double> INTENSIFIERS = Map.of(
        "very", 1.5,
        "extremely", 1.8,
        "really", 1.4,
        "absolutely", 1.7,
        "totally", 1.6,
        "completely", 1.7,
        "quite", 1.2,
        "so", 1.3
    );

    /**
     * Analyze sentiment of feedback text
     * 
     * @param feedbackText Text to analyze
     * @return Sentiment analysis result
     */
    public SentimentResult analyzeSentiment(String feedbackText) {
        if (feedbackText == null || feedbackText.trim().isEmpty()) {
            log.warn("Empty feedback text provided for sentiment analysis");
            return new SentimentResult(0.0, 0.0, Feedback.SentimentLabel.NEUTRAL, new ArrayList<>());
        }

        log.info("=== SENTIMENT ANALYSIS START ===");
        log.info("Text: {}", feedbackText.substring(0, Math.min(50, feedbackText.length())));
        log.info("Config - useAI: {}, openRouterService available: {}", useAI, openRouterService != null);

        try {
            if (useAI && openRouterService != null) {
                log.info("🤖 USING OPENROUTER AI for sentiment analysis");
                // Use OpenRouter AI for sentiment analysis
                OpenRouterService.SentimentAnalysisResult aiResult = openRouterService.analyzeSentiment(feedbackText);
                
                if (aiResult != null) {
                    Feedback.SentimentLabel label = convertSentimentLabel(aiResult.getSentiment());
                    List<String> keywords = extractKeywordsFromText(feedbackText);
                    
                    log.info("AI sentiment analysis complete: score={}, confidence={}, label={}, reasoning={}",
                            aiResult.getScore(), aiResult.getConfidence(), label, aiResult.getReasoning());
                    
                    return new SentimentResult(aiResult.getScore(), aiResult.getConfidence(), label, keywords);
                }
            }
            
            // Fallback to keyword-based analysis
            log.warn("⚠️ FALLING BACK TO KEYWORD-BASED ANALYSIS");
            log.warn("Reason: useAI={}, openRouterService={}", useAI, openRouterService != null ? "available" : "null");
            return keywordBasedAnalysis(feedbackText);
            
        } catch (Exception e) {
            log.error("Error in sentiment analysis, falling back to keywords: {}", e.getMessage(), e);
            return keywordBasedAnalysis(feedbackText);
        }
    }

    /**
     * Convert string sentiment to enum
     */
    private Feedback.SentimentLabel convertSentimentLabel(String sentiment) {
        if (sentiment == null) return Feedback.SentimentLabel.NEUTRAL;
        
        return switch (sentiment.toUpperCase()) {
            case "POSITIVE" -> Feedback.SentimentLabel.POSITIVE;
            case "NEGATIVE" -> Feedback.SentimentLabel.NEGATIVE;
            case "NEUTRAL" -> Feedback.SentimentLabel.NEUTRAL;
            // Map old values to new simplified ones
            case "VERY_POSITIVE" -> Feedback.SentimentLabel.POSITIVE;
            case "VERY_NEGATIVE" -> Feedback.SentimentLabel.NEGATIVE;
            default -> Feedback.SentimentLabel.NEUTRAL;
        };
    }

    /**
     * Extract simple keywords from text
     */
    private List<String> extractKeywordsFromText(String text) {
        Set<String> keywords = new HashSet<>();
        String[] words = text.toLowerCase().split("\\s+");
        
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-z]", "");
            if (cleanWord.length() > 3 && (
                POSITIVE_KEYWORDS.containsKey(cleanWord) || 
                NEGATIVE_KEYWORDS.containsKey(cleanWord))) {
                keywords.add(cleanWord);
            }
        }
        
        return new ArrayList<>(keywords);
    }

    /**
     * Fallback keyword-based sentiment analysis
     */
    private SentimentResult keywordBasedAnalysis(String feedbackText) {
        String normalizedText = feedbackText.toLowerCase();
        
        // Extract keywords
        List<String> keywords = extractKeywords(normalizedText);
        
        // Calculate sentiment score
        double sentimentScore = calculateSentimentScore(normalizedText);
        
        // Calculate confidence based on keyword presence and text length
        double confidence = calculateConfidence(keywords, normalizedText);
        
        // Determine sentiment label
        Feedback.SentimentLabel label = determineSentimentLabel(sentimentScore);
        
        log.info("Keyword-based sentiment analysis: score={}, confidence={}, label={}, keywords={}",
                sentimentScore, confidence, label, keywords);
        
        return new SentimentResult(sentimentScore, confidence, label, keywords);
    }

    /**
     * Calculate sentiment score from text
     * 
     * @param normalizedText Normalized lowercase text
     * @return Sentiment score (-1.0 to 1.0)
     */
    private double calculateSentimentScore(String normalizedText) {
        String[] words = normalizedText.split("\\s+");
        double totalScore = 0.0;
        int scoredWords = 0;
        
        for (int i = 0; i < words.length; i++) {
            String word = words[i].replaceAll("[^a-z]", "");
            
            // Check for negation in previous 3 words
            boolean isNegated = false;
            for (int j = Math.max(0, i - 3); j < i; j++) {
                if (NEGATION_WORDS.contains(words[j].replaceAll("[^a-z]", ""))) {
                    isNegated = true;
                    break;
                }
            }
            
            // Check for intensifiers in previous 2 words
            double intensifier = 1.0;
            for (int j = Math.max(0, i - 2); j < i; j++) {
                String prevWord = words[j].replaceAll("[^a-z]", "");
                if (INTENSIFIERS.containsKey(prevWord)) {
                    intensifier = INTENSIFIERS.get(prevWord);
                    break;
                }
            }
            
            // Calculate word score
            Double wordScore = null;
            if (POSITIVE_KEYWORDS.containsKey(word)) {
                wordScore = POSITIVE_KEYWORDS.get(word);
            } else if (NEGATIVE_KEYWORDS.containsKey(word)) {
                wordScore = NEGATIVE_KEYWORDS.get(word);
            }
            
            if (wordScore != null) {
                // Apply negation (flip sentiment)
                if (isNegated) {
                    wordScore = -wordScore * 0.8; // Slightly reduced when negated
                }
                
                // Apply intensifier
                wordScore = wordScore * intensifier;
                
                totalScore += wordScore;
                scoredWords++;
            }
        }
        
        // Normalize to -1.0 to 1.0 range
        if (scoredWords == 0) {
            return 0.0; // Neutral if no sentiment words found
        }
        
        double avgScore = totalScore / scoredWords;
        
        // Clamp to -1.0 to 1.0
        return Math.max(-1.0, Math.min(1.0, avgScore));
    }

    /**
     * Extract keywords from text
     * 
     * @param normalizedText Normalized lowercase text
     * @return List of relevant keywords
     */
    private List<String> extractKeywords(String normalizedText) {
        Set<String> keywords = new HashSet<>();
        String[] words = normalizedText.split("\\s+");
        
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-z]", "");
            
            // Add sentiment keywords
            if (POSITIVE_KEYWORDS.containsKey(cleanWord) || NEGATIVE_KEYWORDS.containsKey(cleanWord)) {
                keywords.add(cleanWord);
            }
        }
        
        return new ArrayList<>(keywords);
    }

    /**
     * Calculate confidence score
     * 
     * @param keywords Extracted keywords
     * @param normalizedText Normalized text
     * @return Confidence score (0.0 to 1.0)
     */
    private double calculateConfidence(List<String> keywords, String normalizedText) {
        // Base confidence on keyword density and text length
        int wordCount = normalizedText.split("\\s+").length;
        
        if (wordCount == 0) {
            return 0.0;
        }
        
        // Confidence increases with keyword density
        double keywordDensity = (double) keywords.size() / wordCount;
        
        // Confidence increases with text length (up to a point)
        double lengthFactor = Math.min(1.0, wordCount / 20.0);
        
        // Combine factors
        double confidence = (keywordDensity * 0.7 + lengthFactor * 0.3);
        
        // Ensure minimum confidence if any keywords found
        if (keywords.size() > 0) {
            confidence = Math.max(0.3, confidence);
        }
        
        // Clamp to 0.0 to 1.0
        return Math.max(0.0, Math.min(1.0, confidence));
    }

    /**
     * Determine sentiment label from score
     * 
     * @param sentimentScore Sentiment score (-1.0 to 1.0)
     * @return Sentiment label
     */
    private Feedback.SentimentLabel determineSentimentLabel(double sentimentScore) {
        if (sentimentScore >= 0.6) {
            return Feedback.SentimentLabel.VERY_POSITIVE;
        } else if (sentimentScore >= 0.2) {
            return Feedback.SentimentLabel.POSITIVE;
        } else if (sentimentScore >= -0.2) {
            return Feedback.SentimentLabel.NEUTRAL;
        } else if (sentimentScore >= -0.6) {
            return Feedback.SentimentLabel.NEGATIVE;
        } else {
            return Feedback.SentimentLabel.VERY_NEGATIVE;
        }
    }

    /**
     * Batch analyze multiple feedback texts
     * 
     * @param feedbackTexts List of feedback texts
     * @return List of sentiment results
     */
    public List<SentimentResult> analyzeBatch(List<String> feedbackTexts) {
        log.info("Batch analyzing {} feedback texts", feedbackTexts.size());
        
        return feedbackTexts.stream()
                .map(this::analyzeSentiment)
                .toList();
    }

    /**
     * Sentiment analysis result
     */
    public record SentimentResult(
        double sentimentScore,
        double confidence,
        Feedback.SentimentLabel label,
        List<String> keywords
    ) {}
}
