import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/StatusBadge';
import toast from 'react-hot-toast';

export default function PendingApprovals() {
  const { user } = useAuth();
  const [proposals, setProposals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [remarks, setRemarks] = useState('');
  const [acting, setActing] = useState(false);

  const isCoordinator = user.role === 'coordinator' || user.role === 'admin';
  const isDean = user.role === 'dean' || user.role === 'admin';

  const fetchProposals = () => {
    setLoading(true);
    api.get('/events/pending')
      .then((res) => {
        if (isCoordinator && !isDean) setProposals(res.data.filter((p) => p.status === 'pending'));
        else if (isDean && !isCoordinator) setProposals(res.data.filter((p) => p.status === 'coordinator_approved'));
        else setProposals(res.data);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchProposals(); }, []);

  const handleAction = async (proposalId, action, isCoordAction) => {
    setActing(true);
    try {
      const endpoint = isCoordAction
        ? `/events/${proposalId}/${action === 'approve' ? 'coordinator-approve' : 'coordinator-reject'}`
        : `/events/${proposalId}/${action === 'approve' ? 'dean-approve' : 'dean-reject'}`;
      await api.patch(endpoint, { remarks });
      toast.success(`Proposal ${action === 'approve' ? 'approved' : 'rejected'} successfully`);
      setSelected(null);
      setRemarks('');
      fetchProposals();
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Action failed');
    } finally {
      setActing(false);
    }
  };

  if (loading) return <div className="page"><div className="loading-spinner" /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>✅ Pending Approvals</h1>
        <span className="badge-count">{proposals.length} awaiting review</span>
      </div>
      {proposals.length === 0 ? (
        <div className="empty-state"><p>🎉 No pending proposals. All caught up!</p></div>
      ) : (
        <div className="proposals-grid">
          {proposals.map((p) => (
            <div key={p.id} className={`proposal-card ${selected?.id === p.id ? 'expanded' : ''}`}>
              <div className="proposal-card-header">
                <h3>{p.title}</h3>
                <StatusBadge status={p.status} />
              </div>
              <div className="proposal-card-meta">
                <span>👤 {p.organizer.full_name} ({p.organizer.department})</span>
                <span>📅 {new Date(p.start_datetime).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}</span>
                <span>⏱️ to {new Date(p.end_datetime).toLocaleTimeString('en-IN', { timeStyle: 'short' })}</span>
                <span>🏛️ {p.venue.name} (cap {p.venue.capacity})</span>
                <span>👥 {p.expected_participants} participants</span>
                <span>🎓 Faculty: {p.faculty_incharge}</span>
                {p.event_type && <span>🏷️ {p.event_type}</span>}
                {p.description && <span className="proposal-desc">📄 {p.description}</span>}
              </div>
              <div className="proposal-card-footer">
                <button className="btn btn-outline-sm" onClick={() => setSelected(selected?.id === p.id ? null : p)}>
                  {selected?.id === p.id ? 'Close' : 'Review'}
                </button>
              </div>
              {selected?.id === p.id && (
                <div className="review-panel">
                  <div className="form-group">
                    <label>Remarks (optional)</label>
                    <textarea value={remarks} onChange={(e) => setRemarks(e.target.value)} rows={2} placeholder="Add remarks..." />
                  </div>
                  <div className="review-actions">
                    {p.status === 'pending' && isCoordinator && (
                      <>
                        <button className="btn btn-approve" onClick={() => handleAction(p.id, 'approve', true)} disabled={acting}>
                          ✅ Coordinator Approve
                        </button>
                        <button className="btn btn-reject" onClick={() => handleAction(p.id, 'reject', true)} disabled={acting}>
                          ❌ Reject
                        </button>
                      </>
                    )}
                    {p.status === 'coordinator_approved' && isDean && (
                      <>
                        <button className="btn btn-approve" onClick={() => handleAction(p.id, 'approve', false)} disabled={acting}>
                          🏛️ Dean Final Approve
                        </button>
                        <button className="btn btn-reject" onClick={() => handleAction(p.id, 'reject', false)} disabled={acting}>
                          ❌ Reject
                        </button>
                      </>
                    )}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
