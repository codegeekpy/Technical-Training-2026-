import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import StatusBadge from '../components/StatusBadge';

export default function VenueCalendar() {
  const [venues, setVenues] = useState([]);
  const [selectedVenue, setSelectedVenue] = useState(null);
  const [checkForm, setCheckForm] = useState({ start_datetime: '', end_datetime: '' });
  const [availability, setAvailability] = useState(null);
  const [checking, setChecking] = useState(false);
  const [allBookings, setAllBookings] = useState([]);

  useEffect(() => {
    api.get('/venues/').then((res) => {
      setVenues(res.data);
      if (res.data.length) setSelectedVenue(res.data[0]);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    if (!selectedVenue) return;
    setAvailability(null);
    api.get('/events/all', { params: { status: 'approved' } })
      .then((res) => setAllBookings(res.data.filter((e) => e.venue.id === selectedVenue.id)))
      .catch(() => setAllBookings([]));
  }, [selectedVenue]);

  const checkAvailability = async () => {
    if (!selectedVenue || !checkForm.start_datetime || !checkForm.end_datetime) return;
    setChecking(true);
    try {
      const res = await api.get(`/venues/${selectedVenue.id}/availability`, { params: checkForm });
      setAvailability(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setChecking(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>📅 Venue Availability</h1>
        <p className="text-muted">Check real-time availability of university venues</p>
      </div>
      <div className="venue-layout">
        <div className="venue-sidebar">
          <h3>Venues</h3>
          {venues.map((v) => (
            <button
              key={v.id}
              className={`venue-btn ${selectedVenue?.id === v.id ? 'active' : ''}`}
              onClick={() => setSelectedVenue(v)}
            >
              <span className="venue-icon">{v.venue_type === 'auditorium' ? '🏟️' : v.venue_type === 'lab' ? '🖥️' : v.venue_type === 'open_ground' ? '🌳' : '🏛️'}</span>
              <span>
                <strong>{v.name}</strong><br />
                <small>{v.venue_type.replace('_', ' ')} · Cap: {v.capacity}</small>
              </span>
            </button>
          ))}
        </div>
        <div className="venue-main">
          {selectedVenue && (
            <>
              <div className="venue-detail-card card">
                <h2>{selectedVenue.name}</h2>
                <div className="venue-info-row">
                  <span>🏷️ {selectedVenue.venue_type.replace('_', ' ')}</span>
                  <span>👥 Capacity: {selectedVenue.capacity}</span>
                  <span>📍 {selectedVenue.location}</span>
                </div>
                {selectedVenue.description && <p className="text-muted">{selectedVenue.description}</p>}
              </div>
              <div className="card" style={{ marginTop: '16px' }}>
                <h3>Check Slot Availability</h3>
                <div className="form-row">
                  <div className="form-group">
                    <label>From</label>
                    <input type="datetime-local" value={checkForm.start_datetime}
                      onChange={(e) => setCheckForm({ ...checkForm, start_datetime: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>To</label>
                    <input type="datetime-local" value={checkForm.end_datetime}
                      onChange={(e) => setCheckForm({ ...checkForm, end_datetime: e.target.value })} />
                  </div>
                </div>
                <button className="btn btn-primary" onClick={checkAvailability} disabled={checking}>
                  {checking ? 'Checking...' : '🔍 Check Availability'}
                </button>
                {availability && (
                  <div className={`availability-result ${availability.is_available ? 'avail' : 'not-avail'}`} style={{ marginTop: '12px' }}>
                    {availability.is_available ? (
                      <span>✅ Available for the selected slot!</span>
                    ) : (
                      <div>
                        <span>⚠️ Booked during this slot:</span>
                        <ul>{availability.conflicting_events.map((c, i) => <li key={i}>{c.event_title} ({new Date(c.start).toLocaleString('en-IN')})</li>)}</ul>
                      </div>
                    )}
                  </div>
                )}
              </div>
              <div className="card" style={{ marginTop: '16px' }}>
                <h3>Confirmed Bookings for this Venue</h3>
                {allBookings.length === 0 ? (
                  <p className="text-muted">No confirmed bookings yet.</p>
                ) : (
                  <table className="data-table">
                    <thead><tr><th>Event</th><th>Organizer</th><th>Date</th><th>Time</th><th>Status</th></tr></thead>
                    <tbody>
                      {allBookings.map((b) => (
                        <tr key={b.id}>
                          <td>{b.title}</td>
                          <td>{b.organizer.full_name}</td>
                          <td>{new Date(b.start_datetime).toLocaleDateString('en-IN')}</td>
                          <td>{new Date(b.start_datetime).toLocaleTimeString('en-IN', { timeStyle: 'short' })} – {new Date(b.end_datetime).toLocaleTimeString('en-IN', { timeStyle: 'short' })}</td>
                          <td><StatusBadge status={b.status} /></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
