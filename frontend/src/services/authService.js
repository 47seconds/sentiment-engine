import api from './api';

const TOKEN_KEY = 'jwt_token';
const USER_KEY = 'user_info';

/**
 * Authentication Service
 * Handles login, registration, logout, and token management
 */
export const authService = {
    /**
     * Login user
     */
    async login(email, password) {
    //     const DEMO_EMAILS = ['john.employee@moveinsync.com', 'mary.employee@moveinsync.com'];

    // // Activate demo login ONLY when running locally
    // const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

    // if (isLocalhost && DEMO_EMAILS.includes(email.toLowerCase())) {
    //     console.warn('⚠ DEMO LOGIN ACTIVE → Password bypassed for:', email);

    //     const fakeToken = 'demo-token-' + btoa(email + Date.now());
    //     const fakeUser = {
    //         id: -1,
    //         name: email.split('@')[0],
    //         email,
    //         role: 'EMPLOYEE',
    //         isActive: true
    //     };

    //     localStorage.setItem(TOKEN_KEY, fakeToken);
    //     localStorage.setItem(USER_KEY, JSON.stringify(fakeUser));
    //     api.defaults.headers.common['Authorization'] = `Bearer ${fakeToken}`;

    //     return fakeUser;
    // }
        try {
            const response = await api.post('/auth/login', { email, password });
            // api.post returns response.data which is the ApiResponse wrapper:
            // { success, message, data: { token, tokenType, expiresIn, user } }
            // Extract token and user from response.data.data
            const { token, user } = response.data.data;
            
            // Store token and user info
            localStorage.setItem(TOKEN_KEY, token);
            localStorage.setItem(USER_KEY, JSON.stringify(user));
            
            // Set token in API client for future requests
            api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            
            console.log('Login successful:', user);
            return user;
        } catch (error) {
            console.error('Login failed:', error.response?.data?.message || error.message);
            throw error;
        }
    },
    
    /**
     * Register new user
     */
    async register(userData) {
        try {
            const response = await api.post('/auth/register', userData);
            return response.data.data;
        } catch (error) {
            console.error('Registration failed:', error.response?.data?.message || error.message);
            throw error;
        }
    },
    
    /**
     * Logout user
     */
    logout() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        delete api.defaults.headers.common['Authorization'];
        console.log('User logged out');
    },
    
    /**
     * Get current user from localStorage
     */
    getCurrentUser() {
        const userStr = localStorage.getItem(USER_KEY);
        return userStr ? JSON.parse(userStr) : null;
    },
    
    /**
     * Get JWT token
     */
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },
    
    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return !!this.getToken();
    },
    
    /**
     * Check if user has admin role
     */
    isAdmin() {
        const user = this.getCurrentUser();
        return user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN' || user?.role === 'MANAGER';
    },
    
    /**
     * Check if user is employee/driver
     */
    isEmployee() {
        const user = this.getCurrentUser();
        return user?.role === 'DRIVER' || user?.role === 'EMPLOYEE';
    },
    
    /**
     * Initialize auth (call on app startup)
     * Sets token in API headers if exists
     */
    initialize() {
        const token = this.getToken();
        if (token) {
            api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            console.log('Auth initialized with existing token');
        }
    }
};
