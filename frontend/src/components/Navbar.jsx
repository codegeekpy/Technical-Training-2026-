import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import api from '../api/axios';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { theme, cycleTheme, THEME_META } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const [unreadCount, setUnreadCount] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    if (!user) return;
    const fetch = () => {
      api.get('/notifications/me').then((res) => {
        setUnreadCount(res.data.filter((n) => !n.is_read).length);
      }).catch(() => {});
    };
    fetch();
    const interval = setInterval(fetch, 30000);
    return () => clearInterval(interval);
  }, [user, location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navLinks = () => {
    if (!user) return [];
    const links = [{ to: '/dashboard', label: 'Dashboard' }];
    if (user.role === 'student') {
      links.push({ to: '/propose', label: 'Propose Event' });
      links.push({ to: '/my-proposals', label: 'My Proposals' });
    }
    if (user.role === 'coordinator' || user.role === 'admin') {
      links.push({ to: '/coordinator/approvals', label: 'Pending Approvals' });
    }
    if (user.role === 'dean' || user.role === 'admin') {
      links.push({ to: '/dean/approvals', label: 'Dean Approvals' });
    }
    if (user.role === 'admin') {
      links.push({ to: '/admin/venues', label: 'Venues' });
      links.push({ to: '/admin/users', label: 'Users' });
      links.push({ to: '/admin/events', label: 'All Events' });
    }
    links.push({ to: '/venues', label: 'Venue Calendar' });
    return links;
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/dashboard">
          <span className="brand-icon">🎓</span>
          <span className="brand-text">University Event Management System</span>
          <span className="brand-sub">Events Portal</span>
        </Link>
      </div>
      <button className="hamburger" onClick={() => setMenuOpen(!menuOpen)}>☰</button>
      <div className={`navbar-links ${menuOpen ? 'open' : ''}`}>
        {user && navLinks().map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className={location.pathname === link.to ? 'active' : ''}
            onClick={() => setMenuOpen(false)}
          >
            {link.label}
          </Link>
        ))}
      </div>
      {user && (
        <div className="navbar-user">
          <button className="theme-toggle-btn" onClick={cycleTheme} title="Switch theme">
            {THEME_META[theme].icon} {THEME_META[theme].label.split(' ')[1]}
          </button>
          <Link to="/notifications" className="notif-btn" title="Notifications">
            🔔{unreadCount > 0 && <span className="notif-badge">{unreadCount}</span>}
          </Link>
          <div className="user-chip">
            <span className="user-name">{user.full_name.split(' ')[0]}</span>
            <span className={`role-pill role-${user.role}`}>{user.role}</span>
          </div>
          <button className="btn btn-outline-sm" onClick={handleLogout}>🚪 Logout</button>
        </div>
      )}
    </nav>
  );
}
