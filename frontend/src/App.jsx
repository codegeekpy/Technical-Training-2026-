import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';

import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import ProposeEvent from './pages/ProposeEvent';
import MyProposals from './pages/MyProposals';
import PendingApprovals from './pages/PendingApprovals';
import VenueCalendar from './pages/VenueCalendar';
import Notifications from './pages/Notifications';
import AllEvents from './pages/AllEvents';
import ManageVenues from './pages/ManageVenues';
import ManageUsers from './pages/ManageUsers';

function AppRoutes() {
  const { user } = useAuth();
  return (
    <>
      <Navbar />
      <main className="main-content">
        <Routes>
          <Route path="/login" element={user ? <Navigate to="/dashboard" /> : <Login />} />
          <Route path="/register" element={user ? <Navigate to="/dashboard" /> : <Register />} />
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/propose" element={<ProtectedRoute roles={['student', 'admin']}><ProposeEvent /></ProtectedRoute>} />
          <Route path="/my-proposals" element={<ProtectedRoute roles={['student', 'admin']}><MyProposals /></ProtectedRoute>} />
          <Route path="/coordinator/approvals" element={<ProtectedRoute roles={['coordinator', 'admin']}><PendingApprovals /></ProtectedRoute>} />
          <Route path="/dean/approvals" element={<ProtectedRoute roles={['dean', 'admin']}><PendingApprovals /></ProtectedRoute>} />
          <Route path="/venues" element={<ProtectedRoute><VenueCalendar /></ProtectedRoute>} />
          <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />
          <Route path="/admin/events" element={<ProtectedRoute roles={['coordinator', 'dean', 'admin']}><AllEvents /></ProtectedRoute>} />
          <Route path="/admin/venues" element={<ProtectedRoute roles={['admin']}><ManageVenues /></ProtectedRoute>} />
          <Route path="/admin/users" element={<ProtectedRoute roles={['admin']}><ManageUsers /></ProtectedRoute>} />
          <Route path="/" element={<Navigate to={user ? '/dashboard' : '/login'} />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
    </>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <Toaster position="top-right" toastOptions={{ duration: 4000, style: { fontFamily: 'Poppins, sans-serif', fontWeight: 600 } }} />
          <AppRoutes />
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}
