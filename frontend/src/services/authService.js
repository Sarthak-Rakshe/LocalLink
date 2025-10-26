import api from './api';

export const authService = {
  async login(username, password) {
    const response = await api.post('/api/auth/login', { username, password });
    const { token, refreshToken, userResponse } = response.data;

    localStorage.setItem('authToken', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(userResponse));

    return { user: userResponse, token };
  },

  async register(userData) {
    const response = await api.post('/api/auth/register', userData);
    return response.data;
  },

  async logout() {
    try {
      await api.post('/api/auth/logout');
    } finally {
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  },

  async checkUsernameExists(username) {
    const response = await api.get(`/api/users/exists?username=${username}`);
    return response.data;
  },

  async checkEmailExists(email) {
    const response = await api.get(`/api/users/exists?email=${email}`);
    return response.data;
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return !!localStorage.getItem('authToken');
  },
};
