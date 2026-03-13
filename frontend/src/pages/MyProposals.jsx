import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import StatusBadge from '../components/StatusBadge';

export default function MyProposals() {
  const [proposals, setProposals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    api.get('/events/my')
      .then((res) => setProposals(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const filtered = filter === 'all' ? proposals : proposals.filter((p) => p.status === filter);

  if (loading) return <div className="page"><div className="loading-spinner" /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>📋 My Event Proposals</h1>
        <Link to="/propose" className="btn btn-primary">+ New Proposal</Link>
      </div>
      <div className="filter-bar">
        {['all', 'pending', 'coordinator_approved', 'approved', 'rejected'].map((s) => (
          <button key={s} className={`filter-btn ${filter === s ? 'active' : ''}`} onClick={() => setFilter(s)}>
            {s === 'all' ? 'All' : s.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())}
          </button>
        ))}
      </div>
      {filtered.length === 0 ? (
        <div className="empty-state">
          <p>No proposals found.</p>
          <Link to="/propose" className="btn btn-primary">Submit your first proposal</Link>
        </div>
      ) : (
        <div className="proposals-grid">
          {filtered.map((p) => (
            <div key={p.id} className="proposal-card">
              <div className="proposal-card-header">
                <h3>{p.title}</h3>
                <StatusBadge status={p.status} />
              </div>
              <div className="proposal-card-meta">
                <span>📅 {new Date(p.start_datetime).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}</span>
                <span>🏛️ {p.venue.name}</span>
                <span>👥 {p.expected_participants} participants</span>
                <span>🎓 Faculty: {p.faculty_incharge}</span>
              </div>
              {p.coordinator_remarks && (
                <div className="remarks-box">
                  <strong>Coordinator Remarks:</strong> {p.coordinator_remarks}
                </div>
              )}
              {p.dean_remarks && (
                <div className="remarks-box dean">
                  <strong>Dean Remarks:</strong> {p.dean_remarks}
                </div>
              )}
              <div className="proposal-card-footer">
                <span className="text-muted">#{p.id} · Submitted {new Date(p.created_at).toLocaleDateString('en-IN')}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
