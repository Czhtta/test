import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { authService } from '../services/authService';
import { User } from '../types';
import api from '../services/api';
import { AxiosError } from 'axios';

interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(authService.isAuthenticated());
  const [user, setUser] = useState<User | null>(null);

  const fetchCurrentUser = async () => {
    try {
      const res = await api.get('/auth/me');
      setUser(res.data);
    } catch (error) {
      console.error('Failed to fetch current user:', error);
      throw error;
    }
  };

  const login = async (username: string, password: string) => {
    const response = await authService.login({ username, password });
    authService.setToken(response.token);
    setIsAuthenticated(true);
    try {
      await fetchCurrentUser();
    } catch (error) {
      // 出错时立刻清除 token，并把清晰的错误信息抛出去
      authService.logout();
      setIsAuthenticated(false);
      setUser(null);
      if (error instanceof AxiosError && error.response?.status === 401) {
        throw new Error('Failed to fetch current user: token invalid or expired');
      } else {
        throw new Error('Failed to fetch current user: unexpected error');
      }
    }
  };AxiosError

  const logout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUser(null);
  };

  useEffect(() => {
    if (isAuthenticated && !user) fetchCurrentUser();
    // eslint-disable-next-line
  }, [isAuthenticated]);

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

