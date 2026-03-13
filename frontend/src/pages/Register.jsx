import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import toast from 'react-hot-toast';

const DEPARTMENTS = [
  'Computer Science', 'Information Technology', 'Electronics & Communication',
  'Mechanical Engineering', 'Civil Engineering', 'Business Administration',
  'Pharmacy', 'MBA', 'MCA', 'Administration', "Dean's Office", 'Event Management',
];

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    full_name: '', email: '', password: '', department: '', role: 'student',
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.email.endsWith('@anurag.edu.in')) {
      toast.error('Only @anurag.edu.in email addresses are allowed');
      return;
    }
    setLoading(true);
    try {
      await api.post('/auth/register', form);
      toast.success('Account created! Please login.');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: '480px' }}>
        <div className="auth-header">
          <div className="auth-logo">🎓</div>
          <h1>Create Account</h1>
          <p>Anurag University Event Portal</p>
        </div>
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Full Name</label>
            <input name="full_name" value={form.full_name} onChange={handleChange}
              placeholder="Your full name" required />
          </div>
          <div className="form-group">
            <label>University Email</label>
            <input type="email" name="email" value={form.email} onChange={handleChange}
              placeholder="yourname@anurag.edu.in" required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" value={form.password} onChange={handleChange}
              placeholder="Min. 6 characters" required />
          </div>
          <div className="form-group">
            <label>Department</label>
            <select name="department" value={form.department} onChange={handleChange}>
              <option value="">Select Department</option>
              {DEPARTMENTS.map((d) => <option key={d} value={d}>{d}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Register as</label>
            <select name="role" value={form.role} onChange={handleChange}>
              <option value="student">Student / Organizer</option>
              <option value="coordinator">Event Coordinator</option>
              <option value="dean">Dean</option>
            </select>
          </div>
          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'Creating account...' : 'Register'}
          </button>
        </form>
        <div className="auth-footer">
          <p>Already registered? <Link to="/login">Sign in here</Link></p>
        </div>
      </div>
    </div>
  );
}
