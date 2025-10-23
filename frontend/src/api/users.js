import axiosClient from "./axiosClient";
import { API } from "./endpoints";
import { usersPagingParams } from "./queryParams";

export const usersApi = {
  getAll: async () => axiosClient.get(API.USERS.GET_ALL),
  getById: async (id) => axiosClient.get(API.USERS.GET_BY_ID(id)),
  getProviders: async ({
    page = 0,
    size = 4,
    sortBy = "id",
    sortDir = "asc",
  } = {}) =>
    axiosClient.get(API.USERS.GET_PROVIDERS, {
      params: usersPagingParams({ page, size, sortBy, sortDir }),
    }),
};

export default usersApi;
