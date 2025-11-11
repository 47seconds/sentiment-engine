import { api } from './api';
import { authService } from './authService';

/**
 * Helper: Check if user is a demo user
 */
function isDemoUser(user) {
    return user && user.id === -1;
}

/**
 * Alert Service
 * Handles all alert management API calls
 */

// Get all alerts (paginated)
export const getAllAlerts = async (page = 0, size = 20, sort = 'createdAt,desc') => {
  return await api.get('/alerts', {
    params: { page, size, sort },
  });
};

// Get alert by ID
export const getAlertById = async (alertId) => {
  return await api.get(`/alerts/${alertId}`);
};

// Get active alerts
export const getActiveAlerts = async () => {
  const user = authService.getCurrentUser();
  
  // DEMO MODE or backend unavailable: Return empty alerts
  if (isDemoUser(user)) {
    console.warn("⚠ DEMO MODE → Returning empty alerts list");
    return {
      data: [],
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 0,
      number: 0
    };
  }
  
  // Try backend, fallback to empty list if unavailable
  try {
    const response = await api.get('/alerts/active');
    return response;
  } catch (error) {
    console.warn("⚠ Backend unavailable, returning empty alerts list");
    return {
      data: [],
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 0,
      number: 0
    };
  }
};

// Get my alerts (for current authenticated driver)
export const getMyAlerts = async () => {
  const user = authService.getCurrentUser();
  
  // DEMO MODE: Return empty alerts for demo users
  if (isDemoUser(user)) {
    console.warn("⚠ DEMO MODE → Returning empty alerts list for demo user");
    return {
      data: [],
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 0,
      number: 0
    };
  }
  
  return await api.get('/alerts/my');
};

// Get critical alerts
export const getCriticalAlerts = async () => {
  return await api.get('/alerts/critical');
};

// Get alerts by severity
export const getAlertsBySeverity = async (severity) => {
  return await api.get(`/alerts/severity/${severity}`);
};

// Get alerts by driver
export const getDriverAlerts = async (driverId, page = 0, size = 20) => {
  return await api.get(`/alerts/driver/${driverId}`, {
    params: { page, size },
  });
};

// Get alerts by status
export const getAlertsByStatus = async (status, page = 0, size = 20) => {
  return await api.get(`/alerts/status/${status}`, {
    params: { page, size },
  });
};

// Get alert trends
export const getAlertTrends = async (days = 30) => {
  return await api.get('/alerts/trends', {
    params: { days },
  });
};

// Get alert statistics
export const getAlertStats = async () => {
  return await api.get('/alerts/statistics');
};

// Acknowledge alert
export const acknowledgeAlert = async (alertId, managerId) => {
  return await api.post(`/alerts/${alertId}/acknowledge`, { managerId });
};

// Assign alert to manager
export const assignAlert = async (alertId, managerId) => {
  return await api.post(`/alerts/${alertId}/assign`, { managerId });
};

// Resolve alert
export const resolveAlert = async (alertId, resolutionData) => {
  return await api.post(`/alerts/${alertId}/resolve`, resolutionData);
};

// Dismiss alert
export const dismissAlert = async (alertId, reason) => {
  return await api.post(`/alerts/${alertId}/dismiss`, { reason });
};

// Escalate alert
export const escalateAlert = async (alertId, reason) => {
  return await api.post(`/alerts/${alertId}/escalate`, { reason });
};

