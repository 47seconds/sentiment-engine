import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  TextField,
  InputAdornment,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Chip,
  IconButton,
  Typography,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Grid,
  Divider,
  Avatar,
  Fab,
  Collapse,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Snackbar,
  AlertTitle,
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Person as PersonIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  CalendarToday as CalendarIcon,
  Assessment as AssessmentIcon,
  Close as CloseIcon,
  Add as AddIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  PersonAdd as PersonAddIcon,
  Refresh as RefreshIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';
import * as driverStatsService from '../services/driverStatsService';
import * as driverService from '../services/driverService';
import * as alertService from '../services/alertService';
import { TableSkeleton } from '../components/LoadingSkeletons';
import PageTransition from '../components/PageTransition';
import { NoSearchResults, ErrorState } from '../components/EmptyStates';

const DriversPage = () => {
  const navigate = useNavigate();
  const [drivers, setDrivers] = useState([]);
  const [filteredDrivers, setFilteredDrivers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [orderBy, setOrderBy] = useState('emaScore');
  const [order, setOrder] = useState('desc');
  const [selectedDriver, setSelectedDriver] = useState(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [detailsLoading, setDetailsLoading] = useState(false);

  // Alert monitoring state
  const [alertMonitoring, setAlertMonitoring] = useState(true);
  const [lastAlertCheck, setLastAlertCheck] = useState(null);
  const [alertsGenerated, setAlertsGenerated] = useState([]);

  // Add Driver Form State
  const [addDriverOpen, setAddDriverOpen] = useState(false);
  const [addingDriver, setAddingDriver] = useState(false);
  const [newDriverData, setNewDriverData] = useState({
    name: '',
    email: '',
    phoneNumber: '',
    driverId: '',
    licenseNumber: '',
    vehicleNumber: ''
  });
  const [formErrors, setFormErrors] = useState({});

  // Snackbar state
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });

  // Fetch all drivers on component mount and set up periodic refresh
  useEffect(() => {
    fetchDrivers();
    
    // Set up periodic refresh every 30 seconds to catch rating updates
    const refreshInterval = setInterval(() => {
      fetchDrivers();
    }, 30000);
    
    return () => clearInterval(refreshInterval);
  }, []);

  // Monitor for alert conditions every time drivers data changes
  useEffect(() => {
    if (drivers.length > 0 && alertMonitoring) {
      checkDriverAlerts();
    }
  }, [drivers, alertMonitoring]);

  const fetchDrivers = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // First, try to get all drivers from the users endpoint
      let driversData = [];
      
      try {
        const driversResponse = await driverService.getAllDrivers();
        // Handle both array and ApiResponse wrapper formats
        driversData = Array.isArray(driversResponse) ? driversResponse : 
                     (driversResponse.data || []);
        
        // If we got user data, try to enhance it with stats data
        if (driversData.length > 0) {
          try {
            const statsResponse = await driverStatsService.getAllDriverStats();
            const statsData = Array.isArray(statsResponse) ? statsResponse : 
                             (statsResponse.data || []);
            
            // Merge user data with stats data
            driversData = driversData.map(driver => {
              const stats = statsData.find(stat => stat.driverId === driver.driverId);
              return {
                ...driver,
                driverName: driver.name,
                // Use stats if available, otherwise default values
                emaScore: stats?.emaScore || 0,
                totalFeedbackCount: stats?.totalFeedbackCount || 0,
                positiveFeedbackCount: stats?.positiveFeedbackCount || 0,
                negativeFeedbackCount: stats?.negativeFeedbackCount || 0,
                neutralFeedbackCount: stats?.neutralFeedbackCount || 0,
                activeAlertsCount: stats?.activeAlertsCount || 0,
                lastFeedbackDate: stats?.lastFeedbackDate || null
              };
            });
          } catch (statsError) {
            console.warn('Could not fetch driver stats, using user data only:', statsError);
            // Just use the user data with default stats values
            driversData = driversData.map(driver => ({
              ...driver,
              driverName: driver.name,
              emaScore: 0,
              totalFeedbackCount: 0,
              positiveFeedbackCount: 0,
              negativeFeedbackCount: 0,
              neutralFeedbackCount: 0,
              activeAlertsCount: 0,
              lastFeedbackDate: null
            }));
          }
        }
      } catch (userError) {
        console.warn('Could not fetch users, falling back to stats only:', userError);
        // Fallback to stats-only approach
        const response = await driverStatsService.getAllDriverStats();
        driversData = Array.isArray(response) ? response : (response.data || []);
      }
      
      setDrivers(driversData);
      setFilteredDrivers(driversData);
    } catch (err) {
      console.error('Error fetching drivers:', err);
      setError('Failed to load drivers. Please try again.');
    } finally {
      setLoading(false);
    }
  };

      // Check drivers for alert conditions
  const checkDriverAlerts = async () => {
    try {
      const alertsCreated = await alertService.checkAndTriggerAlerts(drivers);
      
      if (alertsCreated.length > 0) {
        setAlertsGenerated(prev => [...prev, ...alertsCreated]);
        setLastAlertCheck(new Date());
        
        // Show simple notification for new alerts
        const criticalCount = alertsCreated.filter(a => a.severity === 'CRITICAL').length;
        const warningCount = alertsCreated.filter(a => a.severity === 'HIGH').length;
        
        let message = '';
        if (criticalCount > 0 && warningCount > 0) {
          message = `${criticalCount} critical and ${warningCount} warning alerts generated! Check Alerts page.`;
        } else if (criticalCount > 0) {
          message = `${criticalCount} critical alert${criticalCount > 1 ? 's' : ''} generated! Check Alerts page.`;
        } else if (warningCount > 0) {
          message = `${warningCount} warning alert${warningCount > 1 ? 's' : ''} generated! Check Alerts page.`;
        }
        
        if (message) {
          setSnackbar({
            open: true,
            message,
            severity: criticalCount > 0 ? 'error' : 'warning'
          });
        }
      }
    } catch (error) {
      console.error('Error checking driver alerts:', error);
    }
  };

  // Search functionality
  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredDrivers(drivers);
    } else {
      const term = searchTerm.toLowerCase();
      const filtered = drivers.filter((driver) => {
        const name = (driver.driverName || driver.name || '').toLowerCase();
        const email = driver.email?.toLowerCase() || '';
        const phone = driver.phoneNumber || '';
        const license = driver.licenseNumber?.toLowerCase() || '';
        const vehicle = driver.vehicleNumber?.toLowerCase() || '';
        const driverId = (driver.driverId?.toString() || '').toLowerCase();
        
        return name.includes(term) || 
               email.includes(term) || 
               phone.includes(term) ||
               license.includes(term) ||
               vehicle.includes(term) ||
               driverId.includes(term);
      });
      setFilteredDrivers(filtered);
    }
    setPage(0); // Reset to first page when searching
  }, [searchTerm, drivers]);

  // Sorting functionality
  const handleSort = (property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const sortedDrivers = [...filteredDrivers].sort((a, b) => {
    let aValue = a[orderBy];
    let bValue = b[orderBy];

    // Handle string values
    if (typeof aValue === 'string') {
      aValue = aValue.toLowerCase();
      bValue = bValue?.toLowerCase() || '';
    }

    // Handle null/undefined
    if (aValue == null) aValue = order === 'asc' ? Infinity : -Infinity;
    if (bValue == null) bValue = order === 'asc' ? Infinity : -Infinity;

    if (order === 'asc') {
      return aValue > bValue ? 1 : -1;
    } else {
      return aValue < bValue ? 1 : -1;
    }
  });

  // Pagination
  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // Get color based on EMA score
  const getEmaColor = (score) => {
    if (score >= 0.2) return 'success'; // Green - Good
    if (score >= -0.3) return 'default'; // Gray - Neutral
    if (score >= -0.6) return 'warning'; // Amber - Warning
    return 'error'; // Red - Critical
  };

  const getEmaLabel = (score) => {
    if (score >= 0.2) return 'Positive';
    if (score >= -0.3) return 'Neutral';
    if (score >= -0.6) return 'Warning';
    return 'Critical';
  };

  // Get alert status based on EMA score
  const getAlertStatus = (score) => {
    if (score <= -0.6) return { level: 'critical', color: 'error', icon: '🚨' };
    if (score <= -0.3) return { level: 'warning', color: 'warning', icon: '⚠️' };
    return { level: 'normal', color: 'success', icon: '✅' };
  };

  // Open driver details dialog
  const handleRowClick = async (driver) => {
    try {
      setSelectedDriver(null); // Clear previous selection
      setDetailsLoading(true);
      setDetailsOpen(true); // Open dialog immediately for better UX
      
      // Use the driver data we already have, and enhance it with additional details if needed
      let detailedDriver = { ...driver };
      
      // If we have a valid driverId, try to fetch additional stats
      if (driver.driverId) {
        try {
          const detailedStats = await driverStatsService.getDriverStats(driver.driverId);
          const statsData = detailedStats.data || detailedStats;
          
          // Merge the existing driver data with the detailed stats
          detailedDriver = {
            ...driver,
            ...statsData,
            // Ensure we keep the name field properly mapped
            driverName: driver.driverName || driver.name || statsData.driverName,
            name: driver.name || driver.driverName || statsData.driverName
          };
        } catch (statsError) {
          console.warn('Could not fetch detailed stats for driver:', driver.driverId, statsError);
          // Use the driver data we already have
        }
      }
      
      setSelectedDriver(detailedDriver);
    } catch (err) {
      console.error('Error opening driver details:', err);
      // Fallback: just use the basic driver data we have
      setSelectedDriver(driver);
      setDetailsOpen(true);
    } finally {
      setDetailsLoading(false);
    }
  };

  const handleCloseDetails = () => {
    setDetailsOpen(false);
    setSelectedDriver(null);
  };

  // Add Driver Functions
  const handleOpenAddDriver = () => {
    setAddDriverOpen(true);
    setNewDriverData({
      name: '',
      email: '',
      phoneNumber: '',
      driverId: '',
      licenseNumber: '',
      vehicleNumber: ''
    });
    setFormErrors({});
  };

  const handleCloseAddDriver = () => {
    setAddDriverOpen(false);
    setNewDriverData({
      name: '',
      email: '',
      phoneNumber: '',
      driverId: '',
      licenseNumber: '',
      vehicleNumber: ''
    });
    setFormErrors({});
  };

  const handleInputChange = (field) => (event) => {
    setNewDriverData(prev => ({
      ...prev,
      [field]: event.target.value
    }));
    
    // Clear error for this field when user starts typing
    if (formErrors[field]) {
      setFormErrors(prev => ({
        ...prev,
        [field]: null
      }));
    }
  };

  const validateForm = () => {
    const errors = {};

    if (!newDriverData.name.trim()) {
      errors.name = 'Name is required';
    }

    if (!newDriverData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newDriverData.email)) {
      errors.email = 'Invalid email format';
    }

    if (!newDriverData.phoneNumber.trim()) {
      errors.phoneNumber = 'Phone number is required';
    }

    if (!newDriverData.driverId.trim()) {
      errors.driverId = 'Driver ID is required to distinguish drivers from employees';
    }

    return errors;
  };

  const handleAddDriver = async () => {
    const errors = validateForm();
    
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }

    try {
      setAddingDriver(true);
      await driverService.addDriver(newDriverData);
      
      setSnackbar({
        open: true,
        message: 'Driver added successfully!',
        severity: 'success'
      });
      
      handleCloseAddDriver();
      fetchDrivers(); // Refresh the drivers list
    } catch (err) {
      console.error('Error adding driver:', err);
      setSnackbar({
        open: true,
        message: 'Failed to add driver. Please try again.',
        severity: 'error'
      });
    } finally {
      setAddingDriver(false);
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar(prev => ({ ...prev, open: false }));
  };

  // Handle manual stats recalculation
  const handleRecalculateStats = async (driverId) => {
    try {
      await driverStatsService.recalculateDriverStats(driverId);
      
      setSnackbar({
        open: true,
        message: 'Driver stats refreshed successfully!',
        severity: 'success'
      });
      
      // Refresh the drivers list and selected driver details
      await fetchDrivers();
      
      // If the details dialog is open, refresh the selected driver
      if (selectedDriver && selectedDriver.driverId === driverId) {
        const updatedStats = await driverStatsService.getDriverStats(driverId);
        setSelectedDriver(prevDriver => ({
          ...prevDriver,
          ...(updatedStats.data || updatedStats)
        }));
      }
    } catch (error) {
      console.error('Error recalculating stats:', error);
      setSnackbar({
        open: true,
        message: 'Failed to refresh stats. Please try again.',
        severity: 'error'
      });
    }
  };

  if (loading && drivers.length === 0) {
    return <TableSkeleton rows={8} columns={6} />;
  }

  if (error) {
    return (
      <ErrorState
        title="Failed to Load Drivers"
        description={error}
        onRetry={fetchDrivers}
      />
    );
  }

  if (filteredDrivers.length === 0 && searchTerm) {
    return (
      <NoSearchResults
        searchTerm={searchTerm}
        onClear={() => setSearchTerm('')}
      />
    );
  }

  return (
    <PageTransition>
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h4" gutterBottom sx={{ mb: 0, fontWeight: 600 }}>
          Driver Management
        </Typography>
        <Box display="flex" gap={2}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={fetchDrivers}
            disabled={loading}
            size="small"
          >
            {loading ? 'Refreshing...' : 'Refresh'}
          </Button>
          {/* Test Alert Button for Demo */}
          <Button
            variant="outlined"
            color="warning"
            startIcon={<WarningIcon />}
            onClick={async () => {
              // Create test alerts for demonstration
              const testDrivers = [
                { driverId: '999', driverName: 'Test Critical Driver', emaScore: -0.7 },
                { driverId: '998', driverName: 'Test Warning Driver', emaScore: -0.4 }
              ];
              await checkDriverAlerts();
              // Also create test alerts
              const testAlerts = await alertService.checkAndTriggerAlerts(testDrivers);
              if (testAlerts.length > 0) {
                setSnackbar({
                  open: true,
                  message: `Demo: ${testAlerts.length} test alerts created! Check Alerts page.`,
                  severity: 'info'
                });
              }
            }}
            size="small"
          >
            Demo Alerts
          </Button>
          <Fab 
            color="primary" 
            aria-label="add driver"
            onClick={handleOpenAddDriver}
            sx={{ boxShadow: 2 }}
          >
            <AddIcon />
          </Fab>
        </Box>
      </Box>

      {/* Alert Monitoring Status - Simplified */}
      {alertMonitoring && (
        <Alert severity="info" sx={{ mb: 3 }} action={
          <Box display="flex" gap={1}>
            {alertsGenerated.length > 0 && (
              <Button
                color="inherit"
                size="small"
                onClick={() => navigate('/alerts')}
                startIcon={<WarningIcon />}
              >
                View Alerts ({alertsGenerated.filter(a => a.severity === 'CRITICAL').length + alertsGenerated.filter(a => a.severity === 'HIGH').length})
              </Button>
            )}
            <Button
              color="inherit"
              size="small"
              onClick={() => setAlertMonitoring(false)}
            >
              Disable
            </Button>
          </Box>
        }>
          <strong>Alert Monitoring Active:</strong> Automatically checking driver sentiment scores. 
          Alerts are generated when drivers fall below warning (-0.3) or critical (-0.6) thresholds.
          {lastAlertCheck && (
            <Typography variant="caption" sx={{ ml: 1, opacity: 0.8 }}>
              Last check: {lastAlertCheck.toLocaleTimeString()}
            </Typography>
          )}
        </Alert>
      )}

      {!alertMonitoring && (
        <Alert severity="warning" sx={{ mb: 3 }} action={
          <Button
            color="inherit"
            size="small"
            onClick={() => setAlertMonitoring(true)}
          >
            Enable
          </Button>
        }>
          <strong>Alert Monitoring Disabled:</strong> Enable monitoring to automatically detect drivers with low sentiment scores.
        </Alert>
      )}

      {/* Add Driver Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
            <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PersonAddIcon />
              Quick Add Driver
            </Typography>
            <Button
              variant="outlined"
              onClick={handleOpenAddDriver}
              startIcon={<AddIcon />}
              size="small"
            >
              Add New Driver
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary">
            Quickly add new drivers to the system. They will receive login credentials and can start receiving feedback immediately.
          </Typography>
        </CardContent>
      </Card>

      {/* Drivers List */}
      <Card>
        <CardContent>
          {/* Search Bar */}
          <TextField
            fullWidth
            variant="outlined"
            placeholder="Search by name, email, phone, license, or vehicle number..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ mb: 3 }}
          />

          {/* Statistics Summary */}
          <Box sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <Chip
              label={`Total Drivers: ${filteredDrivers.length}`}
              color="primary"
              variant="outlined"
            />
            <Chip
              label={`Critical: ${filteredDrivers.filter((d) => d.emaScore <= -0.6).length}`}
              color="error"
              variant="outlined"
              icon={<ErrorIcon />}
            />
            <Chip
              label={`Warning: ${
                filteredDrivers.filter((d) => d.emaScore > -0.6 && d.emaScore <= -0.3).length
              }`}
              color="warning"
              variant="outlined"
              icon={<WarningIcon />}
            />
            <Chip
              label={`Good: ${filteredDrivers.filter((d) => d.emaScore >= 0.2).length}`}
              color="success"
              variant="outlined"
            />
            {alertMonitoring && (
              <Chip
                label="🔍 Monitoring Active"
                color="info"
                variant="outlined"
                size="small"
              />
            )}
          </Box>

          {/* Driver Table - Made scrollable */}
          <TableContainer sx={{ maxHeight: 600, overflow: 'auto' }}>
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <TableSortLabel
                      active={orderBy === 'driverName'}
                      direction={orderBy === 'driverName' ? order : 'asc'}
                      onClick={() => handleSort('driverName')}
                    >
                      Driver Name
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>Contact Info</TableCell>
                  <TableCell align="center">
                    <TableSortLabel
                      active={orderBy === 'emaScore'}
                      direction={orderBy === 'emaScore' ? order : 'asc'}
                      onClick={() => handleSort('emaScore')}
                    >
                      Rating
                    </TableSortLabel>
                  </TableCell>
                  <TableCell align="center">
                    <TableSortLabel
                      active={orderBy === 'totalFeedbackCount'}
                      direction={orderBy === 'totalFeedbackCount' ? order : 'asc'}
                      onClick={() => handleSort('totalFeedbackCount')}
                    >
                      Feedback Count
                    </TableSortLabel>
                  </TableCell>
                  <TableCell align="center">
                    <TableSortLabel
                      active={orderBy === 'activeAlertsCount'}
                      direction={orderBy === 'activeAlertsCount' ? order : 'asc'}
                      onClick={() => handleSort('activeAlertsCount')}
                    >
                      Alerts
                    </TableSortLabel>
                  </TableCell>
                  <TableCell align="center">Trend</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {sortedDrivers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Typography color="text.secondary" sx={{ py: 3 }}>
                        {searchTerm ? 'No drivers found matching your search.' : 'No drivers available. Add your first driver using the button above.'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  sortedDrivers
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((driver) => (
                      <TableRow
                        key={driver.driverId}
                        hover
                        onClick={() => handleRowClick(driver)}
                        sx={{ cursor: 'pointer' }}
                      >
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
                              <PersonIcon fontSize="small" />
                            </Avatar>
                            <Box>
                              <Typography variant="body2" fontWeight={500}>
                                {driver.driverName || driver.name || 'Unknown'}
                              </Typography>
                              <Typography variant="caption" color="text.secondary">
                                ID: {driver.driverId || 'N/A'}
                              </Typography>
                            </Box>
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Box>
                            <Box display="flex" alignItems="center" gap={0.5}>
                              <EmailIcon fontSize="small" color="action" />
                              <Typography variant="body2" color="text.secondary">
                                {driver.email || 'N/A'}
                              </Typography>
                            </Box>
                            <Box display="flex" alignItems="center" gap={0.5}>
                              <PhoneIcon fontSize="small" color="action" />
                              <Typography variant="body2" color="text.secondary">
                                {driver.phoneNumber || 'N/A'}
                              </Typography>
                            </Box>
                          </Box>
                        </TableCell>
                        <TableCell align="center">
                          <Box display="flex" flexDirection="column" alignItems="center" gap={0.5}>
                            <Box display="flex" alignItems="center" gap={1}>
                              <Chip
                                label={`${driver.emaScore?.toFixed(2) || '0.00'}`}
                                color={getEmaColor(driver.emaScore)}
                                size="small"
                              />
                              {/* Alert indicator */}
                              {(() => {
                                const alertStatus = getAlertStatus(driver.emaScore || 0);
                                if (alertStatus.level !== 'normal') {
                                  return (
                                    <Tooltip title={`Alert: ${alertStatus.level.toUpperCase()}`}>
                                      <Chip
                                        label={alertStatus.icon}
                                        color={alertStatus.color}
                                        size="small"
                                        sx={{ 
                                          minWidth: '32px',
                                          '& .MuiChip-label': { px: 0.5 },
                                          animation: alertStatus.level === 'critical' ? 'blink 1s infinite' : 'none',
                                          '@keyframes blink': {
                                            '0%, 50%': { opacity: 1 },
                                            '51%, 100%': { opacity: 0.5 }
                                          }
                                        }}
                                      />
                                    </Tooltip>
                                  );
                                }
                                return null;
                              })()}
                            </Box>
                            <Typography variant="caption" color="text.secondary">
                              {getEmaLabel(driver.emaScore)}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell align="center">
                          <Typography variant="body2" fontWeight={500}>
                            {driver.totalFeedbackCount || 0}
                          </Typography>
                        </TableCell>
                        <TableCell align="center">
                          <Chip
                            label={driver.activeAlertsCount || 0}
                            color={driver.activeAlertsCount > 0 ? 'error' : 'default'}
                            size="small"
                          />
                        </TableCell>
                        <TableCell align="center">
                          {driver.emaScore >= 0 ? (
                            <TrendingUpIcon color="success" />
                          ) : (
                            <TrendingDownIcon color="error" />
                          )}
                        </TableCell>
                      </TableRow>
                    ))
                )}
              </TableBody>
            </Table>
          </TableContainer>

          {/* Pagination */}
          <TablePagination
            rowsPerPageOptions={[5, 10, 25, 50]}
            component="div"
            count={filteredDrivers.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
        </CardContent>
      </Card>

      {/* Driver Details Dialog */}
      <Dialog
        open={detailsOpen}
        onClose={handleCloseDetails}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h5" fontWeight={600}>
              Driver Details
            </Typography>
            <IconButton onClick={handleCloseDetails}>
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          {detailsLoading ? (
            <Box display="flex" justifyContent="center" alignItems="center" py={4}>
              <CircularProgress />
              <Typography variant="body2" sx={{ ml: 2 }}>
                Loading driver details...
              </Typography>
            </Box>
          ) : selectedDriver ? (
            <Grid container spacing={3}>
              {/* Driver Info Section */}
              <Grid item xs={12} md={6}>
                <Box display="flex" alignItems="center" gap={2} mb={2}>
                  <Avatar sx={{ width: 64, height: 64, bgcolor: 'primary.main' }}>
                    <PersonIcon fontSize="large" />
                  </Avatar>
                  <Box>
                    <Typography variant="h6" fontWeight={600}>
                      {selectedDriver.driverName || selectedDriver.name || 'Unknown Driver'}
                    </Typography>
                    <Chip
                      label={getEmaLabel(selectedDriver.emaScore || 0)}
                      color={getEmaColor(selectedDriver.emaScore || 0)}
                      size="small"
                    />
                  </Box>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Box display="flex" flexDirection="column" gap={1.5}>
                  <Box display="flex" alignItems="center" gap={1}>
                    <PersonIcon color="action" fontSize="small" />
                    <Typography variant="body2" color="text.secondary">
                      Driver ID: {selectedDriver.driverId || 'N/A'}
                    </Typography>
                  </Box>
                  <Box display="flex" alignItems="center" gap={1}>
                    <EmailIcon color="action" fontSize="small" />
                    <Typography variant="body2" color="text.secondary">
                      {selectedDriver.email || 'No email provided'}
                    </Typography>
                  </Box>
                  <Box display="flex" alignItems="center" gap={1}>
                    <PhoneIcon color="action" fontSize="small" />
                    <Typography variant="body2" color="text.secondary">
                      {selectedDriver.phoneNumber || 'No phone provided'}
                    </Typography>
                  </Box>
                  {selectedDriver.licenseNumber && (
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography variant="caption" color="text.secondary">
                        🪪 License: {selectedDriver.licenseNumber}
                      </Typography>
                    </Box>
                  )}
                  {selectedDriver.vehicleNumber && (
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography variant="caption" color="text.secondary">
                        🚗 Vehicle: {selectedDriver.vehicleNumber}
                      </Typography>
                    </Box>
                  )}
                  <Box display="flex" alignItems="center" gap={1}>
                    <CalendarIcon color="action" fontSize="small" />
                    <Typography variant="body2" color="text.secondary">
                      Joined:{' '}
                      {selectedDriver.createdAt
                        ? new Date(selectedDriver.createdAt).toLocaleDateString()
                        : 'N/A'}
                    </Typography>
                  </Box>
                  {selectedDriver.active !== undefined && (
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography variant="body2" color="text.secondary">
                        Status: 
                        <Chip 
                          label={selectedDriver.active ? 'Active' : 'Inactive'} 
                          color={selectedDriver.active ? 'success' : 'error'} 
                          size="small" 
                          sx={{ ml: 1 }}
                        />
                      </Typography>
                    </Box>
                  )}
                </Box>
              </Grid>

              {/* Statistics Section */}
              <Grid item xs={12} md={6}>
                <Box display="flex" alignItems="center" gap={1} mb={2}>
                  <AssessmentIcon color="primary" />
                  <Typography variant="h6" fontWeight={600}>
                    Performance Metrics
                  </Typography>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Card variant="outlined" sx={{ bgcolor: '#e3f2fd', border: '1px solid #90caf9' }}>
                      <CardContent>
                        <Typography variant="caption" color="#1565c0">
                          EMA Score
                        </Typography>
                        <Typography variant="h5" fontWeight={600} color="#0d47a1">
                          {selectedDriver.emaScore?.toFixed(2) || '0.00'}
                        </Typography>
                        <Typography variant="caption" color="#1976d2">
                          {getEmaLabel(selectedDriver.emaScore || 0)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={6}>
                    <Card variant="outlined" sx={{ bgcolor: '#f3e5f5', border: '1px solid #ce93d8' }}>
                      <CardContent>
                        <Typography variant="caption" color="#6a1b9a">
                          Total Feedback
                        </Typography>
                        <Typography variant="h5" fontWeight={600} color="#4a148c">
                          {selectedDriver.totalFeedbackCount || 0}
                        </Typography>
                        <Typography variant="caption" color="#7b1fa2">
                          Reviews received
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={6}>
                    <Card variant="outlined" sx={{ bgcolor: '#e8f5e8', border: '1px solid #a5d6a7' }}>
                      <CardContent>
                        <Typography variant="caption" color="#2e7d32">
                          Positive
                        </Typography>
                        <Typography variant="h5" fontWeight={600} color="#1b5e20">
                          {selectedDriver.positiveFeedbackCount || 0}
                        </Typography>
                        <Typography variant="caption" color="#388e3c">
                          Good reviews
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={6}>
                    <Card variant="outlined" sx={{ bgcolor: '#ffebee', border: '1px solid #ef9a9a' }}>
                      <CardContent>
                        <Typography variant="caption" color="#c62828">
                          Negative
                        </Typography>
                        <Typography variant="h5" fontWeight={600} color="#b71c1c">
                          {selectedDriver.negativeFeedbackCount || 0}
                        </Typography>
                        <Typography variant="caption" color="#d32f2f">
                          Critical reviews
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={6}>
                    <Card variant="outlined" sx={{ bgcolor: '#f5f5f5', border: '1px solid #bdbdbd' }}>
                      <CardContent>
                        <Typography variant="caption" color="#424242">
                          Neutral
                        </Typography>
                        <Typography variant="h5" fontWeight={600} color="#212121">
                          {selectedDriver.neutralFeedbackCount || 0}
                        </Typography>
                        <Typography variant="caption" color="#616161">
                          Neutral reviews
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={6}>
                    <Card variant="outlined" sx={{ bgcolor: '#fff8e1', border: '1px solid #ffcc02' }}>
                      <CardContent>
                        <Typography variant="caption" color="#f57f17">
                          Active Alerts
                        </Typography>
                        <Typography variant="h5" fontWeight={600} color="#e65100">
                          {selectedDriver.activeAlertsCount || 0}
                        </Typography>
                        <Typography variant="caption" color="#ff8f00">
                          Need attention
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </Grid>

              {/* Additional Info */}
              <Grid item xs={12}>
                <Typography variant="caption" color="text.secondary">
                  Last Feedback:{' '}
                  {selectedDriver.lastFeedbackDate
                    ? new Date(selectedDriver.lastFeedbackDate).toLocaleString()
                    : 'No feedback yet'}
                </Typography>
                
                {selectedDriver.driverId && (
                  <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
                    <Button
                      size="small"
                      variant="outlined"
                      onClick={() => handleRecalculateStats(selectedDriver.driverId)}
                      startIcon={<RefreshIcon />}
                    >
                      Refresh Stats
                    </Button>
                  </Box>
                )}
              </Grid>
            </Grid>
          ) : (
            <Typography variant="body2" color="text.secondary" align="center">
              No driver data available
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDetails}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Add Driver Dialog */}
      <Dialog
        open={addDriverOpen}
        onClose={handleCloseAddDriver}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <PersonAddIcon color="primary" />
            <Typography variant="h5" fontWeight={600}>
              Add New Driver
            </Typography>
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Full Name"
                value={newDriverData.name}
                onChange={handleInputChange('name')}
                error={!!formErrors.name}
                helperText={formErrors.name}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Email"
                type="email"
                value={newDriverData.email}
                onChange={handleInputChange('email')}
                error={!!formErrors.email}
                helperText={formErrors.email}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Driver ID"
                value={newDriverData.driverId}
                onChange={handleInputChange('driverId')}
                error={!!formErrors.driverId}
                helperText={formErrors.driverId || 'Unique identifier for the driver (e.g., 1001, 1002, etc.)'}
                placeholder="1001"
                type="number"
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Phone Number"
                value={newDriverData.phoneNumber}
                onChange={handleInputChange('phoneNumber')}
                error={!!formErrors.phoneNumber}
                helperText={formErrors.phoneNumber}
                placeholder="+91-XXXXXXXXXX"
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="License Number"
                value={newDriverData.licenseNumber}
                onChange={handleInputChange('licenseNumber')}
                error={!!formErrors.licenseNumber}
                helperText={formErrors.licenseNumber}
                placeholder="DL-XXXXXXXXXX"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Vehicle Number"
                value={newDriverData.vehicleNumber}
                onChange={handleInputChange('vehicleNumber')}
                error={!!formErrors.vehicleNumber}
                helperText={formErrors.vehicleNumber}
                placeholder="XX-00-XX-0000"
              />
            </Grid>
          </Grid>
          
          <Alert severity="info" sx={{ mt: 2 }}>
            <AlertTitle>Login Information</AlertTitle>
            The driver will receive login credentials via email. A default password will be generated and sent to their email address.
          </Alert>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button 
            onClick={handleCloseAddDriver}
            disabled={addingDriver}
            startIcon={<CancelIcon />}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={handleAddDriver}
            disabled={addingDriver}
            startIcon={addingDriver ? <CircularProgress size={16} /> : <SaveIcon />}
          >
            {addingDriver ? 'Adding...' : 'Add Driver'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        action={
          snackbar.severity === 'error' || snackbar.severity === 'warning' ? (
            <Button 
              color="inherit" 
              size="small" 
              onClick={() => {
                navigate('/alerts');
                handleCloseSnackbar();
              }}
              sx={{ mr: 1 }}
            >
              View Alerts
            </Button>
          ) : null
        }
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
    </PageTransition>
  );
};

export default DriversPage;
