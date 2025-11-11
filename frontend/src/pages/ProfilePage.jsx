import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Avatar,
  Button,
  TextField,
  Divider,
  Stack,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
} from '@mui/material';
import {
  AccountCircle as ProfileIcon,
  Edit as EditIcon,
  PhotoCamera as CameraIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Lock as LockIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Business as BusinessIcon,
  CalendarToday as CalendarIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import PageTransition from '../components/PageTransition';
import { useAuth } from '../contexts/AuthContext';
import { profileService } from '../services/profileService';

const ProfilePage = () => {
  const { user, updateUser } = useAuth();
  
  // User data state
  const [userData, setUserData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    role: '',
    department: '',
    joinDate: '',
    avatar: null,
  });

  // Original data for reset
  const [originalData, setOriginalData] = useState({ ...userData });

  // UI state
  const [editMode, setEditMode] = useState(false);
  const [loading, setLoading] = useState(true);
  const [passwordDialogOpen, setPasswordDialogOpen] = useState(false);
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [saving, setSaving] = useState(false);

  // Load user profile on mount
  useEffect(() => {
    const loadProfile = async () => {
      if (!user?.id) return;
      
      try {
        setLoading(true);
        const profile = await profileService.getProfile(user.id);
        
        // Split name into first and last name
        const nameParts = profile.name?.split(' ') || ['', ''];
        const firstName = nameParts[0] || '';
        const lastName = nameParts.slice(1).join(' ') || '';
        
        const profileData = {
          firstName,
          lastName,
          email: profile.email || '',
          phone: profile.phoneNumber || '',
          role: formatRole(profile.role),
          department: 'Operations', // Default or from profile if available
          joinDate: profile.createdAt ? new Date(profile.createdAt).toISOString().split('T')[0] : '',
          avatar: profile.profilePictureUrl || null,
        };
        
        setUserData(profileData);
        setOriginalData(profileData);
      } catch (error) {
        console.error('Error loading profile:', error);
        toast.error('Failed to load profile data');
      } finally {
        setLoading(false);
      }
    };
    
    loadProfile();
  }, [user?.id]);

  // Format role for display
  const formatRole = (role) => {
    const roleMap = {
      'DRIVER': 'Driver',
      'ADMIN': 'Administrator',
      'MANAGER': 'Manager',
      'SUPPORT': 'Support Staff',
      'ANALYST': 'Data Analyst',
      'EMPLOYEE': 'Employee'
    };
    return roleMap[role] || role;
  };

  // Handle profile edit
  const handleEditToggle = () => {
    if (editMode) {
      // Cancel editing - revert changes
      setUserData({ ...originalData });
    }
    setEditMode(!editMode);
  };

  // Handle input change
  const handleInputChange = (field) => (event) => {
    setUserData({ ...userData, [field]: event.target.value });
  };

  // Handle profile save
  const handleSaveProfile = async () => {
    if (!user?.id) {
      toast.error('User not found');
      return;
    }

    try {
      setSaving(true);
      
      // Prepare update payload
      const fullName = `${userData.firstName} ${userData.lastName}`.trim();
      const updatePayload = {
        name: fullName,
        email: userData.email,
        phoneNumber: userData.phone,
      };
      
      // Call API to update profile
      const updatedProfile = await profileService.updateProfile(user.id, updatePayload);
      
      // Update local state
      setOriginalData({ ...userData });
      setEditMode(false);
      
      // Update user in AuthContext
      updateUser({
        name: updatedProfile.name,
        email: updatedProfile.email,
        phoneNumber: updatedProfile.phoneNumber,
      });
      
      toast.success('Profile updated successfully!');
      
    } catch (error) {
      console.error('Error saving profile:', error);
      toast.error(error.response?.data?.message || 'Failed to update profile');
      // Revert changes on error
      setUserData({ ...originalData });
    } finally {
      setSaving(false);
    }
  };

  // Handle avatar upload
  const handleAvatarChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        toast.error('Image size must be less than 2MB');
        return;
      }
      
      const reader = new FileReader();
      reader.onloadend = () => {
        setUserData({ ...userData, avatar: reader.result });
      };
      reader.readAsDataURL(file);
      toast.success('Avatar updated! Don\'t forget to save changes.');
    }
  };

  // Handle password change
  const handlePasswordChange = async () => {
    if (!user?.id) {
      toast.error('User not found');
      return;
    }

    // Validation
    if (!passwordData.currentPassword || !passwordData.newPassword || !passwordData.confirmPassword) {
      toast.error('Please fill in all password fields');
      return;
    }
    
    if (passwordData.newPassword.length < 8) {
      toast.error('New password must be at least 8 characters');
      return;
    }
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error('New passwords do not match');
      return;
    }

    try {
      setSaving(true);
      
      await profileService.changePassword(
        user.id,
        passwordData.currentPassword,
        passwordData.newPassword
      );
      
      setPasswordDialogOpen(false);
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
      toast.success('Password changed successfully!');
      
    } catch (error) {
      console.error('Error changing password:', error);
      toast.error(error.response?.data?.message || 'Failed to change password. Please check your current password.');
    } finally {
      setSaving(false);
    }
  };

  // Get initials for avatar
  const getInitials = () => {
    return `${userData.firstName[0]}${userData.lastName[0]}`;
  };

  return (
    <PageTransition>
    <Box>
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
          <Typography>Loading profile...</Typography>
        </Box>
      ) : (
        <>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <ProfileIcon sx={{ fontSize: 40, color: 'primary.main' }} />
          <Box>
            <Typography variant="h4" gutterBottom>
              My Profile
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Manage your personal information and preferences
            </Typography>
          </Box>
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* Left Column: Profile Card */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 3 }}>
                {/* Avatar */}
                <Box sx={{ position: 'relative', mb: 2 }}>
                  <Avatar
                    src={userData.avatar}
                    sx={{
                      width: 120,
                      height: 120,
                      fontSize: '2.5rem',
                      bgcolor: 'primary.main',
                    }}
                  >
                    {!userData.avatar && getInitials()}
                  </Avatar>
                  
                  {editMode && (
                    <IconButton
                      component="label"
                      sx={{
                        position: 'absolute',
                        bottom: 0,
                        right: 0,
                        bgcolor: 'primary.main',
                        color: 'white',
                        '&:hover': { bgcolor: 'primary.dark' },
                      }}
                      size="small"
                    >
                      <CameraIcon fontSize="small" />
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={handleAvatarChange}
                      />
                    </IconButton>
                  )}
                </Box>

                {/* User Info */}
                <Typography variant="h5" gutterBottom>
                  {userData.firstName} {userData.lastName}
                </Typography>
                <Chip
                  label={userData.role}
                  color="primary"
                  size="small"
                  sx={{ mb: 1 }}
                />
                <Typography variant="body2" color="text.secondary">
                  {userData.department}
                </Typography>

                <Divider sx={{ width: '100%', my: 2 }} />

                {/* Quick Stats */}
                <Box sx={{ width: '100%' }}>
                  <Stack spacing={1.5}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <EmailIcon fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary" sx={{ wordBreak: 'break-all' }}>
                        {userData.email}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <PhoneIcon fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary">
                        {userData.phone}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CalendarIcon fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary">
                        Joined {new Date(userData.joinDate).toLocaleDateString('en-US', { 
                          month: 'long', 
                          year: 'numeric' 
                        })}
                      </Typography>
                    </Box>
                  </Stack>
                </Box>

                <Divider sx={{ width: '100%', my: 2 }} />

                {/* Actions */}
                <Stack spacing={1} sx={{ width: '100%' }}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<LockIcon />}
                    onClick={() => setPasswordDialogOpen(true)}
                  >
                    Change Password
                  </Button>
                </Stack>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Right Column: Edit Form */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h6">Personal Information</Typography>
                <Box>
                  {editMode ? (
                    <Stack direction="row" spacing={1}>
                      <Button
                        variant="outlined"
                        startIcon={<CancelIcon />}
                        onClick={handleEditToggle}
                        disabled={saving}
                      >
                        Cancel
                      </Button>
                      <Button
                        variant="contained"
                        startIcon={saving ? null : <SaveIcon />}
                        onClick={handleSaveProfile}
                        disabled={saving}
                      >
                        {saving ? 'Saving...' : 'Save Changes'}
                      </Button>
                    </Stack>
                  ) : (
                    <Button
                      variant="contained"
                      startIcon={<EditIcon />}
                      onClick={handleEditToggle}
                    >
                      Edit Profile
                    </Button>
                  )}
                </Box>
              </Box>

              <Grid container spacing={3}>
                {/* First Name */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="First Name"
                    value={userData.firstName}
                    onChange={handleInputChange('firstName')}
                    disabled={!editMode}
                    variant={editMode ? 'outlined' : 'filled'}
                  />
                </Grid>

                {/* Last Name */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Last Name"
                    value={userData.lastName}
                    onChange={handleInputChange('lastName')}
                    disabled={!editMode}
                    variant={editMode ? 'outlined' : 'filled'}
                  />
                </Grid>

                {/* Email */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Email"
                    type="email"
                    value={userData.email}
                    onChange={handleInputChange('email')}
                    disabled={!editMode}
                    variant={editMode ? 'outlined' : 'filled'}
                    helperText={editMode ? 'Changing email may require verification' : ''}
                  />
                </Grid>

                {/* Phone */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Phone Number"
                    value={userData.phone}
                    onChange={handleInputChange('phone')}
                    disabled={!editMode}
                    variant={editMode ? 'outlined' : 'filled'}
                  />
                </Grid>

                {/* Department */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Department"
                    value={userData.department}
                    onChange={handleInputChange('department')}
                    disabled={!editMode}
                    variant={editMode ? 'outlined' : 'filled'}
                  />
                </Grid>

                {/* Role (Read-only) */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Role"
                    value={userData.role}
                    disabled
                    variant="filled"
                    helperText="Contact admin to change role"
                  />
                </Grid>

                {/* Join Date (Read-only) */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Join Date"
                    value={userData.joinDate}
                    disabled
                    variant="filled"
                    type="date"
                  />
                </Grid>
              </Grid>

              {editMode && (
                <Alert severity="info" sx={{ mt: 3 }}>
                  Make sure to save your changes before leaving this page.
                </Alert>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Change Password Dialog */}
      <Dialog
        open={passwordDialogOpen}
        onClose={() => !saving && setPasswordDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <LockIcon color="primary" />
            Change Password
          </Box>
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              fullWidth
              label="Current Password"
              type="password"
              value={passwordData.currentPassword}
              onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
              disabled={saving}
            />
            <TextField
              fullWidth
              label="New Password"
              type="password"
              value={passwordData.newPassword}
              onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
              disabled={saving}
              helperText="Must be at least 8 characters"
            />
            <TextField
              fullWidth
              label="Confirm New Password"
              type="password"
              value={passwordData.confirmPassword}
              onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
              disabled={saving}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPasswordDialogOpen(false)} disabled={saving}>
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={handlePasswordChange}
            disabled={saving}
          >
            {saving ? 'Changing...' : 'Change Password'}
          </Button>
        </DialogActions>
      </Dialog>
        </>
      )}
    </Box>
    </PageTransition>
  );
};

export default ProfilePage;
