import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { MessageSquare, Lock, User, Mail, UserCheck, AlertCircle } from 'lucide-react';

export default function AuthForm() {
  const { login, register } = useAuth();
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLogin) {
        await login({
          usernameOrEmail: formData.username,
          password: formData.password,
        });
      } else {
        await register({
          username: formData.username,
          email: formData.email,
          password: formData.password,
          fullName: formData.fullName || formData.username,
        });
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Authentication failed. Please check your credentials.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-card">
      <div className="auth-header">
        <div className="auth-logo">
          <MessageSquare size={28} className="text-primary" />
        </div>
        <h2>{isLogin ? 'Welcome back to GroupConnect' : 'Create your Account'}</h2>
        <p className="auth-subtitle">
          {isLogin ? 'Sign in to access your groups and conversations' : 'Connect and collaborate with your team in real time'}
        </p>
      </div>

      <div className="auth-tabs">
        <button
          type="button"
          className={`tab-btn ${isLogin ? 'active' : ''}`}
          onClick={() => { setIsLogin(true); setError(''); }}
        >
          Sign In
        </button>
        <button
          type="button"
          className={`tab-btn ${!isLogin ? 'active' : ''}`}
          onClick={() => { setIsLogin(false); setError(''); }}
        >
          Register
        </button>
      </div>

      {error && (
        <div className="alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="auth-form">
        {!isLogin && (
          <div className="form-group">
            <label>Full Name</label>
            <div className="input-wrapper">
              <UserCheck size={18} className="input-icon" />
              <input
                type="text"
                name="fullName"
                placeholder="Alex Johnson"
                value={formData.fullName}
                onChange={handleChange}
              />
            </div>
          </div>
        )}

        <div className="form-group">
          <label>{isLogin ? 'Username or Email' : 'Username'}</label>
          <div className="input-wrapper">
            <User size={18} className="input-icon" />
            <input
              type="text"
              name="username"
              placeholder={isLogin ? 'alex or alex@example.com' : 'alex_johnson'}
              value={formData.username}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        {!isLogin && (
          <div className="form-group">
            <label>Email Address</label>
            <div className="input-wrapper">
              <Mail size={18} className="input-icon" />
              <input
                type="email"
                name="email"
                placeholder="alex@example.com"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>
          </div>
        )}

        <div className="form-group">
          <label>Password</label>
          <div className="input-wrapper">
            <Lock size={18} className="input-icon" />
            <input
              type="password"
              name="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              required
              minLength={6}
            />
          </div>
        </div>

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Processing...' : (isLogin ? 'Sign In' : 'Create Account')}
        </button>
      </form>
    </div>
  );
}
