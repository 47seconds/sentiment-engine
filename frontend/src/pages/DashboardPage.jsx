import React from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Alert,
  Paper,
  useTheme,
} from '@mui/material';
import {
  PieChart,
  Pie,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Cell,
} from 'recharts';
import {
  People as PeopleIcon,
  TrendingUp as TrendingUpIcon,
  Warning as WarningIcon,
  Feedback as FeedbackIcon,
} from '@mui/icons-material';
import useFetchOverallStats from '../hooks/useFetchOverallStats';
import useFetchActiveAlerts from '../hooks/useFetchActiveAlerts';
import useFetchRecentFeedback from '../hooks/useFetchRecentFeedback';
import { DashboardSkeleton } from '../components/LoadingSkeletons';
import PageTransition from '../components/PageTransition';
import { EmptyState } from '../components/EmptyStates';

function DashboardPage() {
  const theme = useTheme();
  const { stats, loading: statsLoading, error: statsError } = useFetchOverallStats();
  const { alerts, loading: alertsLoading } = useFetchActiveAlerts();
  const { feedback, loading: feedbackLoading } = useFetchRecentFeedback(5);

  // Show loading skeleton
  if (statsLoading && !stats) {
    return <DashboardSkeleton />;
  }

  // Show error state
  if (statsError) {
    return (
      <EmptyState
        title="Failed to Load Dashboard"
        description="Unable to fetch dashboard data. Please check your connection and try again."
        action={() => window.location.reload()}
        actionLabel="Retry"
      />
    );
  }

  // KPI Card Component
  const KPICard = ({ title, value, icon: Icon, color, loading }) => (
    <Card 
      sx={{ 
        height: '100%', 
        position: 'relative', 
        overflow: 'visible',
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: 4,
        },
      }}
    >
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {title}
            </Typography>
            {loading ? (
              <CircularProgress size={24} />
            ) : (
              <Typography variant="h3" sx={{ fontWeight: 600, color }}>
                {value}
              </Typography>
            )}
          </Box>
          <Box
            sx={{
              width: 64,
              height: 64,
              borderRadius: 2,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              bgcolor: `${color}15`,
            }}
          >
            <Icon sx={{ fontSize: 32, color }} />
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  // Sentiment Distribution Data
  const sentimentData = stats
    ? [
        { name: 'Positive', value: stats.positiveDrivers || 0, color: theme.palette.success.main },
        { name: 'Neutral', value: stats.neutralDrivers || 0, color: theme.palette.grey[500] },
        { name: 'Negative', value: stats.negativeDrivers || 0, color: theme.palette.error.main },
      ]
    : [];

  // Alert Severity Distribution Data
  const alertSeverityData = [
    { name: 'Critical', count: alerts?.filter(a => a.severity === 'CRITICAL').length || 0, color: theme.palette.error.main },
    { name: 'High', count: alerts?.filter(a => a.severity === 'HIGH').length || 0, color: theme.palette.warning.main },
    { name: 'Medium', count: alerts?.filter(a => a.severity === 'MEDIUM').length || 0, color: theme.palette.info.main },
    { name: 'Low', count: alerts?.filter(a => a.severity === 'LOW').length || 0, color: theme.palette.success.main },
  ];

  // Mock trend data (replace with real API call when available)
  const trendData = [
    { date: 'Mon', sentiment: 0.45 },
    { date: 'Tue', sentiment: 0.52 },
    { date: 'Wed', sentiment: 0.38 },
    { date: 'Thu', sentiment: 0.61 },
    { date: 'Fri', sentiment: 0.55 },
    { date: 'Sat', sentiment: 0.48 },
    { date: 'Sun', sentiment: 0.59 },
  ];

  if (statsError) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 3 }}>
          {statsError}
        </Alert>
      </Box>
    );
  }

  return (
    <PageTransition>
      <Box>
        <Typography variant="h4" gutterBottom sx={{ mb: 3, fontWeight: 600 }}>
          Dashboard Overview
        </Typography>

        {/* KPI Cards */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <KPICard
            title="Total Drivers"
            value={stats?.totalDrivers || 0}
            icon={PeopleIcon}
            color={theme.palette.primary.main}
            loading={statsLoading}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KPICard
            title="Avg Sentiment"
            value={stats?.averageSentiment?.toFixed(2) || '0.00'}
            icon={TrendingUpIcon}
            color={
              stats?.averageSentiment > 0
                ? theme.palette.success.main
                : stats?.averageSentiment < -0.3
                ? theme.palette.error.main
                : theme.palette.warning.main
            }
            loading={statsLoading}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KPICard
            title="Active Alerts"
            value={alerts?.length || 0}
            icon={WarningIcon}
            color={theme.palette.warning.main}
            loading={alertsLoading}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <KPICard
            title="Recent Feedback"
            value={stats?.totalFeedbackCount || 0}
            icon={FeedbackIcon}
            color={theme.palette.info.main}
            loading={statsLoading}
          />
        </Grid>
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3}>
        {/* Sentiment Distribution Pie Chart */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
              Sentiment Distribution
            </Typography>
            {statsLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 320 }}>
                <CircularProgress />
              </Box>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <PieChart>
                  <Pie
                    data={sentimentData}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    label={(entry) => `${entry.name}: ${entry.value}`}
                  >
                    {sentimentData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>

        {/* Sentiment Trend Line Chart */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
              7-Day Sentiment Trend
            </Typography>
            <ResponsiveContainer width="100%" height={320}>
              <LineChart data={trendData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis domain={[-1, 1]} />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="sentiment"
                  stroke={theme.palette.primary.main}
                  strokeWidth={2}
                  dot={{ fill: theme.palette.primary.main, r: 4 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Alert Severity Bar Chart */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
              Alerts by Severity
            </Typography>
            {alertsLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 320 }}>
                <CircularProgress />
              </Box>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={alertSeverityData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="count" fill={theme.palette.primary.main}>
                    {alertSeverityData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Recent Feedback Section */}
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h6" gutterBottom sx={{ fontWeight: 600, mb: 2 }}>
          Recent Feedback
        </Typography>
        {feedbackLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : feedback?.length > 0 ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {feedback.slice(0, 5).map((item, index) => (
              <Card key={index} variant="outlined">
                <CardContent sx={{ py: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <Box>
                      <Typography variant="body2" color="text.secondary">
                        {item.feedbackType || 'DRIVER_BEHAVIOR'} • {item.feedbackSource || 'MOBILE_APP'}
                      </Typography>
                      <Typography variant="body1" sx={{ mt: 0.5 }}>
                        {item.feedbackText || 'No feedback text available'}
                      </Typography>
                    </Box>
                    <Typography
                      variant="caption"
                      sx={{
                        px: 1.5,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor:
                          (item.sentimentScore || 0) > 0.2
                            ? `${theme.palette.success.main}20`
                            : (item.sentimentScore || 0) < -0.2
                            ? `${theme.palette.error.main}20`
                            : `${theme.palette.grey[500]}20`,
                        color:
                          (item.sentimentScore || 0) > 0.2
                            ? theme.palette.success.dark
                            : (item.sentimentScore || 0) < -0.2
                            ? theme.palette.error.dark
                            : theme.palette.grey[700],
                        fontWeight: 600,
                      }}
                    >
                      {(item.sentimentScore || 0) > 0 ? '+' : ''}
                      {(item.sentimentScore || 0).toFixed(2)}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            ))}
          </Box>
        ) : (
          <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
            No recent feedback available
          </Typography>
        )}
      </Paper>
      </Box>
    </PageTransition>
  );
}

export default DashboardPage;
