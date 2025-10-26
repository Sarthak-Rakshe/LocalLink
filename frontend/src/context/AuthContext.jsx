import { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const currentUser = authService.getCurrentUser();
    if (currentUser) {
      setUser(currentUser);
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    const { user } = await authService.login(username, password);
    setUser(user);
    return user;
  };

  const register = async (userData) => {
    const user = await authService.register(userData);
    return user;
  };

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  const isCustomer = () => user?.userType === 'CUSTOMER';
  const isProvider = () => user?.userType === 'PROVIDER';
  const isAdmin = () => user?.userRole === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        isCustomer,
        isProvider,
        isAdmin,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
