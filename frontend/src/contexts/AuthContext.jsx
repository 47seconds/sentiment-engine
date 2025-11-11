import { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

/**
 * Hook to use auth context
 */
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};

/**
 * Auth Provider Component
 * Manages global authentication state
 */
export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    
    // Initialize auth on mount
    useEffect(() => {
        authService.initialize();
        const currentUser = authService.getCurrentUser();
        setUser(currentUser);
        setLoading(false);
    }, []);
    
    /**
     * Login user
     */
    const login = async (email, password) => {
        const user = await authService.login(email, password);
        setUser(user);
        return user;
    };
    
    /**
     * Logout user
     */
    const logout = () => {
        authService.logout();
        setUser(null);
    };
    
    /**
     * Check if user has admin role
     */
    const isAdmin = () => {
        return user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN' || user?.role === 'MANAGER';
    };
    
    /**
     * Check if user is employee/driver
     */
    const isEmployee = () => {
        return user?.role === 'DRIVER' || user?.role === 'EMPLOYEE';
    };
    
    /**
     * Update user data in context and localStorage
     */
    const updateUser = (updatedUserData) => {
        const updatedUser = { ...user, ...updatedUserData };
        setUser(updatedUser);
        localStorage.setItem('user_info', JSON.stringify(updatedUser));
    };
    
    const value = {
        user,
        loading,
        login,
        logout,
        isAdmin,
        isEmployee,
        updateUser,
        isAuthenticated: !!user
    };
    
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};
