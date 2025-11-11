import { api } from './api';
import { authService } from './authService';
import { getDemoFeedbackForEntity, calculateDemoDriverScore } from './feedbackService';

/**
 * Helper: Check if user is a demo user
 */
function isDemoUser(user) {
    return user && user.id === -1;
}

/**
 * Demo Drivers List - Mock driver data for demo mode
 */
const DEMO_DRIVERS = [
    { driverId: 'D001', driverName: 'Rajesh Kumar', email: 'rajesh.kumar@moveinsync.com', phoneNumber: '+91-9876501234' },
    { driverId: 'D002', driverName: 'Priya Sharma', email: 'priya.sharma@moveinsync.com', phoneNumber: '+91-9876501235' },
    { driverId: 'D003', driverName: 'Amit Patel', email: 'amit.patel@moveinsync.com', phoneNumber: '+91-9876501236' },
    { driverId: 'D004', driverName: 'Sunita Singh', email: 'sunita.singh@moveinsync.com', phoneNumber: '+91-9876501237' },
    { driverId: 'D005', driverName: 'Vikram Reddy', email: 'vikram.reddy@moveinsync.com', phoneNumber: '+91-9876501238' },
];

/**
 * Helper: Calculate demo driver stats with sentiment scores
 */
function calculateDemoDriverStats(driverId) {
    // Import dynamically to avoid circular dependency issues
    let sentimentData;
    try {
        sentimentData = calculateDemoDriverScore(driverId);
    } catch (error) {
        console.warn('Could not calculate sentiment for driver:', driverId, error);
        sentimentData = {
            avgScore: 0.75, // Default good score when no feedback
            totalFeedback: 0,
            positiveCount: 0,
            neutralCount: 0,
            negativeCount: 0
        };
    }
    
    const driver = DEMO_DRIVERS.find(d => d.driverId === driverId);
    
    if (!driver) return null;
    
    // Calculate EMA score (0-100) from sentiment score (0-1)
    // If no feedback, default to 75 (good score)
    const emaScore = sentimentData.totalFeedback > 0 
        ? Math.round(sentimentData.avgScore * 100)
        : 75;
    
    // Determine alert level based on score
    let alertLevel = 'NORMAL';
    if (emaScore < 50) alertLevel = 'CRITICAL';
    else if (emaScore < 70) alertLevel = 'WARNING';
    
    return {
        ...driver,
        emaScore,
        totalFeedback: sentimentData.totalFeedback,
        positiveFeedback: sentimentData.positiveCount,
        neutralFeedback: sentimentData.neutralCount,
        negativeFeedback: sentimentData.negativeCount,
        alertLevel,
        lastFeedbackDate: sentimentData.totalFeedback > 0 ? new Date().toISOString() : null,
        trend: 'STABLE'
    };
}

/**
 * Driver Stats Service
 * Handles all driver statistics and performance API calls
 */

// Get overall statistics (dashboard KPIs)
export const getOverallStats = async () => {
  const user = authService.getCurrentUser();
  
  // DEMO MODE: Return mock stats for demo users
  if (isDemoUser(user)) {
    console.warn("⚠ DEMO MODE → Returning mock overall stats for demo user");
    
    // Calculate stats from all demo drivers
    const allDriverStats = DEMO_DRIVERS.map(d => calculateDemoDriverStats(d.driverId));
    const totalFeedback = allDriverStats.reduce((sum, d) => sum + d.totalFeedback, 0);
    const avgEmaScore = allDriverStats.length > 0
      ? Math.round(allDriverStats.reduce((sum, d) => sum + d.emaScore, 0) / allDriverStats.length)
      : 0;
    const activeAlerts = allDriverStats.filter(d => d.alertLevel !== 'NORMAL').length;
    
    return {
      data: {
        totalDrivers: DEMO_DRIVERS.length,
        activeAlerts,
        avgSentimentScore: avgEmaScore,
        feedbackCount: totalFeedback
      }
    };
  }
  
  // For regular users, try backend first, fallback to demo stats if backend unavailable
  try {
    const response = await api.get('/stats/overview');
    return response;
  } catch (error) {
    console.warn("⚠ Backend unavailable, falling back to demo stats for admin view");
    
    // Calculate stats from all demo drivers
    const allDriverStats = DEMO_DRIVERS.map(d => calculateDemoDriverStats(d.driverId));
    const totalFeedback = allDriverStats.reduce((sum, d) => sum + d.totalFeedback, 0);
    const avgEmaScore = allDriverStats.length > 0
      ? Math.round(allDriverStats.reduce((sum, d) => sum + d.emaScore, 0) / allDriverStats.length)
      : 0;
    const activeAlerts = allDriverStats.filter(d => d.alertLevel !== 'NORMAL').length;
    
    return {
      data: {
        totalDrivers: DEMO_DRIVERS.length,
        activeAlerts,
        avgSentimentScore: avgEmaScore,
        feedbackCount: totalFeedback
      }
    };
  }
};

