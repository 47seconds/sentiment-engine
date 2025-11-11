import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  IconButton,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  TextField,
  Alert,
  CircularProgress,
  Divider,
  Stack,
  Badge,
  Tooltip,
} from '@mui/material';
import {
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  Person as PersonIcon,
  AccessTime as TimeIcon,
  Assignment as AssignmentIcon,
  Done as DoneIcon,
  Close as CloseIcon,
  TrendingUp as EscalateIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import * as alertService from '../services/alertService';
import toast from 'react-hot-toast';
import { CardGridSkeleton } from '../components/LoadingSkeletons';
import PageTransition from '../components/PageTransition';
import { NoAlerts, ErrorState } from '../components/EmptyStates';
import { useAuth } from '../contexts/AuthContext';

const AlertsPage = () => {
  const { isAdmin } = useAuth();
  // State
  const [alerts, setAlerts] = useState([]);
  const [filteredAlerts, setFilteredAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Filters
  const [severityFilter, setSeverityFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  
  // Dialog state
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogType, setDialogType] = useState('');
  const [selectedAlert, setSelectedAlert] = useState(null);
  const [dialogInput, setDialogInput] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  // Stats
  const [stats, setStats] = useState({
    total: 0,
    critical: 0,
    high: 0,
    medium: 0,
    low: 0,
    active: 0,
  });

  // Load alerts
  useEffect(() => {
    fetchAlerts();
    fetchStats();
  }, []);

  // Apply filters
  useEffect(() => {
    let filtered = [...alerts];

    // Filter by severity
    if (severityFilter !== 'ALL') {
      filtered = filtered.filter(alert => alert.severity === severityFilter);
    }

    // Filter by status
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(alert => (alert.status || 'ACTIVE') === statusFilter);
    }

    setFilteredAlerts(filtered);
  }, [alerts, severityFilter, statusFilter]);

  const fetchAlerts = async () => {
    try {
      setLoading(true);
      
      // Get alerts from backend
      const backendResponse = isAdmin() 
        ? await alertService.getActiveAlerts()
        : await alertService.getMyAlerts();
      const backendAlerts = backendResponse.content || backendResponse.data?.content || backendResponse.data || backendResponse || [];
      
      // Get generated alerts from localStorage
      const generatedResponse = await alertService.getGeneratedAlerts();
      const generatedAlerts = generatedResponse.data || [];
      
      // Combine both types of alerts
      const combinedAlerts = [
        ...Array.isArray(backendAlerts) ? backendAlerts : [],
        ...Array.isArray(generatedAlerts) ? generatedAlerts : []
      ];
      
      setAlerts(combinedAlerts);
      setError(null);
    } catch (error) {
      console.error('Error fetching alerts:', error);
      setError('Failed to load alerts');
      toast.error('Failed to load alerts');
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      // Get backend stats
      const response = await alertService.getAlertStats();
      const backendStats = response.data || response;
      
      // Get generated alerts from localStorage to include in counts
      const generatedResponse = await alertService.getGeneratedAlerts();
      const generatedAlerts = generatedResponse.data || [];
      
      // Count generated alerts by severity and status
      const generatedCounts = generatedAlerts.reduce((acc, alert) => {
        if (alert.status === 'ACTIVE') {
          acc.active += 1;
          acc.total += 1;
          
          switch (alert.severity) {
            case 'CRITICAL':
              acc.critical += 1;
              break;
            case 'HIGH':
              acc.high += 1;
              break;
            case 'MEDIUM':
              acc.medium += 1;
              break;
            case 'LOW':
              acc.low += 1;
              break;
          }
        }
        return acc;
      }, { total: 0, critical: 0, high: 0, medium: 0, low: 0, active: 0 });
      
      // Combine backend and generated alert counts
      setStats({
        total: (backendStats.totalAlerts || 0) + generatedCounts.total,
        critical: (backendStats.criticalAlerts || 0) + generatedCounts.critical,
        high: (backendStats.highAlerts || 0) + generatedCounts.high,
        medium: (backendStats.mediumAlerts || 0) + generatedCounts.medium,
        low: (backendStats.lowAlerts || 0) + generatedCounts.low,
        active: (backendStats.activeAlerts || 0) + generatedCounts.active,
      });
    } catch (error) {
      console.error('Error fetching stats:', error);
      // If backend fails, just count generated alerts
      try {
        const generatedResponse = await alertService.getGeneratedAlerts();
        const generatedAlerts = generatedResponse.data || [];
        
        const generatedCounts = generatedAlerts.reduce((acc, alert) => {
          if (alert.status === 'ACTIVE') {
            acc.active += 1;
            acc.total += 1;
            
            switch (alert.severity) {
              case 'CRITICAL':
                acc.critical += 1;
                break;
              case 'HIGH':
                acc.high += 1;
                break;
              case 'MEDIUM':
                acc.medium += 1;
                break;
              case 'LOW':
                acc.low += 1;
                break;
            }
          }
          return acc;
        }, { total: 0, critical: 0, high: 0, medium: 0, low: 0, active: 0 });
        
        setStats(generatedCounts);
      } catch (generatedError) {
        console.error('Error fetching generated alerts:', generatedError);
      }
    }
  };

  // Get severity color
  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'CRITICAL': return 'error';
      case 'HIGH': return 'warning';
      case 'MEDIUM': return 'info';
      case 'LOW': return 'success';
      default: return 'default';
    }
  };

  // Get severity icon
  const getSeverityIcon = (severity) => {
    switch (severity) {
      case 'CRITICAL': return <ErrorIcon />;
      case 'HIGH': return <WarningIcon />;
      case 'MEDIUM': return <InfoIcon />;
      case 'LOW': return <CheckCircleIcon />;
      default: return <InfoIcon />;
    }
  };

  // Get status color
  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'error';
      case 'ACKNOWLEDGED': return 'warning';
      case 'ASSIGNED': return 'info';
      case 'RESOLVED': return 'success';
      case 'DISMISSED': return 'default';
      default: return 'default';
    }
  };

  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Handle action click
  const handleActionClick = (type, alert) => {
    setSelectedAlert(alert);
    setDialogType(type);
    setDialogInput('');
    setDialogOpen(true);
  };

  // Handle action confirm
  const handleActionConfirm = async () => {
    if (!selectedAlert) return;

    try {
      setActionLoading(true);
      let response;

      // Check if this is a generated alert (has string ID) or backend alert (has numeric ID)
      const isGeneratedAlert = typeof selectedAlert.id === 'string';

      switch (dialogType) {
        case 'acknowledge':
          if (isGeneratedAlert) {
            response = await alertService.updateGeneratedAlert(selectedAlert.id, {
              status: 'ACKNOWLEDGED',
              acknowledgedBy: 'current-user-id',
              acknowledgedAt: new Date().toISOString()
            });
          } else {
            response = await alertService.acknowledgeAlert(selectedAlert.id, 'current-user-id');
          }
          toast.success('Alert acknowledged successfully');
          break;
        
        case 'assign':
          if (!dialogInput.trim()) {
            toast.error('Please enter manager ID');
            return;
          }
          if (isGeneratedAlert) {
            response = await alertService.updateGeneratedAlert(selectedAlert.id, {
              assignedTo: dialogInput.trim()
            });
          } else {
            response = await alertService.assignAlert(selectedAlert.id, dialogInput.trim());
          }
          toast.success('Alert assigned successfully');
          break;
        
        case 'resolve':
          if (!dialogInput.trim()) {
            toast.error('Please enter resolution notes');
            return;
          }
          if (isGeneratedAlert) {
            response = await alertService.updateGeneratedAlert(selectedAlert.id, {
              status: 'RESOLVED',
              resolvedBy: 'current-user-id',
              resolvedAt: new Date().toISOString(),
              resolutionNotes: dialogInput.trim()
            });
          } else {
            response = await alertService.resolveAlert(selectedAlert.id, {
              resolutionNotes: dialogInput.trim(),
              resolvedBy: 'current-user-id',
            });
          }
          toast.success('Alert resolved successfully');
          break;
        
        case 'dismiss':
          if (!dialogInput.trim()) {
            toast.error('Please enter dismissal reason');
            return;
          }
          if (isGeneratedAlert) {
            response = await alertService.updateGeneratedAlert(selectedAlert.id, {
              status: 'DISMISSED',
              resolvedBy: 'current-user-id',
              resolvedAt: new Date().toISOString(),
              resolutionNotes: dialogInput.trim()
            });
          } else {
            response = await alertService.dismissAlert(selectedAlert.id, dialogInput.trim());
          }
          toast.success('Alert dismissed successfully');
          break;
        
        case 'escalate':
          if (!dialogInput.trim()) {
            toast.error('Please enter escalation reason');
            return;
          }
          if (isGeneratedAlert) {
            response = await alertService.updateGeneratedAlert(selectedAlert.id, {
              status: 'ESCALATED',
              severity: 'CRITICAL',
              resolutionNotes: dialogInput.trim()
            });
          } else {
            response = await alertService.escalateAlert(selectedAlert.id, dialogInput.trim());
          }
          toast.success('Alert escalated successfully');
          break;
        
        default:
          break;
      }

      // Refresh alerts and stats
      await fetchAlerts();
      await fetchStats();
      
      setDialogOpen(false);
      setSelectedAlert(null);
      setDialogInput('');
      
    } catch (error) {
      console.error('Error performing action:', error);
      toast.error(error.response?.data?.message || 'Failed to perform action');
    } finally {
      setActionLoading(false);
    }
  };

  // Get dialog content
  const getDialogContent = () => {
    switch (dialogType) {
      case 'acknowledge':
        return {
          title: 'Acknowledge Alert',
          description: 'Confirm that you have seen and acknowledged this alert.',
          showInput: false,
        };
      case 'assign':
        return {
          title: 'Assign Alert',
          description: 'Enter the manager ID to assign this alert to.',
          showInput: true,
          inputLabel: 'Manager ID',
          inputPlaceholder: 'Enter manager identifier',
        };
      case 'resolve':
        return {
          title: 'Resolve Alert',
          description: 'Provide resolution notes for this alert.',
          showInput: true,
          inputLabel: 'Resolution Notes',
          inputPlaceholder: 'Describe how the issue was resolved...',
          multiline: true,
        };
      case 'dismiss':
        return {
          title: 'Dismiss Alert',
          description: 'Provide a reason for dismissing this alert.',
          showInput: true,
          inputLabel: 'Dismissal Reason',
          inputPlaceholder: 'Explain why this alert should be dismissed...',
          multiline: true,
        };
      case 'escalate':
        return {
          title: 'Escalate Alert',
          description: 'Provide a reason for escalating this alert to higher priority.',
          showInput: true,
          inputLabel: 'Escalation Reason',
          inputPlaceholder: 'Explain why this alert requires escalation...',
          multiline: true,
        };
      default:
        return { title: '', description: '', showInput: false };
    }
  };

  const dialogContent = getDialogContent();

  // Sort alerts: CRITICAL first, then by date
  const sortedAlerts = [...filteredAlerts].sort((a, b) => {
    const severityOrder = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };
    const severityDiff = severityOrder[a.severity] - severityOrder[b.severity];
    if (severityDiff !== 0) return severityDiff;
    
    // Handle date sorting for both backend and generated alerts
    const aDate = new Date(a.createdAt || a.created_at || Date.now());
    const bDate = new Date(b.createdAt || b.created_at || Date.now());
    return bDate - aDate;
  });

  if (loading && alerts.length === 0) {
    return <CardGridSkeleton items={6} />;
  }

  if (error) {
    return (
      <ErrorState
        title="Failed to Load Alerts"
        description={error}
        onRetry={fetchAlerts}
      />
    );
  }

  if (filteredAlerts.length === 0 && alerts.length === 0) {
    return <NoAlerts />;
  }

  return (
    <PageTransition>
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <WarningIcon sx={{ fontSize: 40, color: 'warning.main' }} />
          <Box>
            <Typography variant="h4" gutterBottom>
              Alerts Management
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Monitor and manage system alerts
            </Typography>
          </Box>
        </Box>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={() => {
            fetchAlerts();
            fetchStats();
          }}
        >
          Refresh
        </Button>
        <Button
          variant="outlined"
          color="warning"
          startIcon={<CloseIcon />}
          onClick={async () => {
            try {
              await alertService.clearGeneratedAlerts();
              await fetchAlerts(); // Refresh to show cleared alerts
              toast.success('Generated alerts cleared');
            } catch (error) {
              toast.error('Failed to clear alerts');
            }
          }}
        >
          Clear Generated Alerts
        </Button>
      </Box>

      {/* Statistics */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={6} sm={4} md={2}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Total
              </Typography>
              <Typography variant="h4">{stats.total}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card sx={{ bgcolor: 'error.main', color: 'white' }}>
            <CardContent>
              <Typography variant="body2" gutterBottom sx={{ opacity: 0.9 }}>
                Critical
              </Typography>
              <Typography variant="h4">{stats.critical}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card sx={{ bgcolor: 'warning.main', color: 'white' }}>
            <CardContent>
              <Typography variant="body2" gutterBottom sx={{ opacity: 0.9 }}>
                High
              </Typography>
              <Typography variant="h4">{stats.high}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card sx={{ bgcolor: 'info.main', color: 'white' }}>
            <CardContent>
              <Typography variant="body2" gutterBottom sx={{ opacity: 0.9 }}>
                Medium
              </Typography>
              <Typography variant="h4">{stats.medium}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card sx={{ bgcolor: 'success.main', color: 'white' }}>
            <CardContent>
              <Typography variant="body2" gutterBottom sx={{ opacity: 0.9 }}>
                Low
              </Typography>
              <Typography variant="h4">{stats.low}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Active
              </Typography>
              <Typography variant="h4" color="error.main">{stats.active}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Severity</InputLabel>
                <Select
                  value={severityFilter}
                  label="Severity"
                  onChange={(e) => setSeverityFilter(e.target.value)}
                >
                  <MenuItem value="ALL">All Severities</MenuItem>
                  <MenuItem value="CRITICAL">Critical</MenuItem>
                  <MenuItem value="HIGH">High</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="LOW">Low</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Status</InputLabel>
                <Select
                  value={statusFilter}
                  label="Status"
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <MenuItem value="ALL">All Statuses</MenuItem>
                  <MenuItem value="ACTIVE">Active</MenuItem>
                  <MenuItem value="ACKNOWLEDGED">Acknowledged</MenuItem>
                  <MenuItem value="ASSIGNED">Assigned</MenuItem>
                  <MenuItem value="RESOLVED">Resolved</MenuItem>
                  <MenuItem value="DISMISSED">Dismissed</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={12} md={6}>
              <Typography variant="body2" color="text.secondary" sx={{ lineHeight: '40px' }}>
                Showing {sortedAlerts.length} of {alerts.length} alerts
              </Typography>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Error State */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Alerts List */}
      {sortedAlerts.length === 0 ? (
        <Card>
          <CardContent sx={{ textAlign: 'center', py: 8 }}>
            <CheckCircleIcon sx={{ fontSize: 64, color: 'success.main', mb: 2 }} />
            <Typography variant="h6" gutterBottom>
              No Alerts Found
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {severityFilter !== 'ALL' || statusFilter !== 'ALL'
                ? 'Try adjusting your filters to see more alerts'
                : 'All systems are running smoothly'}
            </Typography>
          </CardContent>
        </Card>
      ) : (
        <Grid container spacing={2}>
          {sortedAlerts.map((alert) => (
            <Grid item xs={12} key={alert.id}>
              <Card
                sx={{
                  borderLeft: `4px solid`,
                  borderLeftColor: `${getSeverityColor(alert.severity)}.main`,
                  transition: 'box-shadow 0.2s',
                  '&:hover': {
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent>
                  <Grid container spacing={2} alignItems="center">
                    {/* Left: Icon + Content */}
                    <Grid item xs={12} md={6}>
                      <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                        <Box
                          sx={{
                            p: 1,
                            borderRadius: 1,
                            bgcolor: `${getSeverityColor(alert.severity)}.light`,
                            color: `${getSeverityColor(alert.severity)}.dark`,
                            display: 'flex',
                          }}
                        >
                          {getSeverityIcon(alert.severity)}
                        </Box>
                        <Box sx={{ flex: 1 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                            <Chip
                              label={alert.severity}
                              color={getSeverityColor(alert.severity)}
                              size="small"
                            />
                            <Chip
                              label={alert.status || 'ACTIVE'}
                              color={getStatusColor(alert.status || 'ACTIVE')}
                              size="small"
                              variant="outlined"
                            />
                            {typeof alert.id === 'string' && (
                              <Chip
                                label="AUTO"
                                color="info"
                                size="small"
                                variant="outlined"
                                sx={{ fontSize: '0.7rem' }}
                              />
                            )}
                          </Box>
                          <Typography variant="h6" gutterBottom>
                            {alert.alertType || 'System Alert'}
                          </Typography>
                          <Typography variant="body2" color="text.secondary" paragraph>
                            {alert.message || 'No description available'}
                          </Typography>
                          <Stack direction="row" spacing={2} sx={{ mt: 1 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                              <PersonIcon fontSize="small" color="action" />
                              <Typography variant="caption" color="text.secondary">
                                {alert.driverName || `Driver #${alert.driverId}`}
                              </Typography>
                            </Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                              <TimeIcon fontSize="small" color="action" />
                              <Typography variant="caption" color="text.secondary">
                                {formatDate(alert.createdAt)}
                              </Typography>
                            </Box>
                          </Stack>
                        </Box>
                      </Box>
                    </Grid>

                    {/* Right: Actions */}
                    <Grid item xs={12} md={6}>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', justifyContent: { xs: 'flex-start', md: 'flex-end' } }}>
                        {(alert.status || 'ACTIVE') === 'ACTIVE' && (
                          <Tooltip title="Acknowledge">
                            <Button
                              size="small"
                              variant="outlined"
                              color="warning"
                              startIcon={<CheckCircleIcon />}
                              onClick={() => handleActionClick('acknowledge', alert)}
                            >
                              Acknowledge
                            </Button>
                          </Tooltip>
                        )}
                        {((alert.status || 'ACTIVE') === 'ACTIVE' || alert.status === 'ACKNOWLEDGED') && (
                          <Tooltip title="Assign to manager">
                            <Button
                              size="small"
                              variant="outlined"
                              color="info"
                              startIcon={<AssignmentIcon />}
                              onClick={() => handleActionClick('assign', alert)}
                            >
                              Assign
                            </Button>
                          </Tooltip>
                        )}
                        {alert.status !== 'RESOLVED' && alert.status !== 'DISMISSED' && (
                          <Tooltip title="Resolve alert">
                            <Button
                              size="small"
                              variant="outlined"
                              color="success"
                              startIcon={<DoneIcon />}
                              onClick={() => handleActionClick('resolve', alert)}
                            >
                              Resolve
                            </Button>
                          </Tooltip>
                        )}
                        {alert.status !== 'DISMISSED' && alert.status !== 'RESOLVED' && (
                          <Tooltip title="Dismiss alert">
                            <Button
                              size="small"
                              variant="outlined"
                              color="inherit"
                              startIcon={<CloseIcon />}
                              onClick={() => handleActionClick('dismiss', alert)}
                            >
                              Dismiss
                            </Button>
                          </Tooltip>
                        )}
                        {alert.severity !== 'CRITICAL' && (alert.status || 'ACTIVE') === 'ACTIVE' && (
                          <Tooltip title="Escalate priority">
                            <Button
                              size="small"
                              variant="outlined"
                              color="error"
                              startIcon={<EscalateIcon />}
                              onClick={() => handleActionClick('escalate', alert)}
                            >
                              Escalate
                            </Button>
                          </Tooltip>
                        )}
                      </Box>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Action Dialog */}
      <Dialog 
        open={dialogOpen} 
        onClose={() => !actionLoading && setDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>{dialogContent.title}</DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogContent.description}
          </DialogContentText>
          {selectedAlert && (
            <Alert severity={getSeverityColor(selectedAlert.severity)} sx={{ mb: 2 }}>
              <Typography variant="body2" fontWeight="bold">
                {selectedAlert.alertType}
              </Typography>
              <Typography variant="caption">
                {selectedAlert.message}
              </Typography>
            </Alert>
          )}
          {dialogContent.showInput && (
            <TextField
              autoFocus
              fullWidth
              label={dialogContent.inputLabel}
              placeholder={dialogContent.inputPlaceholder}
              value={dialogInput}
              onChange={(e) => setDialogInput(e.target.value)}
              multiline={dialogContent.multiline}
              rows={dialogContent.multiline ? 4 : 1}
              disabled={actionLoading}
              sx={{ mt: 2 }}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)} disabled={actionLoading}>
            Cancel
          </Button>
          <Button
            onClick={handleActionConfirm}
            variant="contained"
            disabled={actionLoading}
            startIcon={actionLoading ? <CircularProgress size={20} /> : null}
          >
            {actionLoading ? 'Processing...' : 'Confirm'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
    </PageTransition>
  );
};

export default AlertsPage;
