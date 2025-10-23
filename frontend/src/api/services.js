import axiosClient from "./axiosClient";
import { API } from "./endpoints";
import { servicesPagingParams } from "./queryParams";

export const servicesApi = {
  /**
   * Fetch services near a given coordinate.
   * Returns PagedResponse<ServiceItemDto> where ServiceItemDto matches backend POJO.
   */
  getNearby: async ({
    latitude,
    longitude,
    page = 0,
    size = 24,
    sortBy = "service_id",
    sortDir = "asc",
  }) => {
    const params = {
      ...servicesPagingParams({ page, size, sortBy, sortDir }),
      userLatitude: latitude,
      userLongitude: longitude,
    };
    return await axiosClient.get(API.SERVICES.NEARBY, { params });
  },
  getById: async (serviceId) =>
    axiosClient.get(API.SERVICES.GET_BY_ID(serviceId)),
  list: async ({
    page = 0,
    size = 12,
    sortBy = "name",
    sortDir = "asc",
  } = {}) => {
    const params = servicesPagingParams({ page, size, sortBy, sortDir });
    return await axiosClient.get(API.SERVICES.LIST, { params });
  },
  byProvider: async ({
    providerId,
    page = 0,
    size = 20,
    sortBy = "id",
    sortDir = "asc",
  }) => {
    const params = servicesPagingParams({ page, size, sortBy, sortDir });
    return await axiosClient.get(API.SERVICES.BY_PROVIDER(providerId), {
      params,
    });
  },
  byCategory: async ({
    category,
    page = 0,
    size = 12,
    sortBy = "name",
    sortDir = "asc",
  }) => {
    const params = servicesPagingParams({ page, size, sortBy, sortDir });
    return await axiosClient.get(API.SERVICES.BY_CATEGORY(category), {
      params,
    });
  },
  create: async (payload) => axiosClient.post(API.SERVICES.CREATE, payload),
  update: async (serviceId, payload) =>
    axiosClient.put(API.SERVICES.UPDATE(serviceId), payload),
  remove: async (serviceId) =>
    axiosClient.delete(API.SERVICES.DELETE(serviceId)),
};

export default servicesApi;
