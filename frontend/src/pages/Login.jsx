import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import toast from 'react-hot-toast';

export default function Login() {
  const { login } = useAuth();
  const { theme, cycleTheme, THEME_META } = useTheme();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
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
      const user = await login(form.email, form.password);
      toast.success(`Welcome back, ${user.full_name.split(' ')[0]}!`);
      navigate('/dashboard');
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <button className="theme-toggle-btn" onClick={cycleTheme}
        style={{ position: 'absolute', top: 20, right: 24, zIndex: 10 }}>
        {THEME_META[theme].icon} {THEME_META[theme].label.split(' ')[1]}
      </button>
      <div className="auth-card">
        <div className="auth-header">
          <span className="auth-logo">🎓</span>
          <h1>University Event Management System</h1>
          <p>✨ Event Management Portal</p>
        </div>
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>University Email</label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="yourname@anurag.edu.in"
              required
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Enter your password"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? '⏳ Signing in...' : '🚀 Sign In'}
          </button>
        </form>
        <div className="auth-footer">
          <p>Don't have an account? <Link to="/register">Register here</Link></p>
          <div className="demo-accounts">
            <p className="demo-title">Demo Accounts:</p>
            <div className="demo-grid">
              <div><strong>Student:</strong> student@anurag.edu.in / student@123</div>
              <div><strong>Coordinator:</strong> coordinator@anurag.edu.in / coord@123</div>
              <div><strong>Dean:</strong> dean@anurag.edu.in / dean@123</div>
              <div><strong>Admin:</strong> admin@anurag.edu.in / admin@123</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