// Get driver stats by ID
export const getDriverStats = async (driverId) => {
  const user = authService.getCurrentUser();
  
  // DEMO MODE: Return calculated stats for demo driver
  if (isDemoUser(user)) {
    console.warn("⚠ DEMO MODE → Returning stats for demo driver:", driverId);
    const driverStats = calculateDemoDriverStats(driverId);
    return {
      data: driverStats
    };
  }
  
  return await api.get(`/stats/driver/${driverId}`);
};

// Get all driver stats (for table)
export const getAllDriverStats = async () => {
  const user = authService.getCurrentUser();
  
  console.log('getAllDriverStats called, user:', user);
  
  // DEMO MODE: Return all demo drivers with calculated stats
  if (isDemoUser(user)) {
    console.warn("⚠ DEMO MODE → Returning all demo driver stats");
    const allDriverStats = DEMO_DRIVERS.map(d => calculateDemoDriverStats(d.driverId));
    console.log('Demo driver stats:', allDriverStats);
    return allDriverStats;
  }
  
  // For regular users, try backend first, fallback to demo drivers if backend unavailable
  try {
    console.log('Trying to fetch from backend...');
    const response = await api.get('/stats/all');
    console.log('Backend response:', response);
    return response;
  } catch (error) {
    console.warn("⚠ Backend unavailable, falling back to demo drivers for admin view", error);
    // Return demo drivers as fallback when backend is not available
    const allDriverStats = DEMO_DRIVERS.map(d => calculateDemoDriverStats(d.driverId));
    console.log('Fallback demo driver stats:', allDriverStats);
    return allDriverStats;
  }
};

// Get drivers needing attention (WARNING or CRITICAL)
export const getDriversNeedingAttention = async () => {
  return await api.get('/stats/needing-attention');
};

// Get drivers with critical alerts
export const getDriversWithCriticalAlerts = async () => {
  return await api.get('/stats/critical');
};

// Get drivers with improving sentiment
export const getDriversWithImprovingSentiment = async () => {
  return await api.get('/stats/improving');
};

// Get drivers with declining sentiment
export const getDriversWithDecliningSentiment = async () => {
  return await api.get('/stats/declining');
};

// Get EMA score distribution (for histogram/pie chart)
// Backend doesn't have this endpoint yet, so get all stats and calculate distribution on frontend
export const getEmaScoreDistribution = async () => {
  return await api.get('/stats/all');
};

// Recalculate driver stats from scratch
export const recalculateDriverStats = async (driverId) => {
  return await api.post(`/stats/driver/${driverId}/recalculate`);
};

// Export demo drivers list for use in forms
export const getDemoDriversList = () => {
  const user = authService.getCurrentUser();
  return isDemoUser(user) ? DEMO_DRIVERS : [];
};

export default {
  getOverallStats,
  getDriverStats,
  getAllDriverStats,
  getDriversNeedingAttention,
  getDriversWithCriticalAlerts,
  getDriversWithImprovingSentiment,
  getDriversWithDecliningSentiment,
  getEmaScoreDistribution,
  recalculateDriverStats,
  getDemoDriversList,
};
