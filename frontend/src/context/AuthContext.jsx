/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { AuthService } from "../services/auth.service.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const me = await AuthService.me();
        if (active) setUser(me);
      } catch {
        // not logged in or error
        if (active) setUser(null);
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, []);

  const login = async (credentials) => {
    await AuthService.login(credentials);
    const me = await AuthService.me();
    setUser(me);
    return me;
  };

  const register = async (payload) => {
    await AuthService.register(payload);
    const me = await AuthService.me();
    setUser(me);
    return me;
  };

  const logout = async () => {
    await AuthService.logout();
    setUser(null);
  };

  const value = useMemo(
    () => ({ user, setUser, loading, login, register, logout }),
    [user, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
