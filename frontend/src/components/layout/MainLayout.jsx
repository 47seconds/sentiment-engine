import { useState } from 'react';
import { Box, useMediaQuery, useTheme } from '@mui/material';
import Header from './Header';
import Sidebar from './Sidebar';

const DRAWER_WIDTH = 260;

function MainLayout({ children, darkMode, toggleDarkMode }) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Header
        drawerWidth={DRAWER_WIDTH}
        handleDrawerToggle={handleDrawerToggle}
        darkMode={darkMode}
        toggleDarkMode={toggleDarkMode}
      />
      <Sidebar
        drawerWidth={DRAWER_WIDTH}
        mobileOpen={mobileOpen}
        handleDrawerToggle={handleDrawerToggle}
        isMobile={isMobile}
      />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: { xs: 2, sm: 2, md: 3 },
          width: '100%',
          ml: { sm: `${DRAWER_WIDTH}px` },
          mt: '64px', // Height of AppBar
          backgroundColor: 'background.default',
          minHeight: 'calc(100vh - 64px)',
          maxWidth: '100%',
        }}
      >
        {children}
      </Box>
    </Box>
  );
}

export default MainLayout;
