import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import toast from 'react-hot-toast';

const ROLES = ['student', 'coordinator', 'dean', 'admin'];
const ROLE_COLORS = { student: '#6c757d', coordinator: '#0055aa', dean: '#7a1a7a', admin: '#8b1a1a' };

export default function ManageUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    api.get('/users/').then((res) => setUsers(res.data)).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const updateRole = async (userId, role) => {
    try {
      await api.patch(`/users/${userId}/role`, { role });
      setUsers((prev) => prev.map((u) => u.id === userId ? { ...u, role } : u));
      toast.success('Role updated');
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Failed to update role');
    }
  };

  const toggleActive = async (userId) => {
    try {
      const res = await api.patch(`/users/${userId}/toggle-active`);
      setUsers((prev) => prev.map((u) => u.id === userId ? { ...u, is_active: res.data.is_active } : u));
      toast.success(`User ${res.data.is_active ? 'activated' : 'deactivated'}`);
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Failed');
    }
  };

  const filtered = users.filter((u) =>
    u.full_name.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="page"><div className="loading-spinner" /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>👥 Manage Users</h1>
        <span className="badge-count">{users.length} users</span>
      </div>
      <input className="search-input" placeholder="🔍 Search by name or email..." value={search}
        onChange={(e) => setSearch(e.target.value)} />
      <div className="table-wrapper">
        <table className="data-table">
          <thead><tr><th>Name</th><th>Email</th><th>Department</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>
            {filtered.map((u) => (
              <tr key={u.id} className={!u.is_active ? 'row-inactive' : ''}>
                <td><strong>{u.full_name}</strong></td>
                <td>{u.email}</td>
                <td>{u.department || '—'}</td>
                <td>
                  <select
                    value={u.role}
                    onChange={(e) => updateRole(u.id, e.target.value)}
                    className="role-select"
                    style={{ color: ROLE_COLORS[u.role] }}
                  >
                    {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
                  </select>
                </td>
                <td><span className={`pill ${u.is_active ? 'pill-green' : 'pill-red'}`}>{u.is_active ? 'Active' : 'Inactive'}</span></td>
                <td>
                  <button className={`btn btn-outline-sm`} onClick={() => toggleActive(u.id)}>
                    {u.is_active ? 'Deactivate' : 'Activate'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
