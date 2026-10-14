import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';
import { websocketService } from '../api/websocketService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('groupconnect_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('groupconnect_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('groupconnect_token');
      if (storedToken) {
        try {
          const userData = await authApi.getCurrentUser();
          setUser(userData);
          localStorage.setItem('groupconnect_user', JSON.stringify(userData));
        } catch (err) {
          console.error('Session expired or invalid:', err);
          logout();
        }
      }
      setLoading(false);
    };

    initAuth();

    const handleAutoLogout = () => logout();
    window.addEventListener('auth:logout', handleAutoLogout);
    return () => window.removeEventListener('auth:logout', handleAutoLogout);
  }, []);

  const login = async (credentials) => {
    const data = await authApi.login(credentials);
    const userData = {
      id: data.id,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
    };
    localStorage.setItem('groupconnect_token', data.token);
    localStorage.setItem('groupconnect_user', JSON.stringify(userData));
    setToken(data.token);
    setUser(userData);
    return data;
  };

  const register = async (userDataInput) => {
    const data = await authApi.register(userDataInput);
    const userData = {
      id: data.id,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
    };
    localStorage.setItem('groupconnect_token', data.token);
    localStorage.setItem('groupconnect_user', JSON.stringify(userData));
    setToken(data.token);
    setUser(userData);
    return data;
  };

  const logout = () => {
    websocketService.disconnect();
    localStorage.removeItem('groupconnect_token');
    localStorage.removeItem('groupconnect_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
