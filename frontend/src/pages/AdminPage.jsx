import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Slider,
  TextField,
  Switch,
  FormControlLabel,
  Button,
  Divider,
  Alert,
  CircularProgress,
  Paper,
  Chip,
  Stack,
  Tooltip,
  IconButton,
} from '@mui/material';
import {
  Settings as SettingsIcon,
  Save as SaveIcon,
  Refresh as RefreshIcon,
  Info as InfoIcon,
  CheckCircle as CheckCircleIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import { SettingsSkeleton } from '../components/LoadingSkeletons';
import PageTransition from '../components/PageTransition';
import UserManagement from '../components/UserManagement';
import { getAdminConfiguration, saveAdminConfiguration } from '../services/adminConfigService';

const AdminPage = () => {
  // Original configuration (from API or defaults)
  const [originalConfig, setOriginalConfig] = useState(null);
  
  // Current configuration state
  const [config, setConfig] = useState({
    // Threshold settings
    criticalThreshold: -0.6,
    warningThreshold: -0.3,
    cooldownPeriod: 120, // minutes
    
    // Feature flags
    driverFeedbackEnabled: true,
    tripFeedbackEnabled: false,
    appFeedbackEnabled: false,
    marshalFeedbackEnabled: false,
    
    // Alert settings
    maxAlertsPerDriver: 5,
    alertRetentionDays: 30,
    autoEscalationEnabled: true,
    
    // Notification settings
    emailNotificationsEnabled: true,
    smsNotificationsEnabled: false,
  });

  // UI state
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);

  // Load configuration on mount
  useEffect(() => {
    loadConfiguration();
  }, []);

  // Check for changes
  useEffect(() => {
    if (originalConfig) {
      const changed = JSON.stringify(config) !== JSON.stringify(originalConfig);
      setHasChanges(changed);
    }
  }, [config, originalConfig]);

  // Load configuration from API
  const loadConfiguration = async () => {
    try {
      setLoading(true);
      
      // Call the real API
      const response = await getAdminConfiguration();
      const configData = response.data || response;
      
      setOriginalConfig(configData);
      setConfig(configData);
      
    } catch (error) {
      console.error('Error loading configuration:', error);
      toast.error('Failed to load configuration');
      
      // Fallback to defaults if API fails
      const defaultConfig = {
        criticalThreshold: -0.6,
        warningThreshold: -0.3,
        cooldownPeriod: 120,
        driverFeedbackEnabled: true,
        tripFeedbackEnabled: false,
        appFeedbackEnabled: false,
        marshalFeedbackEnabled: false,
        maxAlertsPerDriver: 5,
        alertRetentionDays: 30,
        autoEscalationEnabled: true,
        emailNotificationsEnabled: true,
        smsNotificationsEnabled: false,
      };
      
      setOriginalConfig(defaultConfig);
      setConfig(defaultConfig);
    } finally {
      setLoading(false);
    }
  };

  // Handle save
  const handleSave = async () => {
    try {
      setSaving(true);
      
      // Validate configuration
      if (config.criticalThreshold >= config.warningThreshold) {
        toast.error('Critical threshold must be less than warning threshold');
        return;
      }
      
      if (config.cooldownPeriod < 1) {
        toast.error('Cooldown period must be at least 1 minute');
        return;
      }
      
      // Call the real API to save configuration
      const response = await saveAdminConfiguration(config);
      const savedConfig = response.data || response;
      
      setOriginalConfig(savedConfig);
      setConfig(savedConfig);
      toast.success('Configuration saved successfully!');
      
    } catch (error) {
      console.error('Error saving configuration:', error);
      
      // Check if it's a validation error from the backend
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error('Failed to save configuration');
      }
    } finally {
      setSaving(false);
    }
  };

  // Handle reset
  const handleReset = () => {
    if (originalConfig) {
      setConfig(originalConfig);
      toast.success('Changes reset');
    }
  };

  // Handle threshold change
  const handleThresholdChange = (field, value) => {
    setConfig({ ...config, [field]: value });
  };

  // Handle switch change
  const handleSwitchChange = (field) => (event) => {
    setConfig({ ...config, [field]: event.target.checked });
  };

  // Handle number input change
  const handleNumberChange = (field) => (event) => {
    const value = parseInt(event.target.value, 10);
    if (!isNaN(value)) {
      setConfig({ ...config, [field]: value });
    }
  };

  // Get threshold color
  const getThresholdColor = (value) => {
    if (value <= -0.6) return 'error';
    if (value <= -0.3) return 'warning';
    if (value <= 0) return 'info';
    return 'success';
  };

  // Get enabled features count
  const getEnabledFeaturesCount = () => {
    return [
      config.driverFeedbackEnabled,
      config.tripFeedbackEnabled,
      config.appFeedbackEnabled,
      config.marshalFeedbackEnabled,
    ].filter(Boolean).length;
  };

  if (loading) {
    return <SettingsSkeleton />;
  }

  return (
    <PageTransition>
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <SettingsIcon sx={{ fontSize: 40, color: 'primary.main' }} />
          <Box>
            <Typography variant="h4" gutterBottom>
              Admin Panel
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Manage users, configure thresholds, feature flags, and system settings
            </Typography>
          </Box>
        </Box>
        
        {hasChanges && (
          <Chip
            icon={<WarningIcon />}
            label="Unsaved Changes"
            color="warning"
            variant="outlined"
          />
        )}
      </Box>

      {/* User Management Section */}
      <Box sx={{ mb: 3 }}>
        <UserManagement />
      </Box>

      <Divider sx={{ my: 4 }} />

      {/* System Configuration Header */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          System Configuration
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Configure thresholds, feature flags, and system settings
        </Typography>
      </Box>

      {/* Warning Alert */}
      {hasChanges && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          You have unsaved changes. Click "Save Configuration" to apply them.
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Left Column: Configuration Settings */}
        <Grid item xs={12} md={8}>
          {/* Threshold Settings */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                <Typography variant="h6">Sentiment Thresholds</Typography>
                <Tooltip title="Configure when alerts are triggered based on EMA scores">
                  <IconButton size="small">
                    <InfoIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Box>

              {/* Critical Threshold */}
              <Box sx={{ mb: 4 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    Critical Threshold
                  </Typography>
                  <Chip
                    label={config.criticalThreshold.toFixed(2)}
                    color={getThresholdColor(config.criticalThreshold)}
                    size="small"
                  />
                </Box>
                <Slider
                  value={config.criticalThreshold}
                  onChange={(e, value) => handleThresholdChange('criticalThreshold', value)}
                  min={-1.0}
                  max={0.0}
                  step={0.05}
                  marks={[
                    { value: -1.0, label: '-1.0' },
                    { value: -0.5, label: '-0.5' },
                    { value: 0.0, label: '0.0' },
                  ]}
                  valueLabelDisplay="auto"
                  color={getThresholdColor(config.criticalThreshold)}
                />
                <Typography variant="caption" color="text.secondary">
                  EMA scores below this value trigger CRITICAL alerts
                </Typography>
              </Box>

              {/* Warning Threshold */}
              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    Warning Threshold
                  </Typography>
                  <Chip
                    label={config.warningThreshold.toFixed(2)}
                    color={getThresholdColor(config.warningThreshold)}
                    size="small"
                  />
                </Box>
                <Slider
                  value={config.warningThreshold}
                  onChange={(e, value) => handleThresholdChange('warningThreshold', value)}
                  min={-1.0}
                  max={0.5}
                  step={0.05}
                  marks={[
                    { value: -1.0, label: '-1.0' },
                    { value: -0.25, label: '-0.25' },
                    { value: 0.5, label: '0.5' },
                  ]}
                  valueLabelDisplay="auto"
                  color={getThresholdColor(config.warningThreshold)}
                />
                <Typography variant="caption" color="text.secondary">
                  EMA scores below this value trigger WARNING alerts
                </Typography>
              </Box>

              {/* Validation message */}
              {config.criticalThreshold >= config.warningThreshold && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  Critical threshold must be less than warning threshold
                </Alert>
              )}
            </CardContent>
          </Card>

          {/* Alert Settings */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Alert Settings
              </Typography>

              <Grid container spacing={3}>
                {/* Cooldown Period */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Cooldown Period (minutes)"
                    type="number"
                    value={config.cooldownPeriod}
                    onChange={handleNumberChange('cooldownPeriod')}
                    helperText="Minimum time between alerts for the same driver"
                    inputProps={{ min: 1, max: 1440 }}
                  />
                </Grid>

                {/* Max Alerts Per Driver */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Max Alerts Per Driver"
                    type="number"
                    value={config.maxAlertsPerDriver}
                    onChange={handleNumberChange('maxAlertsPerDriver')}
                    helperText="Maximum active alerts per driver"
                    inputProps={{ min: 1, max: 20 }}
                  />
                </Grid>

                {/* Alert Retention Days */}
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Alert Retention (days)"
                    type="number"
                    value={config.alertRetentionDays}
                    onChange={handleNumberChange('alertRetentionDays')}
                    helperText="How long to keep resolved alerts"
                    inputProps={{ min: 1, max: 365 }}
                  />
                </Grid>

                {/* Auto Escalation */}
                <Grid item xs={12} sm={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={config.autoEscalationEnabled}
                        onChange={handleSwitchChange('autoEscalationEnabled')}
                        color="primary"
                      />
                    }
                    label="Auto Escalation Enabled"
                  />
                  <Typography variant="caption" color="text.secondary" display="block">
                    Automatically escalate unresolved alerts
                  </Typography>
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Feature Flags */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">Feature Flags</Typography>
                <Chip
                  label={`${getEnabledFeaturesCount()} / 4 Enabled`}
                  color="primary"
                  size="small"
                />
              </Box>

              <Stack spacing={2}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={config.driverFeedbackEnabled}
                      onChange={handleSwitchChange('driverFeedbackEnabled')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Driver Feedback</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Allow feedback submission for drivers
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={config.tripFeedbackEnabled}
                      onChange={handleSwitchChange('tripFeedbackEnabled')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Trip Feedback</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Allow feedback submission for trips
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={config.appFeedbackEnabled}
                      onChange={handleSwitchChange('appFeedbackEnabled')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">App Feedback</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Allow feedback submission for app experience
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={config.marshalFeedbackEnabled}
                      onChange={handleSwitchChange('marshalFeedbackEnabled')}
                      color="success"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Marshal Feedback</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Allow feedback submission for marshals
                      </Typography>
                    </Box>
                  }
                />
              </Stack>
            </CardContent>
          </Card>

          {/* Notification Settings */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Notification Settings
              </Typography>

              <Stack spacing={2}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={config.emailNotificationsEnabled}
                      onChange={handleSwitchChange('emailNotificationsEnabled')}
                      color="info"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">Email Notifications</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Send email alerts to managers
                      </Typography>
                    </Box>
                  }
                />

                <Divider />

                <FormControlLabel
                  control={
                    <Switch
                      checked={config.smsNotificationsEnabled}
                      onChange={handleSwitchChange('smsNotificationsEnabled')}
                      color="info"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">SMS Notifications</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Send SMS alerts for critical issues
                      </Typography>
                    </Box>
                  }
                />
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Right Column: Preview & Actions */}
        <Grid item xs={12} md={4}>
          {/* Current Configuration Preview */}
          <Card sx={{ mb: 3, bgcolor: 'primary.main', color: 'white' }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Configuration Preview
              </Typography>
              
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2" sx={{ mb: 1, opacity: 0.9 }}>
                  Thresholds
                </Typography>
                <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
                  <Chip
                    label={`Critical: ${config.criticalThreshold.toFixed(2)}`}
                    size="small"
                    sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                  />
                  <Chip
                    label={`Warning: ${config.warningThreshold.toFixed(2)}`}
                    size="small"
                    sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                  />
                </Stack>

                <Typography variant="body2" sx={{ mb: 1, opacity: 0.9 }}>
                  Alert Settings
                </Typography>
                <Typography variant="caption" display="block" sx={{ mb: 0.5 }}>
                  • Cooldown: {config.cooldownPeriod} minutes
                </Typography>
                <Typography variant="caption" display="block" sx={{ mb: 0.5 }}>
                  • Max Alerts: {config.maxAlertsPerDriver} per driver
                </Typography>
                <Typography variant="caption" display="block" sx={{ mb: 2 }}>
                  • Retention: {config.alertRetentionDays} days
                </Typography>

                <Typography variant="body2" sx={{ mb: 1, opacity: 0.9 }}>
                  Enabled Features
                </Typography>
                <Stack spacing={0.5}>
                  {config.driverFeedbackEnabled && (
                    <Chip
                      icon={<CheckCircleIcon />}
                      label="Driver Feedback"
                      size="small"
                      sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                    />
                  )}
                  {config.tripFeedbackEnabled && (
                    <Chip
                      icon={<CheckCircleIcon />}
                      label="Trip Feedback"
                      size="small"
                      sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                    />
                  )}
                  {config.appFeedbackEnabled && (
                    <Chip
                      icon={<CheckCircleIcon />}
                      label="App Feedback"
                      size="small"
                      sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                    />
                  )}
                  {config.marshalFeedbackEnabled && (
                    <Chip
                      icon={<CheckCircleIcon />}
                      label="Marshal Feedback"
                      size="small"
                      sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                    />
                  )}
                  {getEnabledFeaturesCount() === 0 && (
                    <Typography variant="caption" sx={{ opacity: 0.7 }}>
                      No features enabled
                    </Typography>
                  )}
                </Stack>
              </Box>
            </CardContent>
          </Card>

          {/* Action Buttons */}
          <Paper sx={{ p: 2, mb: 3 }}>
            <Stack spacing={2}>
              <Button
                fullWidth
                variant="contained"
                size="large"
                startIcon={saving ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
                onClick={handleSave}
                disabled={!hasChanges || saving || config.criticalThreshold >= config.warningThreshold}
              >
                {saving ? 'Saving...' : 'Save Configuration'}
              </Button>
              
              <Button
                fullWidth
                variant="outlined"
                startIcon={<RefreshIcon />}
                onClick={handleReset}
                disabled={!hasChanges || saving}
              >
                Reset Changes
              </Button>
            </Stack>
          </Paper>

          {/* Guidelines */}
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom color="primary">
              Guidelines
            </Typography>
            <Box component="ul" sx={{ pl: 2, m: 0 }}>
              <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                Critical threshold should be significantly lower than warning
              </Typography>
              <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                Cooldown prevents alert spam
              </Typography>
              <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                Enable only needed features to reduce noise
              </Typography>
              <Typography component="li" variant="body2">
                Test changes in staging first
              </Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
    </PageTransition>
  );
};

export default AdminPage;
