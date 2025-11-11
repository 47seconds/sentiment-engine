import { api } from './api';

/**
 * User Service
 * Handles user management and authentication API calls
 */

// Get all users (paginated)
export const getAllUsers = async (page = 0, size = 20, sort = 'name,asc') => {
  return await api.get('/users', {
    params: { page, size, sort },
  });
};

// Get user by ID
export const getUserById = async (userId) => {
  return await api.get(`/users/${userId}`);
};

// Get user by email
export const getUserByEmail = async (email) => {
  return await api.get(`/users/email/${email}`);
};

// Create new user
export const createUser = async (userData) => {
  return await api.post('/users', userData);
};

// Update user
export const updateUser = async (userId, userData) => {
  return await api.put(`/users/${userId}`, userData);
};

// Delete user
export const deleteUser = async (userId) => {
  return await api.delete(`/users/${userId}`);
};

// Get users by role
export const getUsersByRole = async (role, page = 0, size = 20) => {
  return await api.get(`/users/role/${role}`, {
    params: { page, size },
  });
};

// Get drivers (users with DRIVER role)
export const getDrivers = async () => {
  return await api.get('/users/drivers');
};

// Search users
export const searchUsers = async (searchTerm, page = 0, size = 20) => {
  return await api.get('/users/search', {
    params: { q: searchTerm, page, size },
  });
};

// Login (future implementation with JWT)
export const login = async (email, password) => {
  return await api.post('/auth/login', { email, password });
};

// Logout
export const logout = async () => {
  localStorage.removeItem('authToken');
  return Promise.resolve();
};

export default {
  getAllUsers,
  getUserById,
  getUserByEmail,
  createUser,
  updateUser,
  deleteUser,
  getUsersByRole,
  getDrivers,
  searchUsers,
  login,
  logout,
};
