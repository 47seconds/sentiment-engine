import { api } from './api';
import { authService } from './authService';

/**
 * Helper: Check if user is a demo user
 */
function isDemoUser(user) {
    return user && user.id === -1;
}

/**
 * Demo Mode Local Storage Key
 */
const DEMO_FEEDBACK_KEY = 'demo_mode_feedback';

/**
 * Helper: Calculate sentiment from text (simple keyword-based analysis)
 */
function calculateSentiment(text) {
    const lowerText = text.toLowerCase();
    
    // Positive keywords
    const positiveWords = ['good', 'great', 'excellent', 'amazing', 'wonderful', 'fantastic', 
                          'love', 'best', 'awesome', 'nice', 'happy', 'helpful', 'professional',
                          'polite', 'clean', 'safe', 'comfortable', 'friendly'];
    
    // Negative keywords
    const negativeWords = ['bad', 'terrible', 'horrible', 'worst', 'awful', 'poor', 'rude',
                          'late', 'dirty', 'unsafe', 'uncomfortable', 'angry', 'disappointed',
                          'slow', 'careless', 'unprofessional'];
    
    let positiveCount = 0;
    let negativeCount = 0;
    
    positiveWords.forEach(word => {
        if (lowerText.includes(word)) positiveCount++;
    });
    
    negativeWords.forEach(word => {
        if (lowerText.includes(word)) negativeCount++;
    });
    
    // Calculate sentiment score (0.0 to 1.0)
    const totalWords = positiveCount + negativeCount;
    let score = 0.5; // neutral
    
    if (totalWords > 0) {
        score = positiveCount / totalWords;
    }
    
    // Determine label
    let label = 'NEUTRAL';
    if (score >= 0.7) label = 'POSITIVE';
    else if (score <= 0.3) label = 'NEGATIVE';
    
    return { score, label };
}

/**
 * Helper: Save feedback to localStorage for demo mode
 */
function saveDemoFeedback(feedback) {
    try {
        const existing = localStorage.getItem(DEMO_FEEDBACK_KEY);
        const feedbackList = existing ? JSON.parse(existing) : [];
        feedbackList.push(feedback);
        localStorage.setItem(DEMO_FEEDBACK_KEY, JSON.stringify(feedbackList));
        console.log('✅ Demo feedback saved to localStorage:', feedback);
    } catch (error) {
        console.error('Failed to save demo feedback:', error);
    }
}

/**
 * Helper: Get all demo feedback from localStorage
 */
function getDemoFeedback() {
    try {
        const existing = localStorage.getItem(DEMO_FEEDBACK_KEY);
        return existing ? JSON.parse(existing) : [];
    } catch (error) {
        console.error('Failed to load demo feedback:', error);
        return [];
    }
}

/**
 * Feedback Service
 * Handles all feedback submission and retrieval API calls
 */

// Submit new feedback
export const submitFeedback = async (feedbackData) => {
  const user = authService.getCurrentUser();
  
  // DEMO MODE: Simulate successful feedback submission and store locally
  if (isDemoUser(user)) {
    console.warn("⚠ DEMO MODE → Simulating feedback submission for demo user");
    
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // Calculate sentiment from feedback text
    const sentiment = calculateSentiment(feedbackData.feedbackText);
    
    // Create feedback object
    const feedback = {
      id: Date.now(), // Use timestamp as unique ID
      entityType: feedbackData.entityType,
      entityId: feedbackData.entityId,
      feedbackText: feedbackData.feedbackText,
      rating: feedbackData.rating,
      source: feedbackData.source,
      sentimentLabel: sentiment.label,
      sentimentScore: sentiment.score,
      createdAt: new Date().toISOString(),
      status: "PROCESSED",
      submittedBy: user.id,
      submittedByEmail: user.email
    };
    
    // Save to localStorage
    saveDemoFeedback(feedback);
    
    // Return mock successful response
    return {
      data: {
        success: true,
        message: "Feedback submitted successfully (Demo Mode)",
        data: feedback
      }
    };
  }
  
  return await api.post('/feedback', feedbackData);
};

// Get feedback by ID
export const getFeedbackById = async (feedbackId) => {
  return await api.get(`/feedback/${feedbackId}`);
};

// Get feedback for a driver (paginated)
export const getDriverFeedback = async (driverId, page = 0, size = 20, sort = 'createdAt,desc') => {
  return await api.get(`/feedback/driver/${driverId}`, {
    params: { page, size, sort },
  });
};

// Get all feedback (paginated)
export const getAllFeedback = async (page = 0, size = 20, sort = 'createdAt,desc') => {
  return await api.get('/feedback', {
    params: { page, size, sort },
  });
};

// Get feedback requiring attention
export const getFeedbackRequiringAttention = async () => {
  return await api.get('/feedback/requiring-attention');
};

// Get feedback by sentiment label
export const getFeedbackBySentiment = async (sentimentLabel, page = 0, size = 20) => {
  return await api.get(`/feedback/sentiment/${sentimentLabel}`, {
    params: { page, size },
  });
};

