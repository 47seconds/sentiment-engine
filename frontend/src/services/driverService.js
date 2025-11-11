import { api } from './api';
import { authService } from './authService';

/**
 * Driver Service
 * Handles driver management operations (CRUD operations)
 */

// Helper: Check if user is a demo user
function isDemoUser(user) {
    return user && user.id === -1;
}

// Demo storage for newly created drivers in demo mode
let demoDrivers = [];
let nextDemoId = 1001;

/**
 * Add a new driver
 */
export const addDriver = async (driverData) => {
    const user = authService.getCurrentUser();
    
    // DEMO MODE: Store driver locally
    if (isDemoUser(user)) {
        console.warn("⚠ DEMO MODE → Adding driver locally");
        const newDriver = {
            id: nextDemoId++,
            driverId: nextDemoId,
            name: driverData.name,
            email: driverData.email,
            phoneNumber: driverData.phoneNumber,
            licenseNumber: driverData.licenseNumber,
            vehicleNumber: driverData.vehicleNumber,
            role: 'EMPLOYEE',
            isActive: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
        };
        demoDrivers.push(newDriver);
        return { data: newDriver };
    }
    
    // For regular users, send to backend
    try {
        return await api.post('/users/drivers', {
            ...driverData,
            password: generateDefaultPassword(driverData.name) // Generate a default password
        });
    } catch (error) {
        console.error('Failed to add driver:', error);
        throw error;
    }
};

/**
 * Get all drivers
 */
export const getAllDrivers = async () => {
    const user = authService.getCurrentUser();
    
    // DEMO MODE: Return demo drivers
    if (isDemoUser(user)) {
        console.warn("⚠ DEMO MODE → Returning demo drivers");
        return { data: demoDrivers };
    }
    
    try {
        // Use the actual-drivers endpoint to get only real drivers, not all employees
        return await api.get('/users/actual-drivers');
    } catch (error) {
        console.warn("⚠ Backend unavailable, returning empty list");
        return { data: [] };
    }
};

/**
 * Update driver information
 */
export const updateDriver = async (driverId, driverData) => {
    const user = authService.getCurrentUser();
    
    // DEMO MODE: Update driver locally
    if (isDemoUser(user)) {
        console.warn("⚠ DEMO MODE → Updating driver locally");
        const driverIndex = demoDrivers.findIndex(d => d.id === driverId);
        if (driverIndex !== -1) {
            demoDrivers[driverIndex] = {
                ...demoDrivers[driverIndex],
                ...driverData,
                updatedAt: new Date().toISOString()
            };
            return { data: demoDrivers[driverIndex] };
        }
        throw new Error('Driver not found');
    }
    
    return await api.put(`/users/${driverId}`, driverData);
};

/**
 * Deactivate driver
 */
export const deactivateDriver = async (driverId) => {
    const user = authService.getCurrentUser();
    
    // DEMO MODE: Deactivate driver locally
    if (isDemoUser(user)) {
        console.warn("⚠ DEMO MODE → Deactivating driver locally");
        const driverIndex = demoDrivers.findIndex(d => d.id === driverId);
        if (driverIndex !== -1) {
            demoDrivers[driverIndex].isActive = false;
            demoDrivers[driverIndex].updatedAt = new Date().toISOString();
            return { data: demoDrivers[driverIndex] };
        }
        throw new Error('Driver not found');
    }
    
    return await api.put(`/users/${driverId}/deactivate`);
};

/**
 * Activate driver
 */
export const activateDriver = async (driverId) => {
    const user = authService.getCurrentUser();
    
    // DEMO MODE: Activate driver locally
    if (isDemoUser(user)) {
        console.warn("⚠ DEMO MODE → Activating driver locally");
        const driverIndex = demoDrivers.findIndex(d => d.id === driverId);
        if (driverIndex !== -1) {
            demoDrivers[driverIndex].isActive = true;
            demoDrivers[driverIndex].updatedAt = new Date().toISOString();
            return { data: demoDrivers[driverIndex] };
        }
        throw new Error('Driver not found');
    }
    
    return await api.put(`/users/${driverId}/activate`);
};

/**
 * Delete driver
 */
export const deleteDriver = async (driverId) => {
    const user = authService.getCurrentUser();
    
    // DEMO MODE: Delete driver locally
    if (isDemoUser(user)) {
        console.warn("⚠ DEMO MODE → Deleting driver locally");
        const driverIndex = demoDrivers.findIndex(d => d.id === driverId);
        if (driverIndex !== -1) {
            demoDrivers.splice(driverIndex, 1);
            return { success: true };
        }
        throw new Error('Driver not found');
    }
    
    return await api.delete(`/users/${driverId}`);
};

/**
 * Generate default password for new drivers
 */
function generateDefaultPassword(name) {
    // Simple default password generation
    // In production, this should be more sophisticated
    const namePart = name.replace(/\s+/g, '').toLowerCase();
    const numberPart = Math.floor(1000 + Math.random() * 9000);
    return `${namePart}${numberPart}`;
}

/**
 * Check if a driver ID exists in the database
 */
export const validateDriverId = async (driverId) => {
    const user = authService.getCurrentUser();
    
    // DEMO MODE: Check if driver exists in demo data
    if (isDemoUser(user)) {
        console.warn("⚠ DEMO MODE → Validating driver locally");
        const driverExists = demoDrivers.some(d => d.driverId == driverId && d.isActive);
        // Also check for some default valid IDs in demo mode
        const defaultValidIds = [21, 22, 23, 24, 25, 1001, 1002, 1003];
        return { 
            exists: driverExists || defaultValidIds.includes(parseInt(driverId)),
            data: { valid: driverExists || defaultValidIds.includes(parseInt(driverId)) }
        };
    }
    
    try {
        // Check if driver exists in backend
        const response = await api.get(`/users/drivers/${driverId}/validate`);
        return { exists: true, data: response.data };
    } catch (error) {
        if (error.response?.status === 404) {
            return { exists: false, data: { valid: false } };
        }
        // If there's a server error, we'll allow the submission but warn
        console.warn("⚠ Could not validate driver ID due to server error");
        return { exists: true, data: { valid: true } }; // Assume valid if can't check
    }
};

/**
 * Get demo drivers for testing
 */
export const getDemoDrivers = () => {
    return demoDrivers;
};

/**
 * Reset demo drivers (for testing)
 */
export const resetDemoDrivers = () => {
    demoDrivers = [];
    nextDemoId = 1001;
};

export default {
    addDriver,
    getAllDrivers,
    updateDriver,
    deactivateDriver,
    activateDriver,
    deleteDriver,
    validateDriverId,
    getDemoDrivers,
    resetDemoDrivers
};