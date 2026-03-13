import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import StatusBadge from '../components/StatusBadge';

export default function AllEvents() {
  const [proposals, setProposals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    api.get('/events/all', filter !== 'all' ? { params: { status: filter } } : {})
      .then((res) => setProposals(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [filter]);

  if (loading) return <div className="page"><div className="loading-spinner" /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>📊 All Event Proposals</h1>
        <span className="badge-count">{proposals.length} events</span>
      </div>
      <div className="filter-bar">
        {['all', 'pending', 'coordinator_approved', 'approved', 'rejected'].map((s) => (
          <button key={s} className={`filter-btn ${filter === s ? 'active' : ''}`} onClick={() => setFilter(s)}>
            {s === 'all' ? 'All' : s.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())}
          </button>
        ))}
      </div>
      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>#</th><th>Event</th><th>Organizer</th><th>Venue</th><th>Date</th><th>Status</th>
            </tr>
          </thead>
          <tbody>
            {proposals.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td><strong>{p.title}</strong>{p.event_type && <span className="tag">{p.event_type}</span>}</td>
                <td>{p.organizer.full_name}<br /><small>{p.organizer.department}</small></td>
                <td>{p.venue.name}</td>
                <td>{new Date(p.start_datetime).toLocaleDateString('en-IN')}<br /><small>{new Date(p.start_datetime).toLocaleTimeString('en-IN', { timeStyle: 'short' })}</small></td>
                <td><StatusBadge status={p.status} /></td>
              </tr>
            ))}
          </tbody>
        </table>
        {proposals.length === 0 && <p className="empty-state">No events found.</p>}
      </div>
    </div>
  );
}
