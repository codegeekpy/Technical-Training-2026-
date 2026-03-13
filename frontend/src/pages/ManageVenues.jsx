import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import toast from 'react-hot-toast';

const VENUE_TYPES = ['auditorium', 'seminar_hall', 'lab', 'conference_room', 'open_ground'];

const emptyForm = { name: '', venue_type: 'seminar_hall', capacity: '', location: '', description: '', is_active: true };

export default function ManageVenues() {
  const [venues, setVenues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);

  const fetchVenues = () => {
    api.get('/venues/', { params: { active_only: false } }).then((res) => setVenues(res.data)).finally(() => setLoading(false));
  };
  useEffect(() => { fetchVenues(); }, []);

  const handleChange = (e) => {
    const val = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm({ ...form, [e.target.name]: val });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = { ...form, capacity: parseInt(form.capacity) };
    try {
      if (editing) {
        await api.patch(`/venues/${editing.id}`, payload);
        toast.success('Venue updated');
      } else {
        await api.post('/venues/', payload);
        toast.success('Venue added');
      }
      setShowForm(false);
      setEditing(null);
      setForm(emptyForm);
      fetchVenues();
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Failed');
    }
  };

  const startEdit = (venue) => {
    setEditing(venue);
    setForm({ name: venue.name, venue_type: venue.venue_type, capacity: venue.capacity, location: venue.location, description: venue.description || '', is_active: venue.is_active });
    setShowForm(true);
  };

  const toggleActive = async (venue) => {
    await api.patch(`/venues/${venue.id}`, { is_active: !venue.is_active });
    fetchVenues();
  };

  if (loading) return <div className="page"><div className="loading-spinner" /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>🏛️ Manage Venues</h1>
        <button className="btn btn-primary" onClick={() => { setEditing(null); setForm(emptyForm); setShowForm(true); }}>+ Add Venue</button>
      </div>
      {showForm && (
        <div className="card form-card" style={{ marginBottom: '24px' }}>
          <h3>{editing ? 'Edit Venue' : 'New Venue'}</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Name *</label>
                <input name="name" value={form.name} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Type *</label>
                <select name="venue_type" value={form.venue_type} onChange={handleChange}>
                  {VENUE_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
                </select>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Capacity *</label>
                <input type="number" name="capacity" value={form.capacity} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Location *</label>
                <input name="location" value={form.location} onChange={handleChange} required />
              </div>
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea name="description" value={form.description} onChange={handleChange} rows={2} />
            </div>
            {editing && (
              <div className="form-group">
                <label><input type="checkbox" name="is_active" checked={form.is_active} onChange={handleChange} /> Active</label>
              </div>
            )}
            <div className="form-actions">
              <button type="submit" className="btn btn-primary">{editing ? 'Save Changes' : 'Add Venue'}</button>
              <button type="button" className="btn btn-outline-sm" onClick={() => { setShowForm(false); setEditing(null); }}>Cancel</button>
            </div>
          </form>
        </div>
      )}
      <div className="venues-grid">
        {venues.map((v) => (
          <div key={v.id} className={`venue-card card ${!v.is_active ? 'inactive' : ''}`}>
            <div className="venue-card-header">
              <h3>{v.name}</h3>
              <span className={`pill ${v.is_active ? 'pill-green' : 'pill-red'}`}>{v.is_active ? 'Active' : 'Inactive'}</span>
            </div>
            <div className="venue-card-meta">
              <span>🏷️ {v.venue_type.replace('_', ' ')}</span>
              <span>👥 Capacity: {v.capacity}</span>
              <span>📍 {v.location}</span>
            </div>
            {v.description && <p className="text-muted" style={{ fontSize: '13px' }}>{v.description}</p>}
            <div className="venue-card-actions">
              <button className="btn btn-outline-sm" onClick={() => startEdit(v)}>Edit</button>
              <button className="btn btn-outline-sm" onClick={() => toggleActive(v)}>
                {v.is_active ? 'Deactivate' : 'Activate'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
