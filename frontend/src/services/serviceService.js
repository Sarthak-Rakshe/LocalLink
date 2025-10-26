import api from "./api";

export const serviceService = {
  async getServices(
    page = 0,
    size = 12,
    sortBy = "id",
    sortDir = "asc",
    filters = {}
  ) {
    // Backend now expects QueryFilter in request body; use POST /api/services/all-services
    const response = await api.post(
      "/api/services/all-services",
      {
        // QueryFilter: { category, userId, serviceName, minPrice, maxPrice }
        // Only include defined values to avoid overriding defaults on server
        ...(filters.category ? { category: filters.category } : {}),
        ...(filters.userId ? { userId: filters.userId } : {}),
        ...(filters.serviceName ? { serviceName: filters.serviceName } : {}),
        ...(filters.minPrice !== undefined && filters.minPrice !== ""
          ? { minPrice: filters.minPrice }
          : {}),
        ...(filters.maxPrice !== undefined && filters.maxPrice !== ""
          ? { maxPrice: filters.maxPrice }
          : {}),
      },
      {
        params: { page, size, sortBy, sortDir },
      }
    );
    return response.data;
  },

  async getServiceById(id) {
    const response = await api.get(`/api/services/${id}`);
    return response.data;
  },

  async getNearbyServices(latitude, longitude, page = 0, size = 10) {
    const response = await api.get("/api/services/nearby", {
      params: {
        userLatitude: latitude,
        userLongitude: longitude,
        page,
        size,
        sortBy: "id",
        sortDir: "asc",
      },
    });
    return response.data;
  },

  async createService(serviceData) {
    const response = await api.post("/api/services", serviceData);
    return response.data;
  },

  async updateService(serviceId, serviceData) {
    const response = await api.put(`/api/services/${serviceId}`, serviceData);
    return response.data;
  },

  async deleteService(serviceId) {
    await api.delete(`/api/services/${serviceId}`);
  },
};