// Create automatic alert based on driver EMA score
export const createAutomaticAlert = async (driverId, alertData) => {
  try {
    // Create a mock alert that will appear in the alerts list
    // In a real system, this would be an API call to create the alert
    const mockAlert = {
      id: `alert-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      driverId: driverId,
      alertType: alertData.alertType || 'LOW_SENTIMENT_SCORE',
      severity: alertData.severity,
      status: 'ACTIVE',
      message: alertData.message,
      currentEmaScore: alertData.currentScore,
      thresholdValue: alertData.threshold,
      createdAt: new Date().toISOString(),
      driverName: alertData.driverName
    };
    
    // Store in localStorage to simulate persistent storage
    const existingAlerts = JSON.parse(localStorage.getItem('generatedAlerts') || '[]');
    
    // Check if similar alert already exists for this driver
    const existingAlert = existingAlerts.find(alert => 
      alert.driverId === driverId && 
      alert.severity === alertData.severity && 
      alert.status === 'ACTIVE'
    );
    
    if (!existingAlert) {
      existingAlerts.push(mockAlert);
      localStorage.setItem('generatedAlerts', JSON.stringify(existingAlerts));
      console.log(`Alert created for driver ${driverId}:`, mockAlert);
      return { success: true, alert: mockAlert };
    } else {
      console.log(`Similar alert already exists for driver ${driverId}`);
      return { success: false, message: 'Alert already exists' };
    }
  } catch (error) {
    console.error('Error creating automatic alert:', error);
    throw error;
  }
};

// Get generated alerts from localStorage
export const getGeneratedAlerts = async () => {
  try {
    const alerts = JSON.parse(localStorage.getItem('generatedAlerts') || '[]');
    return { data: alerts };
  } catch (error) {
    console.error('Error getting generated alerts:', error);
    return { data: [] };
  }
};

// Update a generated alert status
export const updateGeneratedAlert = async (alertId, updates) => {
  try {
    const existingAlerts = JSON.parse(localStorage.getItem('generatedAlerts') || '[]');
    const alertIndex = existingAlerts.findIndex(alert => alert.id === alertId);
    
    if (alertIndex !== -1) {
      existingAlerts[alertIndex] = { ...existingAlerts[alertIndex], ...updates };
      localStorage.setItem('generatedAlerts', JSON.stringify(existingAlerts));
      return { success: true, alert: existingAlerts[alertIndex] };
    }
    
    return { success: false, message: 'Alert not found' };
  } catch (error) {
    console.error('Error updating generated alert:', error);
    throw error;
  }
};

// Clear all generated alerts
export const clearGeneratedAlerts = async () => {
  try {
    localStorage.removeItem('generatedAlerts');
    return { success: true };
  } catch (error) {
    console.error('Error clearing generated alerts:', error);
    throw error;
  }
};

// Check and trigger alerts for drivers with low EMA scores
export const checkAndTriggerAlerts = async (drivers) => {
  const alertsToCreate = [];
  const createdAlerts = [];
  
  for (const driver of drivers) {
    const emaScore = driver.emaScore || 0;
    const driverName = driver.driverName || driver.name || `Driver #${driver.driverId}`;
    
    let alertData = null;
    
    // Critical threshold: EMA <= -0.6
    if (emaScore <= -0.6) {
      alertData = {
        driverId: driver.driverId,
        severity: 'CRITICAL',
        alertType: 'LOW_SENTIMENT_SCORE',
        message: `CRITICAL: ${driverName} has extremely low sentiment score (${emaScore.toFixed(2)}). Immediate action required.`,
        threshold: -0.6,
        currentScore: emaScore,
        driverName: driverName
      };
    }
    // Warning threshold: EMA <= -0.3
    else if (emaScore <= -0.3) {
      alertData = {
        driverId: driver.driverId,
        severity: 'HIGH',
        alertType: 'LOW_SENTIMENT_SCORE',
        message: `WARNING: ${driverName} has low sentiment score (${emaScore.toFixed(2)}). Management review recommended.`,
        threshold: -0.3,
        currentScore: emaScore,
        driverName: driverName
      };
    }
    
    if (alertData) {
      alertsToCreate.push(alertData);
      
      // Try to create the alert
      try {
        const result = await createAutomaticAlert(alertData.driverId, alertData);
        if (result.success) {
          createdAlerts.push(result.alert);
        }
      } catch (error) {
        console.error(`Failed to create alert for driver ${alertData.driverId}:`, error);
      }
    }
  }
  
  return createdAlerts;
};

export default {
  getAllAlerts,
  getAlertById,
  getActiveAlerts,
  getMyAlerts,
  getCriticalAlerts,
  getAlertsBySeverity,
  getDriverAlerts,
  getAlertsByStatus,
  getAlertTrends,
  getAlertStats,
  acknowledgeAlert,
  assignAlert,
  resolveAlert,
  dismissAlert,
  escalateAlert,
  createAutomaticAlert,
  checkAndTriggerAlerts,
  getGeneratedAlerts,
  updateGeneratedAlert,
  clearGeneratedAlerts,
};
