import api from './api';

/**
 * Admin Configuration Service
 * Handles all admin configuration-related API calls
 */

/**
 * Get current system configuration
 * GET /api/admin/config
 */
export const getAdminConfiguration = async () => {
  try {
    console.log('📡 Fetching admin configuration...');
    const response = await api.get('/admin/config');
    console.log('✅ Admin configuration fetched:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ Error fetching admin configuration:', error);
    throw error;
  }
};

/**
 * Save system configuration
 * PUT /api/admin/config
 */
export const saveAdminConfiguration = async (config) => {
  try {
    console.log('📡 Saving admin configuration...', config);
    const response = await api.put('/admin/config', config);
    console.log('✅ Admin configuration saved:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ Error saving admin configuration:', error);
    throw error;
  }
};

export default {
  getAdminConfiguration,
  saveAdminConfiguration,
};