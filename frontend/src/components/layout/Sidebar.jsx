import {
  Drawer,
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  Typography,
  useTheme,
  Avatar,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Feedback as FeedbackIcon,
  DirectionsCar as DriversIcon,
  NotificationsActive as AlertsIcon,
  Settings as SettingsIcon,
  AccountCircle as ProfileIcon,
  AdminPanelSettings as AdminIcon,
  Logout as LogoutIcon,
  History as HistoryIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

// Admin menu items
const adminMenuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard', roles: ['ADMIN', 'MANAGER'] },
  { text: 'Drivers', icon: <DriversIcon />, path: '/drivers', roles: ['ADMIN', 'MANAGER'] },
  { text: 'Alerts', icon: <AlertsIcon />, path: '/alerts', badge: 3, roles: ['ADMIN', 'MANAGER'] },
  { text: 'Feedback', icon: <FeedbackIcon />, path: '/feedback', roles: ['ADMIN', 'MANAGER', 'DRIVER', 'SUPPORT'] },
  { text: 'My Feedback', icon: <HistoryIcon />, path: '/my-feedback', roles: ['ADMIN', 'MANAGER', 'DRIVER', 'SUPPORT'] },
];

// Employee menu items
const employeeMenuItems = [
  { text: 'Submit Feedback', icon: <FeedbackIcon />, path: '/feedback' },
  { text: 'My Feedback', icon: <HistoryIcon />, path: '/my-feedback' },
];

const accountItems = [
  { text: 'My Profile', icon: <ProfileIcon />, path: '/profile' },
  { text: 'Settings', icon: <SettingsIcon />, path: '/settings' },
];

const adminItems = [
  { text: 'Admin Panel', icon: <AdminIcon />, path: '/admin' },
];

