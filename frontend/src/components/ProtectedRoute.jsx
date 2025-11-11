import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Box, CircularProgress } from '@mui/material';

/**
 * Protected Route Component
 * Restricts access to authenticated users only
 * Optional: Can restrict to admin-only routes
 */
export const ProtectedRoute = ({ children, adminOnly = false }) => {
    const { isAuthenticated, isAdmin, loading } = useAuth();
    
    // Show loading spinner while checking auth
    if (loading) {
        return (
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    minHeight: '100vh'
                }}
            >
                <CircularProgress />
            </Box>
        );
    }
    
    // Redirect to login if not authenticated
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }
    
    // Redirect to feedback page if user is not admin but trying to access admin route
    if (adminOnly && !isAdmin()) {
        return <Navigate to="/feedback" replace />;
    }
    
    return children;
};
