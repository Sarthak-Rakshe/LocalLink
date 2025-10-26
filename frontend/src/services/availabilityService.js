import api from "./api";

export const availabilityService = {
  async getAvailableSlots(serviceProviderId, serviceId, date) {
    const response = await api.get(
      `/api/availability/availableSlots/${serviceProviderId}/${serviceId}`,
      {
        params: { date },
      }
    );
    return response.data; // { date, availableSlots: [{startTime,endTime}], isDayAvailable }
  },

  async checkAvailability({
    serviceProviderId,
    serviceId,
    date,
    startTime,
    endTime,
  }) {
    const response = await api.post("/api/availability/status", {
      serviceProviderId,
      serviceId,
      date,
      startTime,
      endTime,
    });
    return response.data; // { status, ... }
  },

  // Management APIs for providers
  async getRulesByProvider(serviceProviderId) {
    const response = await api.get(
      `/api/availability/rules/${serviceProviderId}`
    );
    return response.data; // AvailabilityRulesDto[]
  },

  async getExceptionsByProvider(serviceProviderId) {
    const response = await api.get(
      `/api/availability/exceptions/${serviceProviderId}`
    );
    return response.data; // ProviderExceptionDto[]
  },

  async createRule(rule) {
    const response = await api.post(`/api/availability/rules`, rule);
    return response.data;
  },

  async deleteRule(ruleId) {
    await api.delete(`/api/availability/rules/${ruleId}`);
  },

  async createException(exception) {
    const response = await api.post(`/api/availability/exceptions`, exception);
    return response.data;
  },

  async deleteException(exceptionId) {
    await api.delete(`/api/availability/exceptions/${exceptionId}`);
  },
};
