import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Rating,
  Chip,
  CircularProgress,
  Alert,
  Pagination,
  Grid,
  Paper,
  Stack,
  Divider
} from '@mui/material';
import { 
  Feedback as FeedbackIcon, 
  DriveEta as DriverIcon,
  AccessTime as TimeIcon,
  Person as PersonIcon
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../services/api';
import { formatDistanceToNow } from 'date-fns';

const MyFeedbackPage = () => {
  const { user } = useAuth();
  const [feedbackData, setFeedbackData] = useState({
    content: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);

  const fetchMyFeedback = async (pageNumber = 0) => {
    try {
      setLoading(true);
      setError('');
      
      const response = await api.get(`/feedback/my-feedback?page=${pageNumber}&size=10`);
      
      if (response.success) {
        setFeedbackData({
          content: response.data.content || [],
          totalElements: response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0,
          currentPage: response.data.number || 0
        });
      } else {
        setError(response.message || 'Failed to fetch feedback');
      }
    } catch (error) {
      console.error('Error fetching my feedback:', error);
      setError('Unable to load your feedback history. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyFeedback(page);
  }, [page]);

  const handlePageChange = (event, value) => {
    const newPage = value - 1; // MUI pagination is 1-based, backend is 0-based
    setPage(newPage);
  };

  // Use OpenRouter AI sentiment analysis results instead of rating
  const getSentimentColor = (feedback) => {
    // Prefer AI sentiment analysis over rating
    if (feedback.sentimentLabel) {
      const label = feedback.sentimentLabel.toLowerCase();
      if (label.includes('positive')) return 'success';
      if (label.includes('neutral')) return 'warning';
      if (label.includes('negative')) return 'error';
    }
    
    // Fallback to rating-based sentiment if AI analysis not available
    const rating = feedback.rating || 0;
    if (rating >= 4) return 'success';
    if (rating >= 3) return 'warning';
    return 'error';
  };

  const getSentimentLabel = (feedback) => {
    // Prefer AI sentiment analysis over rating
    if (feedback.sentimentLabel) {
      const label = feedback.sentimentLabel;
      // Convert backend enum values to user-friendly labels
      switch (label) {
        case 'POSITIVE':
        case 'VERY_POSITIVE':
          return 'Positive';
        case 'NEGATIVE':
        case 'VERY_NEGATIVE':
          return 'Negative';
        case 'NEUTRAL':
        default:
          return 'Neutral';
      }
    }
    
    // Fallback to rating-based sentiment if AI analysis not available
    const rating = feedback.rating || 0;
    if (rating >= 4) return 'Positive';
    if (rating >= 3) return 'Neutral';
    return 'Negative';
  };

  const getSentimentDetails = (feedback) => {
    if (feedback.sentimentScore !== null && feedback.sentimentScore !== undefined) {
      return {
        score: feedback.sentimentScore,
        confidence: feedback.confidence,
        keywords: feedback.keywords,
        aiAnalysis: true
      };
    }
    return { aiAnalysis: false };
  };

  if (loading && feedbackData.content.length === 0) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress size={60} />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Paper elevation={1} sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)', color: 'white' }}>
        <Stack direction="row" alignItems="center" spacing={2}>
          <FeedbackIcon sx={{ fontSize: 40 }} />
          <Box>
            <Typography variant="h4" component="h1" fontWeight="bold">
              My Feedback History
            </Typography>
            <Typography variant="h6" sx={{ opacity: 0.9 }}>
              Track all feedback you've submitted to drivers
            </Typography>
          </Box>
        </Stack>
      </Paper>

      {/* Stats Summary */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={2}>
                <FeedbackIcon color="primary" sx={{ fontSize: 30 }} />
                <Box>
                  <Typography variant="h6" fontWeight="bold">
                    {feedbackData.totalElements}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total Feedback
                  </Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={2}>
                <PersonIcon color="success" sx={{ fontSize: 30 }} />
                <Box>
                  <Typography variant="h6" fontWeight="bold">
                    {user?.name || 'User'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Your Profile
                  </Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={2}>
                <DriverIcon color="info" sx={{ fontSize: 30 }} />
                <Box>
                  <Typography variant="h6" fontWeight="bold">
                    {feedbackData.content.reduce((acc, feedback) => {
                      return acc.includes(feedback.driverId) ? acc : [...acc, feedback.driverId];
                    }, []).length}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Drivers Rated
                  </Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={2}>
                <FeedbackIcon color="warning" sx={{ fontSize: 30 }} />
                <Box>
                  <Typography variant="h6" fontWeight="bold">
                    {feedbackData.content.length > 0 
                      ? (feedbackData.content.reduce((sum, f) => sum + f.rating, 0) / feedbackData.content.length).toFixed(1)
                      : '0.0'
                    }
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Avg Rating
                  </Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Feedback List */}
      {feedbackData.content.length === 0 ? (
        <Paper sx={{ p: 6, textAlign: 'center' }}>
          <FeedbackIcon sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h5" gutterBottom color="text.secondary">
            No Feedback Found
          </Typography>
          <Typography variant="body1" color="text.secondary">
            You haven't submitted any feedback yet. Start giving feedback to drivers to see your history here.
          </Typography>
        </Paper>
      ) : (
        <>
          <Grid container spacing={3}>
            {feedbackData.content.map((feedback) => (
              <Grid item xs={12} key={feedback.id}>
                <Card elevation={2} sx={{ '&:hover': { elevation: 4 } }}>
                  <CardContent sx={{ p: 3 }}>
                    <Grid container spacing={3} alignItems="center">
                      {/* Left Section - Feedback Details */}
                      <Grid item xs={12} md={8}>
                        <Stack spacing={2}>
                          {/* Header */}
                          <Stack direction="row" alignItems="center" spacing={2}>
                            <FeedbackIcon color="primary" />
                            <Typography variant="h6" fontWeight="bold">
                              Feedback #{feedback.id}
                            </Typography>
                            <Chip 
                              label={getSentimentLabel(feedback)}
                              color={getSentimentColor(feedback)}
                              size="small"
                            />
                            {/* Show AI Analysis Indicator */}
                            {getSentimentDetails(feedback).aiAnalysis && (
                              <Chip 
                                label="AI Analyzed" 
                                size="small" 
                                variant="outlined"
                                color="info"
                                sx={{ fontSize: '0.7rem' }}
                              />
                            )}
                          </Stack>

                          {/* Driver Info */}
                          <Stack direction="row" alignItems="center" spacing={1}>
                            <DriverIcon color="action" sx={{ fontSize: 20 }} />
                            <Typography variant="body1">
                              <strong>Driver ID:</strong> {feedback.driverId}
                            </Typography>
                          </Stack>

                          {/* Rating */}
                          <Stack direction="row" alignItems="center" spacing={2}>
                            <Typography variant="body1" fontWeight="medium">
                              Rating:
                            </Typography>
                            <Rating 
                              value={feedback.rating} 
                              readOnly 
                              size="small"
                            />
                            <Typography variant="body1" color="text.secondary">
                              ({feedback.rating}/5)
                            </Typography>
                          </Stack>

                          {/* Comments */}
                          {feedback.feedbackText && (
                            <Box>
                              <Typography variant="body2" color="text.secondary" gutterBottom>
                                Feedback:
                              </Typography>
                              <Typography variant="body1" sx={{ 
                                fontStyle: 'italic',
                                p: 2,
                                backgroundColor: 'grey.50',
                                borderRadius: 1,
                                border: '1px solid',
                                borderColor: 'grey.200'
                              }}>
                                "{feedback.feedbackText}"
                              </Typography>
                              
                              {/* OpenRouter AI Sentiment Analysis Details */}
                              {(() => {
                                const sentimentDetails = getSentimentDetails(feedback);
                                if (sentimentDetails.aiAnalysis) {
                                  return (
                                    <Box sx={{ mt: 2, p: 2, backgroundColor: 'blue.50', borderRadius: 1, border: '1px solid', borderColor: 'blue.200' }}>
                                      <Typography variant="body2" color="primary.main" fontWeight="bold" gutterBottom>
                                        🤖 AI Sentiment Analysis (OpenRouter GPT-4o-mini)
                                      </Typography>
                                      <Stack direction="row" spacing={2} flexWrap="wrap">
                                        <Typography variant="caption" color="text.secondary">
                                          <strong>Score:</strong> {(sentimentDetails.score * 100).toFixed(1)}%
                                        </Typography>
                                        {sentimentDetails.confidence && (
                                          <Typography variant="caption" color="text.secondary">
                                            <strong>Confidence:</strong> {(sentimentDetails.confidence * 100).toFixed(1)}%
                                          </Typography>
                                        )}
                                        {sentimentDetails.keywords && sentimentDetails.keywords.length > 0 && (
                                          <Typography variant="caption" color="text.secondary">
                                            <strong>Keywords:</strong> {sentimentDetails.keywords.join(', ')}
                                          </Typography>
                                        )}
                                      </Stack>
                                    </Box>
                                  );
                                }
                                return null;
                              })()}
                            </Box>
                          )}
                        </Stack>
                      </Grid>

                      {/* Right Section - Metadata */}
                      <Grid item xs={12} md={4}>
                        <Stack spacing={2} alignItems={{ xs: 'flex-start', md: 'flex-end' }}>
                          {/* Timestamp */}
                          <Stack direction="row" alignItems="center" spacing={1}>
                            <TimeIcon color="action" sx={{ fontSize: 20 }} />
                            <Box textAlign={{ xs: 'left', md: 'right' }}>
                              <Typography variant="body2" color="text.secondary">
                                {formatDistanceToNow(new Date(feedback.createdAt), { addSuffix: true })}
                              </Typography>
                              <Typography variant="caption" color="text.secondary">
                                {new Date(feedback.createdAt).toLocaleString()}
                              </Typography>
                            </Box>
                          </Stack>

                          {/* Feedback Type */}
                          {feedback.feedbackType && (
                            <Chip 
                              label={feedback.feedbackType}
                              variant="outlined"
                              size="small"
                              color="primary"
                            />
                          )}
                        </Stack>
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>

          {/* Pagination */}
          {feedbackData.totalPages > 1 && (
            <Box display="flex" justifyContent="center" sx={{ mt: 4 }}>
              <Pagination
                count={feedbackData.totalPages}
                page={feedbackData.currentPage + 1} // MUI pagination is 1-based
                onChange={handlePageChange}
                color="primary"
                size="large"
                showFirstButton
                showLastButton
              />
            </Box>
          )}
        </>
      )}

      {loading && feedbackData.content.length > 0 && (
        <Box display="flex" justifyContent="center" sx={{ mt: 2 }}>
          <CircularProgress size={30} />
        </Box>
      )}
    </Container>
  );
};

export default MyFeedbackPage;