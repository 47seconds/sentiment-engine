import { Box, Typography, Button, Paper } from '@mui/material';
import {
  Inbox as InboxIcon,
  SearchOff as SearchOffIcon,
  ErrorOutline as ErrorIcon,
  Add as AddIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';

export const EmptyState = ({ 
  icon: Icon = InboxIcon,
  title = 'No Data Available',
  description = 'There is no data to display at the moment.',
  action,
  actionLabel,
  secondaryAction,
  secondaryActionLabel,
}) => (
  <Paper
    elevation={0}
    sx={{
      p: 6,
      textAlign: 'center',
      bgcolor: 'background.default',
      border: '2px dashed',
      borderColor: 'divider',
      borderRadius: 3,
    }}
  >
    <Icon
      sx={{
        fontSize: 80,
        color: 'text.disabled',
        mb: 2,
      }}
    />
    <Typography variant="h6" gutterBottom color="text.secondary" fontWeight={600}>
      {title}
    </Typography>
    <Typography variant="body2" color="text.secondary" paragraph sx={{ maxWidth: 400, mx: 'auto' }}>
      {description}
    </Typography>
    
    {(action || secondaryAction) && (
      <Box sx={{ mt: 3, display: 'flex', gap: 2, justifyContent: 'center' }}>
        {action && (
          <Button
            variant="contained"
            onClick={action}
            startIcon={<AddIcon />}
          >
            {actionLabel || 'Add New'}
          </Button>
        )}
        {secondaryAction && (
          <Button
            variant="outlined"
            onClick={secondaryAction}
            startIcon={<RefreshIcon />}
          >
            {secondaryActionLabel || 'Refresh'}
          </Button>
        )}
      </Box>
    )}
  </Paper>
);

export const NoSearchResults = ({ searchTerm, onClear }) => (
  <EmptyState
    icon={SearchOffIcon}
    title="No Results Found"
    description={`No items match your search "${searchTerm}". Try adjusting your search criteria or clear the filter to see all items.`}
    action={onClear}
    actionLabel="Clear Search"
  />
);

export const ErrorState = ({ title, description, onRetry }) => (
  <EmptyState
    icon={ErrorIcon}
    title={title || 'Something Went Wrong'}
    description={description || 'An error occurred while loading data. Please try again.'}
    action={onRetry}
    actionLabel="Try Again"
  />
);

export const NoDrivers = ({ onCreate }) => (
  <EmptyState
    icon={InboxIcon}
    title="No Drivers Yet"
    description="Start tracking driver sentiment by adding your first driver to the system."
    action={onCreate}
    actionLabel="Add Driver"
  />
);

export const NoAlerts = () => (
  <EmptyState
    icon={InboxIcon}
    title="All Clear! No Active Alerts"
    description="Great news! There are no active alerts at the moment. Your drivers are doing well."
  />
);

export const NoFeedback = ({ onCreate }) => (
  <EmptyState
    icon={InboxIcon}
    title="No Feedback Submitted"
    description="Be the first to provide feedback about driver performance, trips, or app experience."
    action={onCreate}
    actionLabel="Submit Feedback"
  />
);
