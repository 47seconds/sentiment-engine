import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box,
    Card,
    CardContent,
    TextField,
    Button,
    Typography,
    Alert,
    InputAdornment,
    IconButton,
    Link as MuiLink,
    useTheme
} from '@mui/material';
import {
    Visibility,
    VisibilityOff,
    Email as EmailIcon,
    Lock as LockIcon,
    Person as PersonIcon,
    Phone as PhoneIcon,
    AdminPanelSettings as AdminIcon
} from '@mui/icons-material';
import { authService } from '../services/authService';
import toast from 'react-hot-toast';

/**
 * Admin Registration Page
 * Allows new admins to register themselves in the system
 */
export const AdminRegisterPage = () => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        phoneNumber: ''
    });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    
    const navigate = useNavigate();
    const theme = useTheme();
    
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // Clear error when user starts typing
        if (error) setError('');
    };
    
    const validateForm = () => {
        if (!formData.name.trim()) {
            setError('Name is required');
            return false;
        }
        if (!formData.email.trim()) {
            setError('Email is required');
            return false;
        }
        if (!formData.email.includes('@')) {
            setError('Please enter a valid email address');
            return false;
        }
        if (formData.password.length < 8) {
            setError('Password must be at least 8 characters long');
            return false;
        }
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return false;
        }
        return true;
    };
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }
        
        setError('');
        setLoading(true);
        
        try {
            // Prepare registration data with ADMIN role
            const registrationData = {
                name: formData.name.trim(),
                email: formData.email.trim().toLowerCase(),
                password: formData.password,
                phoneNumber: formData.phoneNumber.trim() || null,
                role: 'ADMIN' // Set role as ADMIN
            };
            
            await authService.register(registrationData);
            
            toast.success('Admin account created successfully! Please login.');
            navigate('/login');
            
        } catch (err) {
            console.error('Registration failed:', err);
            const errorMessage = err.response?.data?.message || 
                               err.response?.data?.error || 
                               'Registration failed. Please try again.';
            setError(errorMessage);
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    };
    
    return (
        <Box
            sx={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: theme.palette.mode === 'dark' 
                    ? 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)'
                    : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                p: 2
            }}
        >
            <Card 
                sx={{ 
                    maxWidth: 500, 
                    width: '100%',
                    boxShadow: theme.shadows[16]
                }}
            >
                <CardContent sx={{ p: 4 }}>
                    {/* Logo and Title */}
                    <Box textAlign="center" mb={4}>
                        <Box
                            sx={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                width: 80,
                                height: 80,
                                borderRadius: '50%',
                                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                mb: 2
                            }}
                        >
                            <AdminIcon sx={{ fontSize: 48, color: 'white' }} />
                        </Box>
                        <Typography variant="h4" fontWeight={700} gutterBottom>
                            Admin Registration
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Create a new admin account for Sentiment Engine
                        </Typography>
                    </Box>
                    
                    {/* Error Alert */}
                    {error && (
                        <Alert severity="error" sx={{ mb: 3 }}>
                            {error}
                        </Alert>
                    )}
                    
                    {/* Registration Form */}
                    <form onSubmit={handleSubmit}>
                        <TextField
                            fullWidth
                            label="Full Name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                            autoComplete="name"
                            autoFocus
                            sx={{ mb: 2 }}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <PersonIcon color="action" />
                                    </InputAdornment>
                                ),
                            }}
                        />
                        
                        <TextField
                            fullWidth
                            label="Email Address"
                            name="email"
                            type="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                            autoComplete="email"
                            sx={{ mb: 2 }}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <EmailIcon color="action" />
                                    </InputAdornment>
                                ),
                            }}
                        />
                        
                        <TextField
                            fullWidth
                            label="Phone Number (Optional)"
                            name="phoneNumber"
                            value={formData.phoneNumber}
                            onChange={handleChange}
                            autoComplete="tel"
                            sx={{ mb: 2 }}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <PhoneIcon color="action" />
                                    </InputAdornment>
                                ),
                            }}
                        />
                        
                        <TextField
                            fullWidth
                            label="Password"
                            name="password"
                            type={showPassword ? 'text' : 'password'}
                            value={formData.password}
                            onChange={handleChange}
                            required
                            autoComplete="new-password"
                            sx={{ mb: 2 }}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <LockIcon color="action" />
                                    </InputAdornment>
                                ),
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={() => setShowPassword(!showPassword)}
                                            edge="end"
                                        >
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />
                        
                        <TextField
                            fullWidth
                            label="Confirm Password"
                            name="confirmPassword"
                            type={showConfirmPassword ? 'text' : 'password'}
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            required
                            autoComplete="new-password"
                            sx={{ mb: 3 }}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <LockIcon color="action" />
                                    </InputAdornment>
                                ),
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                            edge="end"
                                        >
                                            {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />
                        
                        <Button
                            fullWidth
                            type="submit"
                            variant="contained"
                            size="large"
                            disabled={loading}
                            sx={{
                                py: 1.5,
                                fontSize: '1rem',
                                fontWeight: 600,
                                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                '&:hover': {
                                    background: 'linear-gradient(135deg, #5568d3 0%, #63408b 100%)',
                                }
                            }}
                        >
                            {loading ? 'Creating Account...' : 'Create Admin Account'}
                        </Button>
                    </form>
                    
                    {/* Login Link */}
                    <Box mt={3} textAlign="center">
                        <Typography variant="body2" color="text.secondary">
                            Already have an account?{' '}
                            <MuiLink
                                component="button"
                                variant="body2"
                                onClick={() => navigate('/login')}
                                sx={{
                                    fontWeight: 600,
                                    textDecoration: 'none',
                                    cursor: 'pointer',
                                    '&:hover': {
                                        textDecoration: 'underline'
                                    }
                                }}
                            >
                                Login here
                            </MuiLink>
                        </Typography>
                    </Box>
                    
                    {/* Info Box */}
                    <Box
                        mt={4}
                        p={2}
                        sx={{
                            borderRadius: 1,
                            bgcolor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)',
                            border: `1px solid ${theme.palette.divider}`
                        }}
                    >
                        <Typography variant="caption" color="text.secondary" fontWeight={600} display="block" mb={1}>
                            ADMIN REGISTRATION INFO:
                        </Typography>
                        <Typography variant="caption" display="block" sx={{ mb: 0.5 }}>
                            • Admin accounts have full system access
                        </Typography>
                        <Typography variant="caption" display="block" sx={{ mb: 0.5 }}>
                            • Password must be at least 8 characters
                        </Typography>
                        <Typography variant="caption" display="block">
                            • You can manage users, view all feedback, and access analytics
                        </Typography>
                    </Box>
                </CardContent>
            </Card>
        </Box>
    );
};