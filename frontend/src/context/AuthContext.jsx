import { createContext, useContext, useEffect, useMemo, useState } from "react";
import axios from "axios";
import axiosClient from "../api/axiosClient";
import { API } from "../api/endpoints";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(() => localStorage.getItem("authToken"));

  // Load profile if token exists
  useEffect(() => {
    let mounted = true;
    async function bootstrap() {
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        // Try to load profile from API for freshness
        const profile = await axiosClient.get(API.USERS.GET_PROFILE);
        if (mounted) setUser(profile);
      } catch {
        localStorage.removeItem("authToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("tokenType");
        setToken(null);
      } finally {
        if (mounted) setLoading(false);
      }
    }
    bootstrap();
    return () => {
      mounted = false;
    };
  }, [token]);

  async function login(credentials) {
    // credentials: { username, password }
    const res = await axiosClient.post(API.AUTH.LOGIN, credentials);
    // Backend returns: { token, refreshToken, tokenType, userResponse }
    const {
      token: accessToken,
      refreshToken,
      tokenType = "Bearer",
      userResponse,
    } = res || {};
    if (!accessToken) throw new Error("Login failed: missing token");
    localStorage.setItem("authToken", accessToken);
    if (refreshToken) localStorage.setItem("refreshToken", refreshToken);
    if (tokenType) localStorage.setItem("tokenType", tokenType);
    setToken(accessToken);
    // We have userResponse already; set it to user. You can also re-fetch profile for freshness.
    if (userResponse) {
      setUser(userResponse);
      return userResponse;
    }
    const profile = await axiosClient.get(API.USERS.GET_PROFILE);
    setUser(profile);
    return profile;
  }

  async function register(payload) {
    // payload: { username, email, password, ... }
    const res = await axiosClient.post(API.AUTH.REGISTER, payload);
    // If backend auto-logs in on register with { token, refreshToken, tokenType, userResponse }
    const {
      token: accessToken,
      refreshToken,
      tokenType = "Bearer",
      userResponse,
    } = res || {};
    if (accessToken) {
      localStorage.setItem("authToken", accessToken);
      if (refreshToken) localStorage.setItem("refreshToken", refreshToken);
      if (tokenType) localStorage.setItem("tokenType", tokenType);
      setToken(accessToken);
      if (userResponse) {
        setUser(userResponse);
        return userResponse;
      }
      const profile = await axiosClient.get(API.USERS.GET_PROFILE);
      setUser(profile);
      return profile;
    }
    return res;
  }

  function logout() {
    // best-effort server logout if exists
    axios
      .post(`${axiosClient.defaults.baseURL}${API.AUTH.LOGOUT}`)
      .catch(() => {});
    localStorage.removeItem("authToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("tokenType");
    setToken(null);
    setUser(null);
  }

  const value = useMemo(
    () => ({
      user,
      token,
      loading,
      login,
      register,
      logout,
      isAuthenticated: !!user,
    }),
    [user, token, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
