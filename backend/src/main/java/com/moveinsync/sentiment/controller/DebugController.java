package com.moveinsync.sentiment.controller;

import com.moveinsync.sentiment.model.Feedback;
import com.moveinsync.sentiment.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @GetMapping("/feedback-sentiments")
    public Map<String, Object> debugFeedbackSentiments() {
        try {
            List<Feedback> allFeedback = feedbackRepository.findAll();
            Map<String, Object> result = new LinkedHashMap<>();
            
            result.put("total_feedback_count", allFeedback.size());
            
            for (int i = 0; i < allFeedback.size(); i++) {
                Feedback feedback = allFeedback.get(i);
                Map<String, Object> feedbackInfo = new LinkedHashMap<>();
                feedbackInfo.put("id", feedback.getId());
                feedbackInfo.put("content", feedback.getFeedbackText());
                feedbackInfo.put("rating", feedback.getRating());
                feedbackInfo.put("sentiment_label", feedback.getSentimentLabel());
                feedbackInfo.put("sentiment_score", feedback.getSentimentScore());
                feedbackInfo.put("confidence", feedback.getConfidence());
                feedbackInfo.put("keywords", feedback.getKeywords());
                feedbackInfo.put("requires_attention", feedback.getRequiresAttention());
                feedbackInfo.put("created_at", feedback.getCreatedAt());
                
                result.put("feedback_" + (i + 1), feedbackInfo);
            }
            
            return result;
        } catch (Exception e) {
            Map<String, Object> errorResult = new LinkedHashMap<>();
            errorResult.put("error", "Failed to fetch feedback: " + e.getMessage());
            errorResult.put("error_class", e.getClass().getSimpleName());
            return errorResult;
        }
    }
}