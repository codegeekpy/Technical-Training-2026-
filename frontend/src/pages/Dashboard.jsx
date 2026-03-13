import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import StatusBadge from '../components/StatusBadge';

export default function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [recentProposals, setRecentProposals] = useState([]);
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    if (user.role === 'student') {
      api.get('/events/my').then((res) => {
        const data = res.data;
        setRecentProposals(data.slice(0, 3));
        setStats({
          total: data.length,
          approved: data.filter((p) => p.status === 'approved').length,
          pending: data.filter((p) => p.status === 'pending').length,
          rejected: data.filter((p) => p.status === 'rejected').length,
        });
      }).catch(() => {});
    } else {
      const endpoint = user.role === 'dean' ? '/events/pending' : '/events/pending';
      api.get(endpoint).then((res) => {
        setRecentProposals(res.data.slice(0, 5));
        setStats({ pending: res.data.length });
      }).catch(() => {});
    }
    api.get('/notifications/me').then((res) => {
      setNotifications(res.data.filter((n) => !n.is_read).slice(0, 4));
    }).catch(() => {});
  }, [user]);

  const roleActions = {
    student: [
      { to: '/propose', icon: '📝', label: 'Submit New Event', color: '#FF6B2B', desc: 'Create a new event proposal' },
      { to: '/my-proposals', icon: '📋', label: 'My Proposals', color: '#003366', desc: 'Track your submissions' },
      { to: '/venues', icon: '📅', label: 'Check Venues', color: '#FFC107', desc: 'View venue availability' },
    ],
    coordinator: [
      { to: '/coordinator/approvals', icon: '✅', label: 'Pending Approvals', color: '#003366', desc: 'Review proposals awaiting your decision' },
      { to: '/venues', icon: '📅', label: 'Venue Calendar', color: '#FF6B2B', desc: 'View all venue bookings' },
      { to: '/admin/events', icon: '📊', label: 'All Events', color: '#1a7a3a', desc: 'Overview of all proposals' },
    ],
    dean: [
      { to: '/dean/approvals', icon: '🏛️', label: 'Dean Approvals', color: '#003366', desc: 'Final approval queue' },
      { to: '/venues', icon: '📅', label: 'Venue Calendar', color: '#FF6B2B', desc: 'View all venue bookings' },
    ],
    admin: [
      { to: '/admin/users', icon: '👥', label: 'Manage Users', color: '#003366', desc: 'Assign roles to users' },
      { to: '/admin/venues', icon: '🏛️', label: 'Manage Venues', color: '#FF6B2B', desc: 'Add/edit venues' },
      { to: '/admin/events', icon: '📊', label: 'All Events', color: '#1a7a3a', desc: 'Full event overview' },
      { to: '/coordinator/approvals', icon: '✅', label: 'Coordinator Queue', color: '#FFC107', desc: 'Coordinate approvals' },
      { to: '/dean/approvals', icon: '🏛️', label: 'Dean Queue', color: '#8b1a1a', desc: 'Dean approval queue' },
    ],
  };

  return (
    <div className="page dashboard-page">
      <div className="page-header">
        <h1>Welcome, {user.full_name.split(' ')[0]} 👋</h1>
        <p className="text-muted">{new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
      </div>

      {stats && (
        <div className="stats-row">
          {user.role === 'student' ? (
            <>
              <div className="stat-card"><span className="stat-num">{stats.total}</span><span className="stat-label">Total Proposals</span></div>
              <div className="stat-card approved"><span className="stat-num">{stats.approved}</span><span className="stat-label">Approved</span></div>
              <div className="stat-card pending"><span className="stat-num">{stats.pending}</span><span className="stat-label">Pending</span></div>
              <div className="stat-card rejected"><span className="stat-num">{stats.rejected}</span><span className="stat-label">Rejected</span></div>
            </>
          ) : (
            <div className="stat-card pending"><span className="stat-num">{stats.pending}</span><span className="stat-label">Pending Your Review</span></div>
          )}
        </div>
      )}

      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="action-grid">
          {(roleActions[user.role] || []).map((action) => (
            <Link key={action.to} to={action.to} className="action-card" style={{ '--accent': action.color }}>
              <span className="action-icon">{action.icon}</span>
              <span className="action-label">{action.label}</span>
              <span className="action-desc">{action.desc}</span>
            </Link>
          ))}
        </div>
      </div>

      <div className="dashboard-bottom">
        {recentProposals.length > 0 && (
          <div className="recent-box">
            <h2>{user.role === 'student' ? 'Recent Proposals' : 'Pending Proposals'}</h2>
            <div className="proposal-list">
              {recentProposals.map((p) => (
                <div key={p.id} className="proposal-row">
                  <div>
                    <span className="proposal-title">{p.title}</span>
                    <span className="proposal-date">{new Date(p.start_datetime).toLocaleDateString('en-IN')}</span>
                  </div>
                  <div className="proposal-row-right">
                    <StatusBadge status={p.status} />
                    <Link to={user.role === 'student' ? `/my-proposals` : `/coordinator/proposals/${p.id}`} className="link-arrow">→</Link>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {notifications.length > 0 && (
          <div className="recent-box">
            <h2>Unread Notifications <Link to="/notifications" className="see-all">See all</Link></h2>
            {notifications.map((n) => (
              <div key={n.id} className="notif-row">
                <span className="notif-dot" />
                <span>{n.message}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
