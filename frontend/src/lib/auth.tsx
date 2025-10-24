import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { UserResponse } from "./api";
import { getToken, clearTokens } from "./api";

interface AuthContextType {
  user: UserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (user: UserResponse, token: string, refreshToken: string) => void;
  logout: () => void;
  updateUser: (user: UserResponse) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  // Initialize from localStorage synchronously to avoid setState in effect
  const [user, setUser] = useState<UserResponse | null>(() => {
    const token = getToken();
    const savedUser = localStorage.getItem("locallink_user");
    if (token && savedUser) {
      try {
        return JSON.parse(savedUser) as UserResponse;
      } catch {
        clearTokens();
      }
    }
    return null;
  });
  const [isLoading] = useState(false);

  const login = (userData: UserResponse, token: string, refreshToken: string) => {
    setUser(userData);
    localStorage.setItem("locallink_user", JSON.stringify(userData));
    localStorage.setItem("locallink_token", token);
    localStorage.setItem("locallink_refresh_token", refreshToken);
  };

  const logout = () => {
    setUser(null);
    clearTokens();
  };

  const updateUser = (userData: UserResponse) => {
    setUser(userData);
    localStorage.setItem("locallink_user", JSON.stringify(userData));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
