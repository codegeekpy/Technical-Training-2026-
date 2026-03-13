import React from 'react';

const STATUS_CONFIG = {
  draft:                { label: 'Draft',                color: '#6c757d' },
  pending:              { label: 'Pending Review',       color: '#FF6B2B' },
  coordinator_approved: { label: 'Coordinator Approved', color: '#0055aa' },
  approved:             { label: 'Fully Approved',       color: '#1a7a3a' },
  rejected:             { label: 'Rejected',             color: '#8b1a1a' },
};

export default function StatusBadge({ status }) {
  const cfg = STATUS_CONFIG[status] || { label: status, color: '#6c757d' };
  return (
    <span className="status-badge" style={{ background: cfg.color }}>
      {cfg.label}
    </span>
  );
}
