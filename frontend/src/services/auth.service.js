import { Auth, Users } from "./api.js";
import { setAuthToken, clearAuthToken, getAuthToken } from "./apiClient.js";

function extractToken(resp) {
  if (!resp) return null;
  const candidates = [
    resp.token,
    resp.accessToken,
    resp.access_token,
    resp.jwt,
    resp.id_token,
  ];
  return candidates.find(Boolean) || null;
}

export const AuthService = {
  async login(credentials) {
    const data = await Auth.login(credentials);
    const token = extractToken(data);
    if (token) setAuthToken(token);
    return data;
  },
  async logout() {
    try {
      await Auth.logout();
    } finally {
      clearAuthToken();
    }
  },
  async me() {
    // Ensure token is present to call /users/profile in JWT mode; in cookie mode it may still work
    const _ = getAuthToken();
    return Users.getCurrentProfile();
  },
};
