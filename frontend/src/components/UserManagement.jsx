import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress,
  Stack,
  Tooltip,
  InputAdornment,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  PersonAdd as PersonAddIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import api from '../services/api';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    name: '',
    phoneNumber: '',
    role: 'EMPLOYEE',
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  // Load users on mount
  useEffect(() => {
    loadUsers();
  }, []);

  // Filter users based on search term
  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredUsers(users);
    } else {
      const filtered = users.filter(user =>
        user.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.role?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.phoneNumber?.includes(searchTerm)
      );
      setFilteredUsers(filtered);
    }
  }, [users, searchTerm]);

  // Load all users
  const loadUsers = async () => {
    try {
      setLoading(true);
      const response = await api.get('/users/all');
      setUsers(response.data?.data || response.data || []);
    } catch (error) {
      console.error('Error loading users:', error);
      toast.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  // Validate form
  const validateForm = () => {
    const errors = {};

    if (!formData.email || !formData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      errors.email = 'Invalid email format';
    }

    if (!formData.password || !formData.password.trim()) {
      errors.password = 'Password is required';
    } else if (formData.password.length < 8) {
      errors.password = 'Password must be at least 8 characters';
    }

    if (!formData.name || !formData.name.trim()) {
      errors.name = 'Name is required';
    }

    if (!formData.role) {
      errors.role = 'Role is required';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle form submit
  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    try {
      setSubmitting(true);

      const payload = {
        email: formData.email.trim(),
        password: formData.password,
        name: formData.name.trim(),
        phoneNumber: formData.phoneNumber?.trim() || null,
        role: formData.role,
      };

      await api.post('/users', payload);
      
      toast.success('User created successfully!');
      setOpenDialog(false);
      resetForm();
      loadUsers();
    } catch (error) {
      console.error('Error creating user:', error);
      const errorMessage = error.response?.data?.message || 'Failed to create user';
      toast.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  // Reset form
  const resetForm = () => {
    setFormData({
      email: '',
      password: '',
      name: '',
      phoneNumber: '',
      role: 'EMPLOYEE',
    });
    setFormErrors({});
    setShowPassword(false);
  };

  // Handle dialog close
  const handleCloseDialog = () => {
    if (!submitting) {
      setOpenDialog(false);
      resetForm();
    }
  };

  // Handle input change
  const handleInputChange = (field) => (event) => {
    setFormData({ ...formData, [field]: event.target.value });
    // Clear error for this field
    if (formErrors[field]) {
      setFormErrors({ ...formErrors, [field]: null });
    }
  };

  // Get role color
  const getRoleColor = (role) => {
    switch (role) {
      case 'ADMIN':
        return 'error';
      case 'MANAGER':
        return 'warning';
      case 'EMPLOYEE':
      case 'DRIVER':
        return 'primary';
      case 'SUPPORT':
        return 'info';
      default:
        return 'default';
    }
  };

  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h6">User Management</Typography>
            <Typography variant="body2" color="text.secondary">
              {loading ? 'Loading users...' : 
                searchTerm ? 
                  `${filteredUsers.length} of ${users.length} users (filtered)` :
                  `${users.length} user${users.length !== 1 ? 's' : ''} total`
              }
            </Typography>
          </Box>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setOpenDialog(true)}
          >
            Create User
          </Button>
        </Box>

        {/* Search Bar */}
        <Box sx={{ mb: 2 }}>
          <TextField
            fullWidth
            label="Search Users"
            placeholder="Search by name, email, role, or phone..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            variant="outlined"
            size="small"
          />
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            {filteredUsers.length > 6 && (
              <Alert severity="info" sx={{ mb: 2 }}>
                Showing {filteredUsers.length} users. The table is scrollable for easier navigation.
              </Alert>
            )}
            <TableContainer 
              component={Paper} 
              variant="outlined"
              sx={{ 
                maxHeight: 400, // Fixed height for scrolling
                overflow: 'auto', // Enable scrolling
                '&::-webkit-scrollbar': {
                  width: '8px',
                },
                '&::-webkit-scrollbar-track': {
                  backgroundColor: '#f1f1f1',
                  borderRadius: '4px',
                },
                '&::-webkit-scrollbar-thumb': {
                  backgroundColor: '#c1c1c1',
                  borderRadius: '4px',
                  '&:hover': {
                    backgroundColor: '#a8a8a8',
                  },
                },
              }}
            >
            <Table stickyHeader> {/* Keep header visible while scrolling */}
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>ID</TableCell>
                  <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Name</TableCell>
                  <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Email</TableCell>
                  <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Phone</TableCell>
                  <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Role</TableCell>
                  <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Status</TableCell>
                  <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Created</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredUsers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
                        {searchTerm ? 'No users found matching your search' : 'No users found'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredUsers.map((user) => (
                    <TableRow key={user.id} hover>
                      <TableCell>{user.id}</TableCell>
                      <TableCell>{user.name}</TableCell>
                      <TableCell>{user.email}</TableCell>
                      <TableCell>{user.phoneNumber || '-'}</TableCell>
                      <TableCell>
                        <Chip
                          label={user.role}
                          size="small"
                          color={getRoleColor(user.role)}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          icon={user.active ? <CheckCircleIcon /> : <CancelIcon />}
                          label={user.active ? 'Active' : 'Inactive'}
                          size="small"
                          color={user.active ? 'success' : 'default'}
                        />
                      </TableCell>
                      <TableCell>
                        {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-'}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
          </>
        )}

        {/* Create User Dialog */}
        <Dialog 
          open={openDialog} 
          onClose={handleCloseDialog}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PersonAddIcon color="primary" />
              <Typography variant="h6">Create New User</Typography>
            </Box>
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2.5} sx={{ mt: 2 }}>
              <Alert severity="info">
                Create a new user account. The user will be able to login with the provided credentials.
              </Alert>

              <TextField
                fullWidth
                label="Full Name"
                value={formData.name}
                onChange={handleInputChange('name')}
                error={!!formErrors.name}
                helperText={formErrors.name}
                required
                autoFocus
              />

              <TextField
                fullWidth
                label="Email"
                type="email"
                value={formData.email}
                onChange={handleInputChange('email')}
                error={!!formErrors.email}
                helperText={formErrors.email}
                required
              />

              <TextField
                fullWidth
                label="Password"
                type={showPassword ? 'text' : 'password'}
                value={formData.password}
                onChange={handleInputChange('password')}
                error={!!formErrors.password}
                helperText={formErrors.password || 'Minimum 8 characters'}
                required
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => setShowPassword(!showPassword)}
                        edge="end"
                      >
                        {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />

              <TextField
                fullWidth
                label="Phone Number"
                value={formData.phoneNumber}
                onChange={handleInputChange('phoneNumber')}
                helperText="Optional - Format: +91-XXXXXXXXXX"
              />

              <FormControl fullWidth required error={!!formErrors.role}>
                <InputLabel>Role</InputLabel>
                <Select
                  value={formData.role}
                  onChange={handleInputChange('role')}
                  label="Role"
                >
                  <MenuItem value="EMPLOYEE">EMPLOYEE (Driver/Staff)</MenuItem>
                  <MenuItem value="MANAGER">MANAGER</MenuItem>
                  <MenuItem value="ADMIN">ADMIN</MenuItem>
                  <MenuItem value="SUPPORT">SUPPORT</MenuItem>
                  <MenuItem value="ANALYST">ANALYST</MenuItem>
                </Select>
                {formErrors.role && (
                  <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 2 }}>
                    {formErrors.role}
                  </Typography>
                )}
              </FormControl>
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={handleCloseDialog} disabled={submitting}>
              Cancel
            </Button>
            <Button
              onClick={handleSubmit}
              variant="contained"
              disabled={submitting}
              startIcon={submitting ? <CircularProgress size={20} /> : <PersonAddIcon />}
            >
              {submitting ? 'Creating...' : 'Create User'}
            </Button>
          </DialogActions>
        </Dialog>
      </CardContent>
    </Card>
  );
};

export default UserManagement;
