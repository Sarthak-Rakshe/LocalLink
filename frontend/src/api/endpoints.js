export const API = {
  AUTH: {
    LOGIN: "/auth/login",
    REGISTER: "/auth/register",
    REFRESH: "/auth/refresh",
    LOGOUT: "/auth/logout",
  },

  USERS: {
    // Profile
    GET_PROFILE: "/users/profile",
    // Users CRUD and actions
    GET_BY_ID: (id) => `/users/${id}`,
    GET_ALL: "/users/all",
    GET_PROVIDERS: "users/getProviders",
    UPDATE_BY_ID: (id) => `/users/${id}`,
    DEACTIVATE: (id) => `/users/${id}/deactivate`,
    ACTIVATE: (id) => `/users/${id}/activate`,
    DELETE_BY_ID: (id) => `/users/${id}`,
    EXISTS: "/users/exists",
  },

  SERVICES: {
    GET_BY_ID: (id) => `/services/${id}`,
    LIST: "/services",
    BY_PROVIDER: (providerId) => `/services/provider/${providerId}`,
    BY_CATEGORY: (category) => `/services/category/${category}`,
    NEARBY: "/services/nearby",
    CREATE: "/services",
    UPDATE: (id) => `/services/${id}`,
    DELETE: (id) => `/services/${id}`,
  },

  AVAILABILITY: {
    // Read endpoints
    RULES_FOR_PROVIDER: (serviceProviderId) =>
      `/availability/rules/${serviceProviderId}`,
    // Note: Some controller mappings use a hyphenated variable in the path; client uses canonical form
    RULES_FOR_SERVICE: (serviceProviderId, serviceId) =>
      `/availability/rules/${serviceProviderId}/${serviceId}`,
    EXCEPTIONS_FOR_PROVIDER: (serviceProviderId) =>
      `/availability/exceptions/${serviceProviderId}`,
    EXCEPTIONS_FOR_SERVICE: (serviceProviderId, serviceId) =>
      `/availability/exceptions/${serviceProviderId}/${serviceId}`,
    RULES_DAY_AND_TIME: "/availability/rules/dayAndTime",
    AVAILABLE_DAYS_FOR_SERVICE: (serviceId) =>
      `/availability/rules/${serviceId}/availableDays`,
    AVAILABLE_SLOTS: (serviceProviderId, serviceId) =>
      `/availability/availableSlots/${serviceProviderId}/${serviceId}`,

    // Write endpoints
    CREATE_RULE: "/availability/rules",
    CREATE_EXCEPTION: "/availability/exceptions",
    CHECK_STATUS: "/availability/status",
    UPDATE_RULE: (ruleId) => `/availability/rules/${ruleId}`,
    UPDATE_EXCEPTION: (exceptionId) =>
      `/availability/exceptions/${exceptionId}`,
    DELETE_RULE: (ruleId) => `/availability/rules/${ruleId}`,
    DELETE_EXCEPTION: (exceptionId) =>
      `/availability/exceptions/${exceptionId}`,
  },

  BOOKINGS: {
    GET_BY_ID: (id) => `/bookings/${id}`,
    LIST: "/bookings",
    BY_SERVICE_PROVIDER: (serviceProviderId) =>
      `/bookings/serviceProvider/${serviceProviderId}`,
    BY_SERVICE_PROVIDER_BY_DATE: (serviceProviderId) =>
      `/bookings/serviceProvider/${serviceProviderId}/by-date`,
    BY_CUSTOMER: (customerId) => `/bookings/customer/${customerId}`,
    SUMMARY_FOR_PROVIDER: (serviceProviderId) =>
      `/bookings/summary/${serviceProviderId}`,
    BOOKED_SLOTS: (serviceProviderId, serviceId) =>
      `/bookings/bookedSlots/${serviceProviderId}/${serviceId}`,
    CREATE: "/bookings",
    UPDATE_STATUS: (bookingId, status) =>
      `/bookings/${bookingId}/updateStatus/${status}`,
    RESCHEDULE: (bookingId) => `/bookings/${bookingId}/reschedule`,
    DELETE_BY_ID: (id) => `/bookings/${id}`,
  },

  REVIEWS: {
    MY_REVIEWS_CUSTOMER: "/reviews/customer/myReviews",
    MY_REVIEWS_PROVIDER: "/reviews/serviceProvider/myReviews",
    FOR_SERVICE: (serviceId) => `/reviews/${serviceId}/service`,
    CREATE: "/reviews",
    UPDATE: (reviewId) => `/reviews/${reviewId}`,
    DELETE: (reviewId) => `/reviews/${reviewId}`,
    AVERAGE_FOR_SERVICE: (serviceId) =>
      `/reviews/services/${serviceId}/average`,
    AGGREGATE_FOR_PROVIDER: (serviceProviderId) =>
      `/reviews/providers/${serviceProviderId}/aggregate`,
  },

  PAYMENTS: {
    GET_BY_ID: (id) => `/payments/${id}`,
    LIST: "/payments",
    CREATE_ORDER: (amount) => `/payments/createOrder/${amount}`,
    PROCESS_PAYMENT: "/payments/processPayment",
    HANDLE_WEBHOOK: "/payments/handleWebhook",
  },
};
