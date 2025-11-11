import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Button,
  Divider,
  Stack,
  Alert,
  Chip,
} from '@mui/material';
import {
  Settings as SettingsIcon,
  Notifications as NotificationsIcon,
  Language as LanguageIcon,
  Schedule as ScheduleIcon,
  Palette as PaletteIcon,
  Save as SaveIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import PageTransition from '../components/PageTransition';

const SettingsPage = () => {
  // Settings state
  const [settings, setSettings] = useState({
    // Appearance
    theme: 'system', // light, dark, system
    language: 'en',
    timezone: 'Asia/Kolkata',
    dateFormat: 'DD/MM/YYYY',
    
    // Notifications
    emailNotifications: true,
    pushNotifications: false,
    alertNotifications: true,
    feedbackNotifications: true,
    weeklyReports: true,
    
    // Data & Display
    itemsPerPage: 25,
    autoRefresh: true,
    refreshInterval: 30, // seconds
    showEmptyStates: true,
    
    // Privacy
    shareAnalytics: true,
    activityTracking: true,
  });

  // Original settings for reset
  const [originalSettings, setOriginalSettings] = useState({ ...settings });
  const [hasChanges, setHasChanges] = useState(false);
  const [saving, setSaving] = useState(false);

  // Handle setting change
  const handleSettingChange = (field) => (event) => {
    const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
    const newSettings = { ...settings, [field]: value };
    setSettings(newSettings);
    setHasChanges(JSON.stringify(newSettings) !== JSON.stringify(originalSettings));
  };

  // Handle save
  const handleSave = async () => {
    try {
      setSaving(true);
      
      // TODO: Replace with actual API call
      // await api.put('/users/settings', settings);
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      setOriginalSettings({ ...settings });
      setHasChanges(false);
      toast.success('Settings saved successfully!');
      
    } catch (error) {
      console.error('Error saving settings:', error);
      toast.error('Failed to save settings');
    } finally {
      setSaving(false);
    }
  };

  // Handle reset
  const handleReset = () => {
    setSettings({ ...originalSettings });
    setHasChanges(false);
    toast.success('Settings reset to last saved state');
  };

  return (
    <PageTransition>
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <SettingsIcon sx={{ fontSize: 40, color: 'primary.main' }} />
          <Box>
            <Typography variant="h4" gutterBottom>
              Application Settings
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Customize your experience and preferences
            </Typography>
          </Box>
        </Box>
        
        {hasChanges && (
          <Chip
            label="Unsaved Changes"
            color="warning"
            variant="outlined"
          />
        )}
      </Box>

      {/* Warning Alert */}
      {hasChanges && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          You have unsaved changes. Click "Save Settings" to apply them.
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Appearance Settings */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                <PaletteIcon color="primary" />
                <Typography variant="h6">Appearance</Typography>
              </Box>

              <Stack spacing={3}>
                <FormControl fullWidth>
                  <InputLabel>Theme</InputLabel>
                  <Select
                    value={settings.theme}
                    label="Theme"
                    onChange={handleSettingChange('theme')}
                  >
                    <MenuItem value="light">Light</MenuItem>
                    <MenuItem value="dark">Dark</MenuItem>
                    <MenuItem value="system">System Default</MenuItem>
                  </Select>
                </FormControl>

                <FormControl fullWidth>
                  <InputLabel>Language</InputLabel>
                  <Select
                    value={settings.language}
                    label="Language"
                    onChange={handleSettingChange('language')}
                  >
                    <MenuItem value="en">English</MenuItem>
                    <MenuItem value="hi">हिन्दी (Hindi)</MenuItem>
                    <MenuItem value="kn">ಕನ್ನಡ (Kannada)</MenuItem>
                    <MenuItem value="ta">தமிழ் (Tamil)</MenuItem>
                  </Select>
                </FormControl>

                <FormControl fullWidth>
                  <InputLabel>Date Format</InputLabel>
                  <Select
                    value={settings.dateFormat}
                    label="Date Format"
                    onChange={handleSettingChange('dateFormat')}
                  >
                    <MenuItem value="DD/MM/YYYY">DD/MM/YYYY</MenuItem>
                    <MenuItem value="MM/DD/YYYY">MM/DD/YYYY</MenuItem>
                    <MenuItem value="YYYY-MM-DD">YYYY-MM-DD</MenuItem>
                  </Select>
                </FormControl>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Regional Settings */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                <ScheduleIcon color="primary" />
                <Typography variant="h6">Regional Settings</Typography>
              </Box>

              <Stack spacing={3}>
                <FormControl fullWidth>
                  <InputLabel>Timezone</InputLabel>
                  <Select
                    value={settings.timezone}
                    label="Timezone"
                    onChange={handleSettingChange('timezone')}
                  >
                    <MenuItem value="Asia/Kolkata">Asia/Kolkata (IST, GMT+5:30)</MenuItem>
                    <MenuItem value="America/New_York">America/New York (EST, GMT-5)</MenuItem>
                    <MenuItem value="Europe/London">Europe/London (GMT)</MenuItem>
                    <MenuItem value="Asia/Singapore">Asia/Singapore (SGT, GMT+8)</MenuItem>
                  </Select>
                </FormControl>

                <FormControl fullWidth>
                  <InputLabel>Items Per Page</InputLabel>
                  <Select
                    value={settings.itemsPerPage}
                    label="Items Per Page"
                    onChange={handleSettingChange('itemsPerPage')}
                  >
                    <MenuItem value={10}>10</MenuItem>
                    <MenuItem value={25}>25</MenuItem>
                    <MenuItem value={50}>50</MenuItem>
                    <MenuItem value={100}>100</MenuItem>
                  </Select>
                </FormControl>

                <FormControl fullWidth>
                  <InputLabel>Auto Refresh Interval</InputLabel>
                  <Select
                    value={settings.refreshInterval}
                    label="Auto Refresh Interval"
                    onChange={handleSettingChange('refreshInterval')}
                    disabled={!settings.autoRefresh}
                  >
                    <MenuItem value={15}>15 seconds</MenuItem>
                    <MenuItem value={30}>30 seconds</MenuItem>
                    <MenuItem value={60}>1 minute</MenuItem>
                    <MenuItem value={300}>5 minutes</MenuItem>
                  </Select>
                </FormControl>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Notification Settings */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                <NotificationsIcon color="primary" />
                <Typography variant="h6">Notifications</Typography>
              </Box>

              <Stack spacing={2}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.emailNotifications}
                      onChange={handleSettingChange('emailNotifications')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Email Notifications</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Receive important updates via email
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.pushNotifications}
                      onChange={handleSettingChange('pushNotifications')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Push Notifications</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Browser push notifications for real-time alerts
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.alertNotifications}
                      onChange={handleSettingChange('alertNotifications')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Alert Notifications</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Get notified when new alerts are created
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.feedbackNotifications}
                      onChange={handleSettingChange('feedbackNotifications')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Feedback Notifications</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Notify when new feedback is submitted
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.weeklyReports}
                      onChange={handleSettingChange('weeklyReports')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Weekly Reports</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Receive weekly summary reports
                      </Typography>
                    </Box>
                  }
                />
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Data & Privacy Settings */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                <LanguageIcon color="primary" />
                <Typography variant="h6">Data & Privacy</Typography>
              </Box>

              <Stack spacing={2}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.autoRefresh}
                      onChange={handleSettingChange('autoRefresh')}
                      color="info"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Auto Refresh Data</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Automatically refresh dashboards and lists
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.showEmptyStates}
                      onChange={handleSettingChange('showEmptyStates')}
                      color="info"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Show Empty States</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Display helpful messages when no data is available
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.shareAnalytics}
                      onChange={handleSettingChange('shareAnalytics')}
                      color="info"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Share Analytics</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Help improve the app by sharing anonymous usage data
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.activityTracking}
                      onChange={handleSettingChange('activityTracking')}
                      color="info"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Activity Tracking</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Track your activity for audit and compliance
                      </Typography>
                    </Box>
                  }
                />
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Action Buttons */}
      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={handleReset}
          disabled={!hasChanges || saving}
        >
          Reset Changes
        </Button>
        <Button
          variant="contained"
          startIcon={saving ? null : <SaveIcon />}
          onClick={handleSave}
          disabled={!hasChanges || saving}
          size="large"
        >
          {saving ? 'Saving...' : 'Save Settings'}
        </Button>
      </Box>
    </Box>
    </PageTransition>
  );
};

export default SettingsPage;
