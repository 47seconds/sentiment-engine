import api from './api';
import { authService } from './authService';

/**
 * Helper: Check if user is a demo user
 */
function isDemoUser(user) {
    return user && user.id === -1;
}

/**
 * Profile Service
 * Handles user profile operations
 */
export const profileService = {
    /**
     * Get current user's profile
     */
    async getProfile(userId) {
        const user = authService.getCurrentUser();
        
        // DEMO MODE: User is a fake employee (id: -1)
        if (isDemoUser(user)) {
            console.warn("⚠ DEMO PROFILE ACTIVE → Using placeholder profile for", user.email);
            
            // Extract first and last name from email
            const nameParts = user.name.split('.');
            const firstName = nameParts[0] ? nameParts[0].charAt(0).toUpperCase() + nameParts[0].slice(1) : "John";
            const lastName = nameParts[1] ? nameParts[1].charAt(0).toUpperCase() + nameParts[1].slice(1) : "Employee";
            const fullName = `${firstName} ${lastName}`;
            
            return {
                id: -1,
                name: fullName,
                firstName: firstName,
                lastName: lastName,
                email: user.email,
                phoneNumber: "+91-9876543210",
                department: "Operations",
                role: user.role, // Use role from auth (EMPLOYEE)
                isActive: true,
                createdAt: "2024-01-01T00:00:00.000Z" // Demo join date
            };
        }
        
        // ✅ Normal backend request
        try {
            const response = await api.get(`/users/${userId}`);
            return response.data.data;
        } catch (error) {
            console.error('Failed to fetch profile:', error);
            throw error;
        }
    },

    /**
     * Update user profile
     */
    async updateProfile(userId, profileData) {
        const user = authService.getCurrentUser();
        
        // DEMO MODE: Prevent updates for demo users
        if (isDemoUser(user)) {
            console.warn("⚠ DEMO MODE → Profile updates are disabled for demo users");
            throw new Error("Profile updates are not available in demo mode");
        }
        
        try {
            const response = await api.put(`/users/${userId}`, profileData);
            return response.data.data;
        } catch (error) {
            console.error('Failed to update profile:', error);
            throw error;
        }
    },

    /**
     * Change password
     */
    async changePassword(userId, currentPassword, newPassword) {
        const user = authService.getCurrentUser();
        
        // DEMO MODE: Prevent password changes for demo users
        if (isDemoUser(user)) {
            console.warn("⚠ DEMO MODE → Password changes are disabled for demo users");
            throw new Error("Password changes are not available in demo mode");
        }
        
        try {
            const response = await api.put(`/users/${userId}/password`, {
                currentPassword,
                newPassword
            });
            return response.data;
        } catch (error) {
            console.error('Failed to change password:', error);
            throw error;
        }
    },

    /**
     * Upload profile picture
     */
    async uploadProfilePicture(userId, file) {
        try {
            const formData = new FormData();
            formData.append('file', file);
            
            const response = await api.post(`/users/${userId}/profile-picture`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            return response.data.data;
        } catch (error) {
            console.error('Failed to upload profile picture:', error);
            throw error;
        }
    }
};

export { isDemoUser };
export default profileService;
