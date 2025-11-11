import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
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
    TrendingUp as TrendingUpIcon
} from '@mui/icons-material';

/**
 * Login Page
 * Allows users to authenticate and access the system
 */
export const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    
    const { login, isAdmin } = useAuth();
    const navigate = useNavigate();
    const theme = useTheme();
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        
        try {
            console.log('🔑 LoginPage: Attempting login with email:', email);
            console.log('🔑 LoginPage: Password length:', password.length);
            
            await login(email, password);
            
            console.log('🔑 LoginPage: Login successful, checking role...');
            
            // Redirect based on role
            if (isAdmin()) {
                console.log('🔑 LoginPage: User is admin, redirecting to dashboard');
                navigate('/dashboard');
            } else {
                console.log('🔑 LoginPage: User is not admin, redirecting to feedback');
                navigate('/feedback');
            }
        } catch (err) {
            console.error('🔑 LoginPage: Login failed:', err);
            console.error('🔑 LoginPage: Error response:', err.response?.data);
            setError(err.response?.data?.message || 'Invalid email or password');
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
                    maxWidth: 450, 
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
                            <TrendingUpIcon sx={{ fontSize: 48, color: 'white' }} />
                        </Box>
                        <Typography variant="h4" fontWeight={700} gutterBottom>
                            Sentiment Engine
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Driver Feedback Analytics Platform
                        </Typography>
                    </Box>
                    
                    {/* Error Alert */}
                    {error && (
                        <Alert severity="error" sx={{ mb: 3 }}>
                            {error}
                        </Alert>
                    )}
                    
                    {/* Login Form */}
                    <form onSubmit={handleSubmit}>
                        <TextField
                            fullWidth
                            label="Email Address"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            autoComplete="email"
                            autoFocus
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
                            label="Password"
                            type={showPassword ? 'text' : 'password'}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            autoComplete="current-password"
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
                                            onClick={() => setShowPassword(!showPassword)}
                                            edge="end"
                                        >
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
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
                            {loading ? 'Signing In...' : 'Sign In'}
                        </Button>
                    </form>
                    
                    {/* Register Link */}
                    <Box mt={3} textAlign="center">
                        <Typography variant="body2" color="text.secondary">
                            Need admin access?{' '}
                            <MuiLink
                                component="button"
                                variant="body2"
                                onClick={() => navigate('/admin-register')}
                                sx={{
                                    fontWeight: 600,
                                    textDecoration: 'none',
                                    cursor: 'pointer',
                                    '&:hover': {
                                        textDecoration: 'underline'
                                    }
                                }}
                            >
                                Register as Admin
                            </MuiLink>
                        </Typography>
                    </Box>
                    
                    {/* Demo Credentials */}
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
                            DEMO CREDENTIALS:
                        </Typography>
                        <Typography variant="caption" display="block" sx={{ fontFamily: 'monospace' }}>
                            <strong>Admin:</strong> admin@moveinsync.com / admin123
                        </Typography>
                        <Typography variant="caption" display="block" sx={{ fontFamily: 'monospace' }}>
                            <strong>User:</strong> john.employee@moveinsync.com / password123
                        </Typography>
                        <Typography variant="caption" display="block" sx={{ fontFamily: 'monospace' }}>
                            <strong>User:</strong> mary.employee@moveinsync.com / password123
                        </Typography>
                    </Box>
                </CardContent>
            </Card>
        </Box>
    );
};
