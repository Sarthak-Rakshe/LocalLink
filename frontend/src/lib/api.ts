/**
 * API Client for LocalLink Backend
 * Base URL: http://localhost:8088
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8088";

// Token management
const TOKEN_KEY = "locallink_token";
const REFRESH_TOKEN_KEY = "locallink_refresh_token";

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY);
export const setTokens = (token: string, refreshToken: string) => {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
};
export const clearTokens = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem("locallink_user");
};

interface ApiOptions extends RequestInit {
  auth?: boolean;
  params?: Record<string, any>;
}

async function apiRequest<T>(
  endpoint: string,
  options: ApiOptions = {}
): Promise<T> {
  const { auth = false, params, ...fetchOptions } = options;
  
  let url = `${API_BASE_URL}${endpoint}`;
  
  // Add query params
  if (params) {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        searchParams.append(key, String(value));
      }
    });
    const queryString = searchParams.toString();
    if (queryString) url += `?${queryString}`;
  }

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(fetchOptions.headers as Record<string, string>),
  };

  // Always attach Authorization header if a token exists, even for public endpoints.
  // This allows calling endpoints that may accept optional auth without forcing auth:true.
  const existingAuthHeader = (headers as Record<string, string>)["Authorization"];
  if (!existingAuthHeader) {
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    ...fetchOptions,
    headers,
  });

  if (!response.ok) {
    if (response.status === 401) {
      // If we had a token (even for non-auth endpoints), try to refresh silently.
      const hadToken = !!getToken();
      if (auth || hadToken) {
        const refreshed = await refreshTokenIfNeeded();
        if (refreshed) {
          // Retry the original request once after refresh
          return apiRequest(endpoint, options);
        }
        // If this was a protected call, clear and redirect to login.
        if (auth) {
          clearTokens();
          window.location.href = "/login";
        }
      }
    }
    const error = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(error.message || `API Error: ${response.status}`);
  }

  return response.json();
}

let isRefreshing = false;

async function refreshTokenIfNeeded(): Promise<boolean> {
  if (isRefreshing) return false;
  
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;

  try {
    isRefreshing = true;
    const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refresh: refreshToken }),
    });

    if (!response.ok) return false;

    const data = await response.json();
    setTokens(data.token, data.refreshToken);
    return true;
  } catch {
    return false;
  } finally {
    isRefreshing = false;
  }
}

// ============= AUTH APIs =============

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  userName: string;
  userEmail: string;
  userContact: string;
  userType: "CUSTOMER" | "PROVIDER" | "ADMIN";
  userAddress: string;
  userPassword: string; // Backend expects `userPassword`
}

export interface UserResponse {
  userId: number;
  userName: string;
  userEmail: string;
  userContact: string;
  userType: string;
  userAddress: string;
  isActive: boolean;
}

export interface JwtResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  userResponse: UserResponse;
}

export const authApi = {
  login: (data: LoginRequest) =>
    apiRequest<JwtResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  register: (data: RegisterRequest) =>
    apiRequest<UserResponse>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  logout: () =>
    apiRequest<{ message: string }>("/api/auth/logout", {
      method: "POST",
      auth: true,
    }),

  checkExists: (params: { username?: string; email?: string; contact?: string }) =>
    apiRequest<boolean>("/api/users/exists", { params }),
};

// ============= USER APIs =============

export const userApi = {
  getProfile: () =>
    apiRequest<UserResponse>("/api/users/profile", { auth: true }),

  getUser: (userId: number) =>
    apiRequest<UserResponse>(`/api/users/${userId}`, { auth: true }),

  getAllUsers: (params?: { sortBy?: string; sortDir?: string; page?: number; size?: number }) =>
    apiRequest<any>("/api/users/all", { auth: true, params }),

  getProviders: (params?: { page?: number; size?: number; sortBy?: string; sortDir?: string }) =>
    apiRequest<any>("/api/users/getProviders", { params }),

  updateUser: (userId: number, data: Partial<UserResponse>) =>
    apiRequest<UserResponse>(`/api/users/${userId}`, {
      method: "PUT",
      auth: true,
      body: JSON.stringify(data),
    }),

  deactivateUser: (userId: number) =>
    apiRequest<any>(`/api/users/${userId}/deactivate`, {
      method: "PUT",
      auth: true,
    }),

  activateUser: (userId: number) =>
    apiRequest<any>(`/api/users/${userId}/activate`, {
      method: "PUT",
      auth: true,
    }),

  deleteUser: (userId: number) =>
    apiRequest<any>(`/api/users/${userId}`, {
      method: "DELETE",
      auth: true,
    }),
};

// ============= SERVICE APIs =============

export const serviceApi = {
  getService: (id: number) =>
    apiRequest<any>(`/api/services/${id}`),

  getAllServices: (params?: { page?: number; size?: number; sortBy?: string; sortDir?: string }) =>
    apiRequest<any>("/api/services", { params }),

  getServicesByProvider: (providerId: number, params?: any) =>
    apiRequest<any>(`/api/services/provider/${providerId}`, { params }),

  getServicesByCategory: (category: string, params?: any) =>
    apiRequest<any>(`/api/services/category/${category}`, { params }),

  getNearbyServices: (params: { userLatitude: number; userLongitude: number; page?: number; size?: number }) =>
    apiRequest<any>("/api/services/nearby", { params }),

  createService: (data: any) =>
    apiRequest<any>("/api/services", {
      method: "POST",
      auth: true,
      body: JSON.stringify(data),
    }),

  updateService: (serviceId: number, data: any) =>
    apiRequest<any>(`/api/services/${serviceId}`, {
      method: "PUT",
      auth: true,
      body: JSON.stringify(data),
    }),

  deleteService: (serviceId: number) =>
    apiRequest<any>(`/api/services/${serviceId}`, {
      method: "DELETE",
      auth: true,
    }),
};

// ============= REVIEW APIs =============

export const reviewApi = {
  getServiceReviews: (serviceId: number, params?: any) =>
    apiRequest<any>(`/api/reviews/${serviceId}/service`, { params }),

  getAverageRating: (serviceId: number) =>
    apiRequest<number>(`/api/reviews/services/${serviceId}/average`),

  getMyReviews: (params?: any) =>
    apiRequest<any>("/api/reviews/customer/myReviews", { auth: true, params }),

  getProviderReviews: (params?: any) =>
    apiRequest<any>("/api/reviews/serviceProvider/myReviews", { auth: true, params }),

  createReview: (data: any) =>
    apiRequest<any>("/api/reviews", {
      method: "POST",
      auth: true,
      body: JSON.stringify(data),
    }),

  updateReview: (reviewId: number, data: any) =>
    apiRequest<any>(`/api/reviews/${reviewId}`, {
      method: "PUT",
      auth: true,
      body: JSON.stringify(data),
    }),

  deleteReview: (reviewId: number) =>
    apiRequest<any>(`/api/reviews/${reviewId}`, {
      method: "DELETE",
      auth: true,
    }),

  getAggregatesByServiceIds: (serviceIds: number[]) =>
    apiRequest<any>("/api/reviews/getAggregatesByServiceIds", {
      method: "POST",
      body: JSON.stringify(serviceIds),
    }),
};

// ============= AVAILABILITY APIs =============

export const availabilityApi = {
  getRules: (serviceProviderId: number) =>
    apiRequest<any>(`/api/availability/rules/${serviceProviderId}`, { auth: true }),

  getAvailableDays: (serviceId: number) =>
    apiRequest<string[]>(`/api/availability/rules/${serviceId}/availableDays`),

  getAvailableSlots: (serviceProviderId: number, serviceId: number, date: string) =>
    apiRequest<any>(`/api/availability/availableSlots/${serviceProviderId}/${serviceId}`, {
      params: { date },
    }),

  checkStatus: (data: any) =>
    apiRequest<any>("/api/availability/status", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  createRule: (data: any) =>
    apiRequest<any>("/api/availability/rules", {
      method: "POST",
      auth: true,
      body: JSON.stringify(data),
    }),

  updateRule: (ruleId: number, data: any) =>
    apiRequest<any>(`/api/availability/rules/${ruleId}`, {
      method: "PUT",
      auth: true,
      body: JSON.stringify(data),
    }),

  deleteRule: (ruleId: number) =>
    apiRequest<any>(`/api/availability/rules/${ruleId}`, {
      method: "DELETE",
      auth: true,
    }),

  createException: (data: any) =>
    apiRequest<any>("/api/availability/exceptions", {
      method: "POST",
      auth: true,
      body: JSON.stringify(data),
    }),
};

// ============= BOOKING APIs =============

export const bookingApi = {
  getBooking: (id: number) =>
    apiRequest<any>(`/api/bookings/${id}`, { auth: true }),

  getAllBookings: (params?: any) =>
    apiRequest<any>("/api/bookings", { auth: true, params }),

  getProviderBookings: (serviceProviderId: number, params?: any) =>
    apiRequest<any>(`/api/bookings/serviceProvider/${serviceProviderId}`, { auth: true, params }),

  getProviderBookingsByDate: (serviceProviderId: number, date: string) =>
    apiRequest<any>(`/api/bookings/serviceProvider/${serviceProviderId}/by-date`, {
      auth: true,
      params: { date },
    }),

  getCustomerBookings: (customerId: number, params?: any) =>
    apiRequest<any>(`/api/bookings/customer/${customerId}`, { auth: true, params }),

  getBookedSlots: (serviceProviderId: number, serviceId: number, date: string) =>
    apiRequest<any>(`/api/bookings/bookedSlots/${serviceProviderId}/${serviceId}`, {
      params: { date },
    }),

  createBooking: (data: any) =>
    apiRequest<any>("/api/bookings", {
      method: "POST",
      auth: true,
      body: JSON.stringify(data),
    }),

  updateStatus: (bookingId: number, status: string) =>
    apiRequest<any>(`/api/bookings/${bookingId}/updateStatus/${status}`, {
      method: "POST",
      auth: true,
    }),

  reschedule: (bookingId: number, data: any) =>
    apiRequest<any>(`/api/bookings/${bookingId}/reschedule`, {
      method: "POST",
      auth: true,
      body: JSON.stringify(data),
    }),

  deleteBooking: (id: number) =>
    apiRequest<any>(`/api/bookings/${id}`, {
      method: "DELETE",
      auth: true,
    }),
};

// ============= PAYMENT APIs =============

export const paymentApi = {
  getPayments: (params?: any) =>
    apiRequest<any>("/api/payments", { auth: true, params }),

  getPayment: (id: number) =>
    apiRequest<any>(`/api/payments/${id}`, { auth: true }),

  createOrder: (amount: number) =>
    apiRequest<string>(`/api/payments/createOrder/${amount}`, {
      method: "POST",
      auth: true,
    }),

  processPayment: (data: any) =>
    apiRequest<any>("/api/payments/processPayment", {
      method: "POST",
      auth: true,
      body: JSON.stringify(data),
    }),
};
