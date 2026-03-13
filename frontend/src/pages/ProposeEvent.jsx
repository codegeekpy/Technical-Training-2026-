import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import toast from 'react-hot-toast';

export default function ProposeEvent() {
  const navigate = useNavigate();
  const [venues, setVenues] = useState([]);
  const [availability, setAvailability] = useState(null);
  const [checkingAvail, setCheckingAvail] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    title: '', description: '', venue_id: '', faculty_incharge: '',
    expected_participants: '', start_datetime: '', end_datetime: '', event_type: '',
  });

  useEffect(() => {
    api.get('/venues/').then((res) => setVenues(res.data)).catch(() => {});
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setAvailability(null);
  };

  const checkAvailability = async () => {
    if (!form.venue_id || !form.start_datetime || !form.end_datetime) {
      toast.error('Select venue, start and end time first');
      return;
    }
    setCheckingAvail(true);
    try {
      const res = await api.get(`/venues/${form.venue_id}/availability`, {
        params: { start_datetime: form.start_datetime, end_datetime: form.end_datetime },
      });
      setAvailability(res.data);
      if (res.data.is_available) toast.success('Venue is available for the selected slot!');
      else toast.error('Venue is already booked for this slot.');
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Failed to check availability');
    } finally {
      setCheckingAvail(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.venue_id) { toast.error('Please select a venue'); return; }
    if (availability === null) {
      toast.error('Please check venue availability before submitting');
      return;
    }
    if (!availability.is_available) {
      toast.error('Cannot submit — venue is not available for the selected slot');
      return;
    }
    setLoading(true);
    try {
      const payload = {
        ...form,
        venue_id: parseInt(form.venue_id),
        expected_participants: parseInt(form.expected_participants),
      };
      await api.post('/events/propose', payload);
      toast.success('Event proposal submitted! Awaiting coordinator review.');
      navigate('/my-proposals');
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Submission failed');
    } finally {
      setLoading(false);
    }
  };

  const EVENT_TYPES = ['Technical', 'Cultural', 'Sports', 'Academic', 'Workshop', 'Seminar', 'Guest Lecture', 'Hackathon', 'Other'];

  return (
    <div className="page">
      <div className="page-header">
        <h1>📝 Submit Event Proposal</h1>
        <p className="text-muted">Fill in the details below. Your proposal will be reviewed by the coordinator and dean.</p>
      </div>
      <div className="card form-card">
        <form onSubmit={handleSubmit}>
          <div className="form-section">
            <h3 className="section-title">Event Details</h3>
            <div className="form-group">
              <label>Event Title *</label>
              <input name="title" value={form.title} onChange={handleChange} placeholder="e.g. Anurag TechFest 2025" required />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Event Type</label>
                <select name="event_type" value={form.event_type} onChange={handleChange}>
                  <option value="">Select type</option>
                  {EVENT_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Expected Participants *</label>
                <input type="number" name="expected_participants" value={form.expected_participants}
                  onChange={handleChange} min="1" placeholder="e.g. 150" required />
              </div>
            </div>
            <div className="form-group">
              <label>Faculty In-Charge *</label>
              <input name="faculty_incharge" value={form.faculty_incharge} onChange={handleChange}
                placeholder="Name of faculty supervising the event" required />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea name="description" value={form.description} onChange={handleChange}
                rows={3} placeholder="Brief description of the event..." />
            </div>
          </div>

          <div className="form-section">
            <h3 className="section-title">Venue & Schedule</h3>
            <div className="form-group">
              <label>Venue *</label>
              <select name="venue_id" value={form.venue_id} onChange={handleChange} required>
                <option value="">-- Select a Venue --</option>
                {venues.map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.name} ({v.venue_type.replace('_', ' ')}) — Capacity: {v.capacity}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Start Date & Time *</label>
                <input type="datetime-local" name="start_datetime" value={form.start_datetime}
                  onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>End Date & Time *</label>
                <input type="datetime-local" name="end_datetime" value={form.end_datetime}
                  onChange={handleChange} required />
              </div>
            </div>
            <button type="button" className="btn btn-secondary" onClick={checkAvailability} disabled={checkingAvail}>
              {checkingAvail ? 'Checking...' : '🔍 Check Venue Availability'}
            </button>

            {availability !== null && (
              <div className={`availability-result ${availability.is_available ? 'avail' : 'not-avail'}`}>
                {availability.is_available ? (
                  <span>✅ <strong>{availability.venue_name}</strong> is available for the selected time slot.</span>
                ) : (
                  <div>
                    <span>⚠️ <strong>{availability.venue_name}</strong> is already booked.</span>
                    <ul>
                      {availability.conflicting_events.map((c, i) => (
                        <li key={i}>{c.event_title} by {c.organizer} ({new Date(c.start).toLocaleString('en-IN')} – {new Date(c.end).toLocaleString('en-IN')})</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={loading || (availability !== null && !availability.is_available)}>
              {loading ? 'Submitting...' : '📨 Submit Proposal'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