function Sidebar({ drawerWidth, mobileOpen, handleDrawerToggle, isMobile }) {
  const theme = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isAdmin } = useAuth();

  // Determine which menu items to show based on role
  const menuItems = isAdmin() ? adminMenuItems : employeeMenuItems;

  const handleNavigation = (path) => {
    navigate(path);
    if (isMobile) {
      handleDrawerToggle();
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Logo/Brand */}
      <Box
        sx={{
          p: 2.5,
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          borderBottom: `1px solid ${theme.palette.divider}`,
        }}
      >
        <Box
          sx={{
            width: 40,
            height: 40,
            borderRadius: '10px',
            background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Typography variant="h6" sx={{ color: 'white', fontWeight: 700 }}>
            M
          </Typography>
        </Box>
        <Box>
          <Typography variant="subtitle1" fontWeight={700} sx={{ lineHeight: 1.2 }}>
            MoveInSync
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Sentiment Engine
          </Typography>
        </Box>
      </Box>

      {/* Main Navigation */}
      <List sx={{ px: 1.5, pt: 2, flexGrow: 1 }}>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding sx={{ mb: 0.5 }}>
            <ListItemButton
              onClick={() => handleNavigation(item.path)}
              sx={{
                borderRadius: 2,
                minHeight: 48,
                backgroundColor: isActive(item.path)
                  ? theme.palette.mode === 'dark'
                    ? 'rgba(59, 130, 246, 0.15)'
                    : 'rgba(30, 58, 138, 0.08)'
                  : 'transparent',
                color: isActive(item.path)
                  ? theme.palette.primary.main
                  : theme.palette.text.primary,
                '&:hover': {
                  backgroundColor: theme.palette.mode === 'dark'
                    ? 'rgba(255, 255, 255, 0.05)'
                    : 'rgba(0, 0, 0, 0.04)',
                },
                ...(isActive(item.path) && {
                  fontWeight: 600,
                  '& .MuiListItemIcon-root': {
                    color: theme.palette.primary.main,
                  },
                }),
              }}
            >
              <ListItemIcon
                sx={{
                  minWidth: 40,
                  color: 'inherit',
                }}
              >
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.text}
                primaryTypographyProps={{
                  fontSize: '0.875rem',
                  fontWeight: isActive(item.path) ? 600 : 500,
                }}
              />
              {item.badge && (
                <Box
                  sx={{
                    bgcolor: 'error.main',
                    color: 'white',
                    borderRadius: '10px',
                    px: 1,
                    py: 0.25,
                    fontSize: '0.75rem',
                    fontWeight: 600,
                    minWidth: 20,
                    textAlign: 'center',
                  }}
                >
                  {item.badge}
                </Box>
              )}
            </ListItemButton>
          </ListItem>
        ))}
      </List>

      {/* Admin Section */}
      <Box sx={{ px: 1.5, pb: 2 }}>
        <Divider sx={{ mb: 1 }} />
        <Typography
          variant="caption"
          sx={{
            px: 2,
            py: 1,
            display: 'block',
            color: 'text.secondary',
            fontWeight: 600,
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
          }}
        >
          Account
        </Typography>
        <List>
          {accountItems.map((item) => (
            <ListItem key={item.text} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                onClick={() => handleNavigation(item.path)}
                sx={{
                  borderRadius: 2,
                  minHeight: 48,
                  backgroundColor: isActive(item.path)
                    ? theme.palette.mode === 'dark'
                      ? 'rgba(59, 130, 246, 0.15)'
                      : 'rgba(30, 58, 138, 0.08)'
                    : 'transparent',
                  color: isActive(item.path)
                    ? theme.palette.primary.main
                    : theme.palette.text.primary,
                  '&:hover': {
                    backgroundColor: theme.palette.mode === 'dark'
                      ? 'rgba(255, 255, 255, 0.05)'
                      : 'rgba(0, 0, 0, 0.04)',
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: 40,
                    color: 'inherit',
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{
                    fontSize: '0.875rem',
                    fontWeight: isActive(item.path) ? 600 : 500,
                  }}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>

        {/* Admin Section - Only show for admin users */}
        {isAdmin() && (
          <>
            <Divider sx={{ my: 1 }} />
            <Typography
              variant="caption"
              sx={{
                px: 2,
                py: 1,
                display: 'block',
                color: 'text.secondary',
                fontWeight: 600,
                textTransform: 'uppercase',
                letterSpacing: '0.5px',
              }}
            >
              Administration
            </Typography>
            <List>
              {adminItems.map((item) => (
                <ListItem key={item.text} disablePadding>
                  <ListItemButton
                    onClick={() => handleNavigation(item.path)}
                    sx={{
                      borderRadius: 2,
                      minHeight: 48,
                      backgroundColor: isActive(item.path)
                        ? theme.palette.mode === 'dark'
                          ? 'rgba(59, 130, 246, 0.15)'
                          : 'rgba(30, 58, 138, 0.08)'
                        : 'transparent',
                      color: isActive(item.path)
                        ? theme.palette.primary.main
                        : theme.palette.text.primary,
                      '&:hover': {
                        backgroundColor: theme.palette.mode === 'dark'
                          ? 'rgba(255, 255, 255, 0.05)'
                          : 'rgba(0, 0, 0, 0.04)',
                      },
                    }}
                  >
                    <ListItemIcon
                      sx={{
                        minWidth: 40,
                        color: 'inherit',
                      }}
                    >
                      {item.icon}
                    </ListItemIcon>
                    <ListItemText
                      primary={item.text}
                      primaryTypographyProps={{
                        fontSize: '0.875rem',
                        fontWeight: isActive(item.path) ? 600 : 500,
                      }}
                    />
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          </>
        )}
      </Box>

      {/* User Profile & Logout at Bottom */}
      <Box sx={{ p: 2, borderTop: `1px solid ${theme.palette.divider}` }}>
        {/* User Info */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1, px: 1 }}>
          <Avatar 
            sx={{ 
              width: 36, 
              height: 36,
              bgcolor: theme.palette.primary.main,
              fontSize: '0.875rem'
            }}
          >
            {user?.name?.charAt(0)?.toUpperCase() || 'U'}
          </Avatar>
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Typography variant="body2" fontWeight={600} noWrap>
              {user?.name || 'User'}
            </Typography>
            <Typography variant="caption" color="text.secondary" noWrap>
              {user?.role || 'DRIVER'}
            </Typography>
          </Box>
        </Box>
        
        {/* Logout Button */}
        <ListItem disablePadding>
          <ListItemButton
            onClick={handleLogout}
            sx={{
              borderRadius: 2,
              minHeight: 44,
              color: theme.palette.error.main,
              '&:hover': {
                backgroundColor: theme.palette.mode === 'dark'
                  ? 'rgba(239, 68, 68, 0.1)'
                  : 'rgba(239, 68, 68, 0.08)',
              },
            }}
          >
            <ListItemIcon sx={{ minWidth: 40, color: 'inherit' }}>
              <LogoutIcon />
            </ListItemIcon>
            <ListItemText
              primary="Logout"
              primaryTypographyProps={{
                fontSize: '0.875rem',
                fontWeight: 600,
              }}
            />
          </ListItemButton>
        </ListItem>
      </Box>
    </Box>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
    >
      {/* Mobile drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true, // Better mobile performance
        }}
        sx={{
          display: { xs: 'block', sm: 'none' },
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: drawerWidth,
            borderRight: 'none',
          },
        }}
      >
        {drawer}
      </Drawer>

      {/* Desktop drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', sm: 'block' },
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: drawerWidth,
          },
        }}
        open
      >
        {drawer}
      </Drawer>
    </Box>
  );
}

export default Sidebar;
