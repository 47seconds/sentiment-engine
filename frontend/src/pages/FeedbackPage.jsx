import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Rating,
  Alert,
  CircularProgress,
  Autocomplete,
  Chip,
  Grid,
  Paper,
  Divider,
  FormHelperText,
  InputAdornment,
} from '@mui/material';
import {
  Send as SendIcon,
  Refresh as RefreshIcon,
  RateReview as ReviewIcon,
  Person as PersonIcon,
  DirectionsCar as TripIcon,
  PhoneAndroid as AppIcon,
  Support as MarshalIcon,
} from '@mui/icons-material';
import * as feedbackService from '../services/feedbackService';
import * as driverService from '../services/driverService';
import toast from 'react-hot-toast';
import { FormSkeleton } from '../components/LoadingSkeletons';
import PageTransition from '../components/PageTransition';
import { ErrorState } from '../components/EmptyStates';
import { useAuth } from '../contexts/AuthContext';

const FeedbackPage = () => {
  const { user, isAdmin } = useAuth();
  
  // Form state
  const [entityType, setEntityType] = useState('EMPLOYEE');
  const [entityId, setEntityId] = useState('');
  const [feedbackText, setFeedbackText] = useState('');
  const [rating, setRating] = useState(0);
  const [source, setSource] = useState('WEB_PORTAL');

  // UI state
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [drivers, setDrivers] = useState([]);
  const [selectedDriver, setSelectedDriver] = useState(null);
  
  // Validation state
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [validatingDriverId, setValidatingDriverId] = useState(false);
  const [driverIdValid, setDriverIdValid] = useState(null);

  // Feature flags - Enable all feedback types
  const [featureFlags] = useState({
    employeeFeedbackEnabled: true,
    tripFeedbackEnabled: true,
    appFeedbackEnabled: true,
    marshalFeedbackEnabled: true,
  });

  // Success state
  const [showSuccess, setShowSuccess] = useState(false);

  // Load drivers on mount - Removed as we're using simple driver ID input for all users
  useEffect(() => {
    // No longer loading driver stats - using simple ID input for all users
    setDrivers([]); // Keep empty for consistent behavior
    setLoading(false);
  }, [entityType, isAdmin]);

  // Clear driver validation state when entity type changes
  useEffect(() => {
    if (entityType !== 'EMPLOYEE') {
      setDriverIdValid(null);
      setValidatingDriverId(false);
    }
  }, [entityType]);

  // Get available entity types based on feature flags
  const getAvailableEntityTypes = () => {
    const types = [];
    if (featureFlags.employeeFeedbackEnabled) {
      types.push({ value: 'EMPLOYEE', label: 'Driver', icon: <PersonIcon /> });
    }
    if (featureFlags.tripFeedbackEnabled) {
      types.push({ value: 'TRIP', label: 'Trip', icon: <TripIcon /> });
    }
    if (featureFlags.appFeedbackEnabled) {
      types.push({ value: 'MOBILE_APP', label: 'Mobile App', icon: <AppIcon /> });
    }
    if (featureFlags.marshalFeedbackEnabled) {
      types.push({ value: 'MARSHAL', label: 'Marshal', icon: <MarshalIcon /> });
    }
    return types;
  };

  // Validate driver ID
  const validateDriverId = async (driverId) => {
    if (!driverId || driverId.trim() === '') {
      setDriverIdValid(null);
      return;
    }

    try {
      setValidatingDriverId(true);
      const result = await driverService.validateDriverId(driverId.trim());
      
      if (result.exists && result.data.valid !== false) {
        setDriverIdValid(true);
        // Clear any existing error for this field
        setErrors(prev => ({ ...prev, entityId: null }));
      } else {
        setDriverIdValid(false);
        setErrors(prev => ({ ...prev, entityId: 'Enter valid driver ID' }));
      }
    } catch (error) {
      console.error('Error validating driver ID:', error);
      // On error, assume valid to not block legitimate submissions
      setDriverIdValid(true);
    } finally {
      setValidatingDriverId(false);
    }
  };

  // Validation
  const validateForm = () => {
    const newErrors = {};

    if (!entityType) {
      newErrors.entityType = 'Entity type is required';
    }

    // Require driver/employee selection for EMPLOYEE entity type
    if (entityType === 'EMPLOYEE' && !entityId) {
      newErrors.entityId = 'Please enter a driver ID';
    }

    // Validate driver ID if it's an EMPLOYEE type and ID is provided
    if (entityType === 'EMPLOYEE' && entityId && driverIdValid === false) {
      newErrors.entityId = 'Enter valid driver ID';
    }

    // For other entity types, require entity ID only for admin users
    if (entityType !== 'EMPLOYEE' && !entityId && isAdmin()) {
      newErrors.entityId = `Please enter ${entityType.toLowerCase().replace('_', ' ')} ID`;
    }

    if (!feedbackText.trim()) {
      newErrors.feedbackText = 'Feedback text is required';
    } else if (feedbackText.trim().length < 10) {
      newErrors.feedbackText = 'Feedback must be at least 10 characters';
    } else if (feedbackText.trim().length > 1000) {
      newErrors.feedbackText = 'Feedback must not exceed 1000 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle field blur (for validation)
  const handleBlur = (field) => {
    setTouched({ ...touched, [field]: true });
    validateForm();
  };

  // Handle submit
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Mark all fields as touched
    setTouched({
      entityType: true,
      entityId: true,
      feedbackText: true,
    });

    // Validate
    if (!validateForm()) {
      toast.error('Please fix the errors before submitting');
      return;
    }

    // Additional check for driver ID validation when it's EMPLOYEE type
    if (entityType === 'EMPLOYEE' && entityId && driverIdValid === false) {
      toast.error('Enter valid driver ID');
      return;
    }

    // Ensure driver ID validation has completed for EMPLOYEE type
    if (entityType === 'EMPLOYEE' && entityId && driverIdValid === null) {
      toast.error('Please wait for driver ID validation to complete');
      return;
    }

    try {
      setSubmitting(true);

      // Use driver ID for EMPLOYEE type, otherwise use entityId
      // For EMPLOYEE feedback, get the driver ID from the input
      let finalEntityId;
      if (entityType === 'EMPLOYEE') {
        if (entityId) {
          // Use entered driver ID
          finalEntityId = parseInt(entityId, 10);
          if (isNaN(finalEntityId)) {
            toast.error('Please enter a valid driver ID');
            return;
          }
        } else {
          // Fallback to default driver ID
          finalEntityId = 21;
        }
      } else {
        finalEntityId = entityId;
      }

      // Ensure finalEntityId is a number
      if (typeof finalEntityId === 'string') {
        const parsed = parseInt(finalEntityId, 10);
        finalEntityId = isNaN(parsed) ? 21 : parsed;
      }

      // Determine feedbackType based on rating and entityType
      let feedbackType;
      if (rating >= 4) {
        feedbackType = 'POSITIVE_PRAISE';
      } else if (rating <= 2) {
        feedbackType = 'COMPLAINT';
      } else if (entityType === 'EMPLOYEE') {
        feedbackType = 'EMPLOYEE';
      } else if (entityType === 'TRIP') {
        feedbackType = 'TRIP';
      } else if (entityType === 'MOBILE_APP') {
        feedbackType = 'MOBILE_APP';
      } else if (entityType === 'MARSHAL') {
        feedbackType = 'MARSHAL';
      } else {
        feedbackType = 'GENERAL_EXPERIENCE';
      }

      const feedbackData = {
        userId: user?.id,
        driverId: entityType === 'EMPLOYEE' ? finalEntityId : null,
        entityType,
        entityId: finalEntityId,
        feedbackText: feedbackText.trim(),
        rating: rating || null,
        feedbackType,
        source,
      };

      await feedbackService.submitFeedback(feedbackData);
      
      toast.success('Feedback submitted successfully!');
      setShowSuccess(true);
      
      // Reset form after 2 seconds
      setTimeout(() => {
        handleReset();
      }, 2000);

    } catch (error) {
      console.error('Error submitting feedback:', error);
      toast.error(error.response?.data?.message || 'Failed to submit feedback');
    } finally {
      setSubmitting(false);
    }
  };

  // Handle reset
  const handleReset = () => {
    setEntityType('EMPLOYEE');
    setEntityId('');
    setSelectedDriver(null);
    setFeedbackText('');
    setRating(0);
    setSource('WEB_PORTAL');
    setErrors({});
    setTouched({});
    setShowSuccess(false);
    // Reset driver ID validation state
    setDriverIdValid(null);
    setValidatingDriverId(false);
  };

  // Get entity type icon
  const getEntityIcon = (type) => {
    switch (type) {
      case 'EMPLOYEE': return <PersonIcon />;
      case 'TRIP': return <TripIcon />;
      case 'MOBILE_APP': return <AppIcon />;
      case 'MARSHAL': return <MarshalIcon />;
      default: return <ReviewIcon />;
    }
  };

  const availableTypes = getAvailableEntityTypes();
  const characterCount = feedbackText.length;
  const isOverLimit = characterCount > 1000;
  const isUnderLimit = characterCount > 0 && characterCount < 10;

  if (loading && drivers.length === 0) {
    return <FormSkeleton />;
  }

  return (
    <PageTransition>
    <Box>
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
        <ReviewIcon sx={{ fontSize: 40, color: 'primary.main' }} />
        <Box>
          <Typography variant="h4" gutterBottom>
            Submit Feedback
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Share your experience to help us improve our service
          </Typography>
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* Feedback Form */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              {showSuccess && (
                <Alert severity="success" sx={{ mb: 3 }} onClose={() => setShowSuccess(false)}>
                  Your feedback has been submitted successfully! Thank you for your input.
                </Alert>
              )}

              <form onSubmit={handleSubmit}>
                {/* Entity Type Selection */}
                <FormControl 
                  fullWidth 
                  sx={{ mb: 3 }}
                  error={touched.entityType && !!errors.entityType}
                >
                  <InputLabel>Feedback Type</InputLabel>
                  <Select
                    value={entityType}
                    label="Feedback Type"
                    onChange={(e) => {
                      setEntityType(e.target.value);
                      setSelectedDriver(null);
                      setEntityId('');
                    }}
                    onBlur={() => handleBlur('entityType')}
                  >
                    {availableTypes.map((type) => (
                      <MenuItem key={type.value} value={type.value}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          {type.icon}
                          {type.label}
                        </Box>
                      </MenuItem>
                    ))}
                  </Select>
                  {touched.entityType && errors.entityType && (
                    <FormHelperText>{errors.entityType}</FormHelperText>
                  )}
                </FormControl>

                {/* Driver Selection (for EMPLOYEE type) - Simplified for all users */}
                {entityType === 'EMPLOYEE' && (
                  <FormControl 
                    fullWidth 
                    sx={{ mb: 3 }}
                    error={touched.entityId && !!errors.entityId}
                  >
                    <TextField
                      fullWidth
                      label="Driver ID"
                      placeholder="Enter driver ID (e.g., 21)"
                      value={entityId}
                      onChange={(e) => {
                        setEntityId(e.target.value);
                        setSelectedDriver(null);
                        setDriverIdValid(null); // Reset validation state
                        // Validate driver ID after a short delay (debounce)
                        setTimeout(() => {
                          if (e.target.value && e.target.value.trim() !== '') {
                            validateDriverId(e.target.value);
                          }
                        }, 500);
                      }}
                      onBlur={() => {
                        handleBlur('entityId');
                        if (entityId && entityId.trim() !== '') {
                          validateDriverId(entityId);
                        }
                      }}
                      error={touched.entityId && !!errors.entityId}
                      helperText={
                        validatingDriverId ? 'Validating driver ID...' :
                        driverIdValid === true ? 'Driver ID is valid' :
                        driverIdValid === false ? 'Enter valid driver ID' :
                        (touched.entityId && errors.entityId) || 'Enter the ID of the driver you want to provide feedback for'
                      }
                      type="number"
                      InputProps={{
                        endAdornment: validatingDriverId ? (
                          <CircularProgress size={20} />
                        ) : driverIdValid === true ? (
                          <Box sx={{ color: 'success.main', display: 'flex', alignItems: 'center' }}>
                            ✓
                          </Box>
                        ) : driverIdValid === false ? (
                          <Box sx={{ color: 'error.main', display: 'flex', alignItems: 'center' }}>
                            ✗
                          </Box>
                        ) : null
                      }}
                      FormHelperTextProps={{
                        sx: {
                          color: 
                            validatingDriverId ? 'info.main' :
                            driverIdValid === true ? 'success.main' :
                            driverIdValid === false ? 'error.main' :
                            'text.secondary'
                        }
                      }}
                    />
                  </FormControl>
                )}

                {/* Entity ID (for non-driver types) */}
                {entityType !== 'EMPLOYEE' && (
                  <TextField
                    fullWidth
                    label={`${entityType === 'MOBILE_APP' ? 'App Feature/Issue' : entityType.replace('_', ' ')} ID`}
                    value={entityId}
                    onChange={(e) => setEntityId(e.target.value)}
                    onBlur={() => handleBlur('entityId')}
                    error={touched.entityId && !!errors.entityId}
                    helperText={touched.entityId && errors.entityId}
                    sx={{ mb: 3 }}
                    placeholder={
                      entityType === 'TRIP' ? 'Enter trip ID or reference number' :
                      entityType === 'MOBILE_APP' ? 'Describe app feature or issue' :
                      entityType === 'MARSHAL' ? 'Enter marshal ID or name' :
                      `Enter ${entityType.toLowerCase().replace('_', ' ')} identifier`
                    }
                  />
                )}

                {/* Feedback Text */}
                <TextField
                  fullWidth
                  multiline
                  rows={6}
                  label="Your Feedback"
                  value={feedbackText}
                  onChange={(e) => setFeedbackText(e.target.value)}
                  onBlur={() => handleBlur('feedbackText')}
                  error={touched.feedbackText && (!!errors.feedbackText || isOverLimit)}
                  helperText={
                    (touched.feedbackText && errors.feedbackText) ||
                    `${characterCount}/1000 characters${isUnderLimit ? ' (minimum 10)' : ''}`
                  }
                  sx={{ mb: 3 }}
                  placeholder="Share your experience in detail..."
                  FormHelperTextProps={{
                    sx: {
                      color: isOverLimit ? 'error.main' : isUnderLimit ? 'warning.main' : 'text.secondary'
                    }
                  }}
                />

                {/* Rating */}
                <Box sx={{ mb: 3 }}>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Rating (Optional)
                  </Typography>
                  <Rating
                    value={rating}
                    onChange={(event, newValue) => setRating(newValue)}
                    size="large"
                    precision={1}
                  />
                  {rating > 0 && (
                    <Typography variant="caption" color="text.secondary" sx={{ ml: 2 }}>
                      {rating} star{rating !== 1 ? 's' : ''}
                    </Typography>
                  )}
                </Box>

                {/* Source Selection */}
                <FormControl fullWidth sx={{ mb: 3 }}>
                  <InputLabel>Feedback Source</InputLabel>
                  <Select
                    value={source}
                    label="Feedback Source"
                    onChange={(e) => setSource(e.target.value)}
                  >
                    <MenuItem value="WEB_PORTAL">Web Portal</MenuItem>
                    <MenuItem value="MOBILE_APP">Mobile App</MenuItem>
                    <MenuItem value="CALL_CENTER">Call Center</MenuItem>
                    <MenuItem value="EMAIL">Email</MenuItem>
                    <MenuItem value="IN_PERSON">In Person</MenuItem>
                  </Select>
                </FormControl>

                <Divider sx={{ mb: 3 }} />

                {/* Action Buttons */}
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                  <Button
                    variant="outlined"
                    onClick={handleReset}
                    startIcon={<RefreshIcon />}
                    disabled={submitting}
                  >
                    Reset
                  </Button>
                  <Button
                    variant="contained"
                    type="submit"
                    startIcon={submitting ? <CircularProgress size={20} /> : <SendIcon />}
                    disabled={submitting}
                  >
                    {submitting ? 'Submitting...' : 'Submit Feedback'}
                  </Button>
                </Box>
              </form>
            </CardContent>
          </Card>
        </Grid>

        {/* Info Panel */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, mb: 3, bgcolor: 'primary.main', color: 'white' }}>
            <Typography variant="h6" gutterBottom>
              Why Feedback Matters
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.9 }}>
              Your feedback helps us identify issues quickly, improve employee performance, 
              enhance trip quality, fix app bugs, and ensure excellent service. Every submission 
              is reviewed and contributes to our continuous improvement process.
            </Typography>
          </Paper>

          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom color="text.primary">
              Guidelines
            </Typography>
            <Box component="ul" sx={{ pl: 2, m: 0 }}>
              <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                Be specific and detailed in your feedback
              </Typography>
              <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                Minimum 10 characters required
              </Typography>
              <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                Include relevant details (date, time, location)
              </Typography>
              <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                Remain professional and constructive
              </Typography>
              <Typography component="li" variant="body2">
                Rating is optional but appreciated
              </Typography>
            </Box>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom color="text.primary">
              Available Types
            </Typography>
            {availableTypes.map((type) => (
              <Chip
                key={type.value}
                icon={type.icon}
                label={type.label}
                color={entityType === type.value ? 'primary' : 'default'}
                sx={{ mr: 1, mb: 1 }}
                onClick={() => {
                  setEntityType(type.value);
                  setSelectedDriver(null);
                  setEntityId('');
                }}
              />
            ))}
            {availableTypes.length === 1 && (
              <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
                Other feedback types are currently disabled
              </Typography>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
    </PageTransition>
  );
};

export default FeedbackPage;
