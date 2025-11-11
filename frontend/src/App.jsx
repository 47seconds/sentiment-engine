import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Toaster } from 'react-hot-toast';
import { theme, darkTheme } from './styles/theme';

// Auth
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';

// Layout components
import MainLayout from './components/layout/MainLayout';
import ErrorBoundary from './components/ErrorBoundary';

// Pages
import { LoginPage } from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import FeedbackPage from './pages/FeedbackPage';
import DriversPage from './pages/DriversPage';
import AlertsPage from './pages/AlertsPage';
import AdminPage from './pages/AdminPage';
import ProfilePage from './pages/ProfilePage';
import SettingsPage from './pages/SettingsPage';
import MyFeedbackPage from './pages/MyFeedbackPage';

function App() {
  const [darkMode, setDarkMode] = useState(false);

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  return (
    <ThemeProvider theme={darkMode ? darkTheme : theme}>
      <CssBaseline />
      <ErrorBoundary>
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: darkMode ? '#1f2937' : '#fff',
              color: darkMode ? '#f9fafb' : '#1f2937',
            },
            success: {
              iconTheme: {
                primary: '#10b981',
                secondary: '#fff',
              },
            },
            error: {
              iconTheme: {
                primary: '#ef4444',
                secondary: '#fff',
              },
            },
          }}
        />
        <Router>
          <AuthProvider>
            <Routes>
              {/* Public routes */}
              <Route path="/login" element={<LoginPage />} />
              
              {/* Protected routes - Admin only */}
              <Route path="/" element={
                <ProtectedRoute adminOnly>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <Navigate to="/dashboard" replace />
                  </MainLayout>
                </ProtectedRoute>
              } />
              <Route path="/dashboard" element={
                <ProtectedRoute adminOnly>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <DashboardPage />
                  </MainLayout>
                </ProtectedRoute>
              } />
              <Route path="/drivers" element={
                <ProtectedRoute adminOnly>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <DriversPage />
                  </MainLayout>
                </ProtectedRoute>
              } />
              <Route path="/alerts" element={
                <ProtectedRoute adminOnly>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <AlertsPage />
                  </MainLayout>
                </ProtectedRoute>
              } />
              <Route path="/admin" element={
                <ProtectedRoute adminOnly>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <AdminPage />
                  </MainLayout>
                </ProtectedRoute>
              } />
              
              {/* Protected routes - All authenticated users */}
              <Route path="/feedback" element={
                <ProtectedRoute>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <FeedbackPage />
                  </MainLayout>
                </ProtectedRoute>
              } />
              <Route path="/my-feedback" element={
                <ProtectedRoute>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <MyFeedbackPage />
                  </MainLayout>
                </ProtectedRoute>
              } />
              <Route path="/profile" element={
                <ProtectedRoute>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <ProfilePage />
                  </MainLayout>
                </ProtectedRoute>
              } />
              <Route path="/settings" element={
                <ProtectedRoute>
                  <MainLayout darkMode={darkMode} toggleDarkMode={toggleDarkMode}>
                    <SettingsPage />
                  </MainLayout>
                </ProtectedRoute>
              } />
            </Routes>
          </AuthProvider>
        </Router>
      </ErrorBoundary>
    </ThemeProvider>
  );
}

export default App;
