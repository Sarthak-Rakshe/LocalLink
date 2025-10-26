import api from "./api";

export const userService = {
  async getProfile() {
    const response = await api.get("/api/users/profile");
    return response.data;
  },

  async getProviders(
    page = 0,
    size = 10,
    sortBy = "providerName",
    sortDir = "asc"
  ) {
    const response = await api.get("/api/users/getProviders", {
      params: { page, size, "sort-by": sortBy, "sort-dir": sortDir },
    });
    return response.data;
  },

  async getUserById(userId) {
    const response = await api.get(`/api/users/${userId}`);
    return response.data;
  },

  // New provider-specific endpoint
  async getProviderById(providerId) {
    const response = await api.get(`/api/users/provider/${providerId}`);
    return response.data;
  },

  async getAllUsers(page = 0, size = 10, sortBy = "userName", sortDir = "asc") {
    const response = await api.get("/api/users/all", {
      params: { page, size, "sort-by": sortBy, "sort-dir": sortDir },
    });
    return response.data;
  },

  async updateUser(userId, userData) {
    const response = await api.put(`/api/users/${userId}`, userData);
    return response.data;
  },

  async deactivateUser(userId) {
    const response = await api.put(`/api/users/${userId}/deactivate`);
    return response.data;
  },

  async activateUser(userId) {
    const response = await api.put(`/api/users/${userId}/activate`);
    return response.data;
  },

  async deleteUser(userId) {
    await api.delete(`/api/users/${userId}`);
  },
};
