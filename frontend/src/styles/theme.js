import { createTheme } from '@mui/material/styles';

// Color Palette
// Primary: Deep Blue (trust, professionalism)
// Success: Green (positive sentiment, good performance)
// Warning: Amber (caution, needs attention)
// Error: Red (critical alerts, negative sentiment)

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1e3a8a', // Deep Blue
      light: '#3b82f6',
      dark: '#1e40af',
      contrastText: '#fff',
    },
    secondary: {
      main: '#7c3aed', // Purple accent
      light: '#a78bfa',
      dark: '#6d28d9',
      contrastText: '#fff',
    },
    success: {
      main: '#10b981', // Green for positive
      light: '#34d399',
      dark: '#059669',
      contrastText: '#fff',
    },
    warning: {
      main: '#f59e0b', // Amber for warning
      light: '#fbbf24',
      dark: '#d97706',
      contrastText: '#000',
    },
    error: {
      main: '#ef4444', // Red for critical
      light: '#f87171',
      dark: '#dc2626',
      contrastText: '#fff',
    },
    info: {
      main: '#3b82f6',
      light: '#60a5fa',
      dark: '#2563eb',
      contrastText: '#fff',
    },
    background: {
      default: '#f5f7fa',
      paper: '#ffffff',
    },
    text: {
      primary: '#1f2937',
      secondary: '#6b7280',
    },
    divider: '#e5e7eb',
  },
  typography: {
    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 700,
      lineHeight: 1.2,
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 700,
      lineHeight: 1.3,
    },
    h3: {
      fontSize: '1.75rem',
      fontWeight: 600,
      lineHeight: 1.3,
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h6: {
      fontSize: '1rem',
      fontWeight: 600,
      lineHeight: 1.5,
    },
    body1: {
      fontSize: '1rem',
      lineHeight: 1.6,
    },
    body2: {
      fontSize: '0.875rem',
      lineHeight: 1.6,
    },
    button: {
      textTransform: 'none', // Don't uppercase buttons
      fontWeight: 500,
    },
  },
  shape: {
    borderRadius: 8, // Rounded corners
  },
  shadows: [
    'none',
    '0px 1px 2px rgba(0, 0, 0, 0.05)',
    '0px 1px 3px rgba(0, 0, 0, 0.1), 0px 1px 2px rgba(0, 0, 0, 0.06)',
    '0px 4px 6px -1px rgba(0, 0, 0, 0.1), 0px 2px 4px -1px rgba(0, 0, 0, 0.06)',
    '0px 10px 15px -3px rgba(0, 0, 0, 0.1), 0px 4px 6px -2px rgba(0, 0, 0, 0.05)',
    '0px 20px 25px -5px rgba(0, 0, 0, 0.1), 0px 10px 10px -5px rgba(0, 0, 0, 0.04)',
    '0px 25px 50px -12px rgba(0, 0, 0, 0.25)',
    '0px 2px 4px rgba(0,0,0,0.08)',
    '0px 3px 5px rgba(0,0,0,0.1)',
    '0px 3px 6px rgba(0,0,0,0.12)',
    '0px 4px 8px rgba(0,0,0,0.14)',
    '0px 5px 10px rgba(0,0,0,0.16)',
    '0px 6px 12px rgba(0,0,0,0.18)',
    '0px 7px 14px rgba(0,0,0,0.2)',
    '0px 8px 16px rgba(0,0,0,0.22)',
    '0px 9px 18px rgba(0,0,0,0.24)',
    '0px 10px 20px rgba(0,0,0,0.26)',
    '0px 11px 22px rgba(0,0,0,0.28)',
    '0px 12px 24px rgba(0,0,0,0.3)',
    '0px 13px 26px rgba(0,0,0,0.32)',
    '0px 14px 28px rgba(0,0,0,0.34)',
    '0px 15px 30px rgba(0,0,0,0.36)',
    '0px 16px 32px rgba(0,0,0,0.38)',
    '0px 17px 34px rgba(0,0,0,0.4)',
    '0px 18px 36px rgba(0,0,0,0.42)',
  ],
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: '8px 16px',
          fontSize: '0.875rem',
          fontWeight: 500,
          boxShadow: 'none',
          minHeight: 40, // Touch-friendly minimum height
          '@media (max-width:600px)': {
            minHeight: 44, // Larger touch targets on mobile
            padding: '10px 20px',
          },
          '&:hover': {
            boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)',
          },
        },
        contained: {
          '&:hover': {
            boxShadow: '0px 4px 8px rgba(0, 0, 0, 0.15)',
          },
        },
        sizeLarge: {
          minHeight: 48,
          padding: '12px 24px',
          fontSize: '1rem',
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          '@media (max-width:600px)': {
            padding: 12, // Larger touch area on mobile
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0px 1px 3px rgba(0, 0, 0, 0.1), 0px 1px 2px rgba(0, 0, 0, 0.06)',
          transition: 'box-shadow 0.2s ease-in-out, transform 0.2s ease-in-out',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: '0px 1px 3px rgba(0, 0, 0, 0.1)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: '1px solid #e5e7eb',
          boxShadow: 'none',
        },
      },
    },
  },
});

// Dark theme variant
export const darkTheme = createTheme({
  ...theme,
  palette: {
    mode: 'dark',
    primary: {
      main: '#3b82f6',
      light: '#60a5fa',
      dark: '#2563eb',
      contrastText: '#fff',
    },
    secondary: {
      main: '#a78bfa',
      light: '#c4b5fd',
      dark: '#8b5cf6',
      contrastText: '#fff',
    },
    success: {
      main: '#10b981',
      light: '#34d399',
      dark: '#059669',
      contrastText: '#fff',
    },
    warning: {
      main: '#f59e0b',
      light: '#fbbf24',
      dark: '#d97706',
      contrastText: '#000',
    },
    error: {
      main: '#ef4444',
      light: '#f87171',
      dark: '#dc2626',
      contrastText: '#fff',
    },
    background: {
      default: '#111827',
      paper: '#1f2937',
    },
    text: {
      primary: '#f9fafb',
      secondary: '#d1d5db',
    },
    divider: '#374151',
  },
});

// Sentiment color helper
export const getSentimentColor = (score) => {
  if (score >= 0.6) return theme.palette.success.main; // Very positive
  if (score >= 0.2) return theme.palette.success.light; // Positive
  if (score >= -0.2) return theme.palette.grey[500]; // Neutral
  if (score >= -0.6) return theme.palette.warning.main; // Negative
  return theme.palette.error.main; // Very negative
};

// Alert severity color helper
export const getAlertSeverityColor = (severity) => {
  switch (severity?.toUpperCase()) {
    case 'CRITICAL':
      return theme.palette.error.main;
    case 'HIGH':
      return theme.palette.warning.main;
    case 'MEDIUM':
      return theme.palette.info.main;
    case 'LOW':
      return theme.palette.success.main;
    default:
      return theme.palette.grey[500];
  }
};

// Driver status color helper (for EMA-based coloring)
export const getDriverStatusColor = (emaScore) => {
  if (emaScore < -0.6) return theme.palette.error.main; // Red (critical)
  if (emaScore < -0.3) return theme.palette.warning.main; // Amber (warning)
  if (emaScore < 0.2) return theme.palette.grey[500]; // Gray (neutral)
  return theme.palette.success.main; // Green (good)
};

export default theme;
