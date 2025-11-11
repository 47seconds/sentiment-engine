import axios from 'axios';
import toast from 'react-hot-toast';

// Base API URL - point directly at backend to avoid proxy/CORS mismatch during dev
const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000, // 15 seconds
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - add auth token, log requests
apiClient.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Log request in development
    if (import.meta.env.DEV) {
      console.log(`🌐 ${config.method?.toUpperCase()} ${config.url}`, config.data);
    }

    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor - handle errors globally
apiClient.interceptors.response.use(
  (response) => {
    // Log successful response in development
    if (import.meta.env.DEV) {
      console.log(`✅ ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data);
    }
    return response;
  },
  (error) => {
    // Handle different error types
    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;
      
      switch (status) {
        case 400:
          toast.error(data.message || 'Invalid request');
          break;
        case 401:
          toast.error('Unauthorized - Please login');
          // Redirect to login page (future implementation)
          break;
        case 403:
          toast.error('Access forbidden');
          break;
        case 404:
          toast.error(data.message || 'Resource not found');
          break;
        case 500:
          toast.error('Server error - Please try again later');
          break;
        default:
          toast.error(data.message || 'Something went wrong');
      }

      console.error(`❌ ${error.config.method?.toUpperCase()} ${error.config.url}`, {
        status,
        data,
      });
    } else if (error.request) {
      // Request made but no response
      toast.error('Network error - Cannot reach server');
      console.error('Network error:', error.request);
    } else {
      // Something else happened
      toast.error('Request failed');
      console.error('Error:', error.message);
    }

    return Promise.reject(error);
  }
);

// Generic API methods
export const api = {
  // GET request
  get: async (url, config = {}) => {
    try {
      const response = await apiClient.get(url, config);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // POST request
  post: async (url, data, config = {}) => {
    try {
      const response = await apiClient.post(url, data, config);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // PUT request
  put: async (url, data, config = {}) => {
    try {
      const response = await apiClient.put(url, data, config);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // PATCH request
  patch: async (url, data, config = {}) => {
    try {
      const response = await apiClient.patch(url, data, config);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // DELETE request
  delete: async (url, config = {}) => {
    try {
      const response = await apiClient.delete(url, config);
      return response.data;
    } catch (error) {
      throw error;
    }
  },
};

export default apiClient;
