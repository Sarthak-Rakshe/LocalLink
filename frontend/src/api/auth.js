import axiosClient from "./axiosClient";
import { API } from "./endpoints";

export const authApi = {
  login: async ({ username, password }) => {
    const res = await axiosClient.post(API.AUTH.LOGIN, { username, password });
    return res; // JwtResponseDto
  },
  register: async ({
    userName,
    userEmail,
    userContact,
    userType,
    userPassword,
    userAddress,
    isActive = true,
  }) => {
    const res = await axiosClient.post(API.AUTH.REGISTER, {
      userName,
      userEmail,
      userContact,
      userType,
      userPassword,
      userAddress,
      isActive,
    });
    return res; // UserResponse
  },
  refresh: async (refresh) => {
    const res = await axiosClient.post(API.AUTH.REFRESH, { refresh });
    return res;
  },
};
