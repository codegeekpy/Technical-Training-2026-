import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import toast from 'react-hot-toast';

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/notifications/me')
      .then((res) => setNotifications(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const markRead = async (id) => {
    await api.patch(`/notifications/${id}/read`).catch(() => {});
    setNotifications((prev) => prev.map((n) => n.id === id ? { ...n, is_read: true } : n));
  };

  const markAllRead = async () => {
    await api.patch('/notifications/mark-all-read').catch(() => {});
    setNotifications((prev) => prev.map((n) => ({ ...n, is_read: true })));
    toast.success('All notifications marked as read');
  };

  const TYPE_ICONS = {
    proposal_submitted: '📝',
    proposal_approved: '✅',
    proposal_rejected: '❌',
    coordinator_review: '📋',
    dean_review: '🏛️',
    general: '🔔',
  };

  if (loading) return <div className="page"><div className="loading-spinner" /></div>;

  const unread = notifications.filter((n) => !n.is_read).length;

  return (
    <div className="page">
      <div className="page-header">
        <h1>🔔 Notifications</h1>
        {unread > 0 && (
          <button className="btn btn-outline-sm" onClick={markAllRead}>Mark all as read ({unread})</button>
        )}
      </div>
      {notifications.length === 0 ? (
        <div className="empty-state"><p>No notifications yet.</p></div>
      ) : (
        <div className="notif-list">
          {notifications.map((n) => (
            <div
              key={n.id}
              className={`notif-item ${!n.is_read ? 'unread' : ''}`}
              onClick={() => !n.is_read && markRead(n.id)}
            >
              <span className="notif-type-icon">{TYPE_ICONS[n.notification_type] || '🔔'}</span>
              <div className="notif-content">
                <p>{n.message}</p>
                <span className="notif-time">{new Date(n.created_at).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}</span>
              </div>
              {!n.is_read && <span className="unread-dot" />}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