// Get feedback by status
export const getFeedbackByStatus = async (status, page = 0, size = 20) => {
  return await api.get(`/feedback/status/${status}`, {
    params: { page, size },
  });
};

// Get recent feedback
export const getRecentFeedback = async (limit = 10) => {
  const user = authService.getCurrentUser();
  
  // DEMO MODE: Return stored demo feedback
  if (isDemoUser(user)) {
    console.warn("⚠ DEMO MODE → Returning stored demo feedback");
    const allFeedback = getDemoFeedback();
    
    // Sort by date (newest first) and limit
    const recentFeedback = allFeedback
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, limit);
    
    return {
      data: recentFeedback,
      content: recentFeedback,
      totalElements: allFeedback.length
    };
  }
  
  // For regular users, try backend first, fallback to demo feedback if backend unavailable
  try {
    const response = await api.get('/feedback/recent', {
      params: { limit },
    });
    return response;
  } catch (error) {
    console.warn("⚠ Backend unavailable, falling back to demo feedback");
    const allFeedback = getDemoFeedback();
    
    // Sort by date (newest first) and limit
    const recentFeedback = allFeedback
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, limit);
    
    return {
      data: recentFeedback,
      content: recentFeedback,
      totalElements: allFeedback.length
    };
  }
};

// Get feedback statistics for a driver
export const getDriverFeedbackStats = async (driverId) => {
  return await api.get(`/feedback/stats/driver/${driverId}`);
};

// Get sentiment trends for a driver
export const getDriverSentimentTrends = async (driverId, days = 30) => {
  return await api.get(`/feedback/trends/driver/${driverId}`, {
    params: { days },
  });
};

// Get keyword analysis for a driver
export const getDriverKeywordAnalysis = async (driverId) => {
  return await api.get(`/feedback/keywords/driver/${driverId}`);
};

// Process feedback (trigger sentiment analysis)
export const processFeedback = async (feedbackId) => {
  return await api.post(`/feedback/${feedbackId}/process`);
};

// Review feedback
export const reviewFeedback = async (feedbackId, reviewData) => {
  return await api.post(`/feedback/${feedbackId}/review`, reviewData);
};

// Search feedback
export const searchFeedback = async (searchTerm, page = 0, size = 20) => {
  return await api.get('/feedback/search', {
    params: { q: searchTerm, page, size },
  });
};

// Get feedback configuration (feature flags)
export const getFeedbackConfig = async () => {
  return await api.get('/feedback/config');
};

/**
 * Demo Mode Utilities
 * Export these for use in calculating demo driver stats
 */

// Get all demo feedback from localStorage
export const getAllDemoFeedback = () => {
  const user = authService.getCurrentUser();
  if (!isDemoUser(user)) return [];
  return getDemoFeedback();
};

// Get demo feedback for a specific entity
export const getDemoFeedbackForEntity = (entityId) => {
  const user = authService.getCurrentUser();
  if (!isDemoUser(user)) return [];
  
  const allFeedback = getDemoFeedback();
  return allFeedback.filter(f => String(f.entityId) === String(entityId));
};

// Calculate demo driver sentiment score
export const calculateDemoDriverScore = (entityId) => {
  const feedback = getDemoFeedbackForEntity(entityId);
  
  if (feedback.length === 0) {
    return {
      avgScore: 0,
      totalFeedback: 0,
      positiveCount: 0,
      neutralCount: 0,
      negativeCount: 0
    };
  }
  
  const scores = feedback.map(f => f.sentimentScore);
  const avgScore = scores.reduce((sum, score) => sum + score, 0) / scores.length;
  
  const positiveCount = feedback.filter(f => f.sentimentLabel === 'POSITIVE').length;
  const neutralCount = feedback.filter(f => f.sentimentLabel === 'NEUTRAL').length;
  const negativeCount = feedback.filter(f => f.sentimentLabel === 'NEGATIVE').length;
  
  return {
    avgScore,
    totalFeedback: feedback.length,
    positiveCount,
    neutralCount,
    negativeCount
  };
};

// Clear all demo feedback (for testing/reset)
export const clearDemoFeedback = () => {
  const user = authService.getCurrentUser();
  if (isDemoUser(user)) {
    localStorage.removeItem(DEMO_FEEDBACK_KEY);
    console.log('✅ Demo feedback cleared');
  }
};

export default {
  submitFeedback,
  getFeedbackById,
  getDriverFeedback,
  getAllFeedback,
  getFeedbackRequiringAttention,
  getFeedbackBySentiment,
  getFeedbackByStatus,
  getRecentFeedback,
  getDriverFeedbackStats,
  getDriverSentimentTrends,
  getDriverKeywordAnalysis,
  processFeedback,
  reviewFeedback,
  searchFeedback,
  getFeedbackConfig,
  // Demo mode utilities
  getAllDemoFeedback,
  getDemoFeedbackForEntity,
  calculateDemoDriverScore,
  clearDemoFeedback,
};
