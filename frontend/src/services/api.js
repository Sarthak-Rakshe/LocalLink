// Centralized Axios API layer: wraps backend endpoints per domain
// Assumes an ApiGateway exposes these paths; adjust as needed.
// Requests use withCredentials (see apiClient.js). Auth is JWT-based per backend.

import api from "./apiClient.js";

// --- AUTH (/api/auth) ---
export const Auth = {
  /** @param {{username?: string, email?: string, password: string}} payload */
  login: (payload) => api.post("/auth/login", payload).then((r) => r.data),
  /** @param {{username: string, email: string, password: string, userType: 'CUSTOMER'|'PROVIDER'}} payload */
  register: (payload) =>
    api.post("/auth/register", payload).then((r) => r.data),
  /** @param {{refresh: string}} payload */
  refresh: (payload) => api.post("/auth/refresh", payload).then((r) => r.data),
  logout: () => api.post("/auth/logout").then((r) => r.data),
};

// --- USERS (/api/users) ---
export const Users = {
  /** current user profile */
  getCurrentProfile: () => api.get(`/users/profile`).then((r) => r.data),
  /** @param {number} userId */
  getById: (userId) => api.get(`/users/${userId}`).then((r) => r.data),
  /** admin list */
  getAll: (params) => api.get(`/users/all`, { params }).then((r) => r.data), // params: { 'sort-by', 'sort-dir', page, size }
  /** @param {number} id @param {object} body */
  update: (id, body) => api.put(`/users/${id}`, body).then((r) => r.data),
  deactivate: (id) => api.put(`/users/${id}/deactivate`).then((r) => r.data),
  activate: (id) => api.put(`/users/${id}/activate`).then((r) => r.data),
  delete: (id) => api.delete(`/users/${id}`).then((r) => r.data),
  /** any of username/email/contact */
  exists: (params) => api.get(`/users/exists`, { params }).then((r) => r.data),
  /** providers listing with optional backend filter (providerName/providerEmail)
   * @param {{providerName?: string, providerEmail?: string}|undefined} filter
   * @param {{page?: number, size?: number, 'sort-by'?: string, 'sort-dir'?: string}|undefined} page
   */
  getProviders: (filter, page) =>
    api
      .request({
        method: "get",
        url: `/users/getProviders`,
        params: page,
        data: filter ?? undefined,
      })
      .then((r) => r.data),
  /** provider public profile */
  getProviderById: (providerId) =>
    api.get(`/users/provider/${providerId}`).then((r) => r.data),
};

// --- SERVICE LISTING (/api/services) ---
export const Services = {
  /** @param {number} id */
  getById: (id) => api.get(`/services/${id}`).then((r) => r.data),
  /** @param {object|undefined} queryFilter @param {{page?:number,size?:number,sortBy?:string,sortDir?:string}} page */
  getAll: (queryFilter, page = {}) =>
    api
      .post(`/services/all-services`, queryFilter ?? {}, { params: page })
      .then((r) => r.data),
  /** @param {{userLatitude:number,userLongitude:number,page?:number,size?:number,sortBy?:string,sortDir?:string}} params */
  getNearby: (params) =>
    api.get(`/services/nearby`, { params }).then((r) => r.data),
  /** provider-only */
  create: (body) => api.post(`/services`, body).then((r) => r.data),
  update: (serviceId, body) =>
    api.put(`/services/${serviceId}`, body).then((r) => r.data),
  remove: (serviceId) =>
    api.delete(`/services/${serviceId}`).then((r) => r.data),
};

// --- BOOKINGS (/api/bookings) ---
export const Bookings = {
  /** page list (admin or general): params { 'sort-by','sort-dir', page, size } */
  listAll: (params) => api.get(`/bookings`, { params }).then((r) => r.data),
  /** aggregated list with service, customer and provider details
   * @param {{bookingStatus?: string, serviceCategory?: string, dateFrom?: string, dateTo?: string, serviceProviderId?: number, customerId?: number, serviceId?: number}} queryFilter
   * @param {{page?: number, size?: number, 'sort-by'?: string, 'sort-dir'?: string}} params
   */
  getList: (queryFilter, params) =>
    api
      .post(`/bookings/getList`, queryFilter ?? {}, { params })
      .then((r) => r.data),
  getById: (bookingId) => api.get(`/bookings/${bookingId}`).then((r) => r.data),
  /** provider-specific by date */
  listByProviderOnDate: (serviceProviderId, date) =>
    api
      .get(`/bookings/serviceProvider/${serviceProviderId}/by-date`, {
        params: { date },
      })
      .then((r) => r.data),
  /** provider paginated */
  listByProvider: (serviceProviderId, params) =>
    api
      .get(`/bookings/serviceProvider/${serviceProviderId}`, { params })
      .then((r) => r.data),
  /** customer paginated */
  listByCustomer: (customerId, params) =>
    api.get(`/bookings/customer/${customerId}`, { params }).then((r) => r.data),
  /** booking summary for the authenticated user (provider or customer) */
  mySummary: () => api.get(`/bookings/my-summary`).then((r) => r.data),
  bookedSlots: (serviceProviderId, serviceId, date) =>
    api
      .get(`/bookings/bookedSlots/${serviceProviderId}/${serviceId}`, {
        params: { date },
      })
      .then((r) => r.data),
  create: (bookingDto) => api.post(`/bookings`, bookingDto).then((r) => r.data),
  updateStatus: (bookingId, status) =>
    api
      .post(`/bookings/${bookingId}/updateStatus/${status}`)
      .then((r) => r.data),
  reschedule: (bookingId, payload) =>
    api.post(`/bookings/${bookingId}/reschedule`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/bookings/${id}`).then((r) => r.data),
};

// --- AVAILABILITY (/api/availability) ---
export const Availability = {
  getRulesForProvider: (serviceProviderId) =>
    api.get(`/availability/rules/${serviceProviderId}`).then((r) => r.data),
  // Note: controller has a path typo; expected providerId param name
  getRulesForService: (serviceProviderId, serviceId) =>
    api
      .get(`/availability/rules/${serviceProviderId}/${serviceId}`)
      .then((r) => r.data),
  getExceptionsForProvider: (serviceProviderId) =>
    api
      .get(`/availability/exceptions/${serviceProviderId}`)
      .then((r) => r.data),
  getExceptionsForService: (serviceProviderId, serviceId) =>
    api
      .get(`/availability/exceptions/${serviceProviderId}/${serviceId}`)
      .then((r) => r.data),
  // Controller uses GET with body; axios supports 'data' in config but servers may ignore GET bodies.
  getRulesByDayAndTime: (request, page) =>
    api
      .request({
        method: "get",
        url: `/availability/rules/dayAndTime`,
        params: page,
        data: request,
      })
      .then((r) => r.data),
  getAvailableDays: (serviceId) =>
    api
      .get(`/availability/rules/${serviceId}/availableDays`)
      .then((r) => r.data),
  getAvailableSlots: (serviceProviderId, serviceId, date) =>
    api
      .get(`/availability/availableSlots/${serviceProviderId}/${serviceId}`, {
        params: { date },
      })
      .then((r) => r.data),
  createRule: (rule) =>
    api.post(`/availability/rules`, rule).then((r) => r.data),
  createException: (exception) =>
    api.post(`/availability/exceptions`, exception).then((r) => r.data),
  checkAvailability: (request) =>
    api.post(`/availability/status`, request).then((r) => r.data),
  updateRule: (ruleId, rule) =>
    api.put(`/availability/rules/${ruleId}`, rule).then((r) => r.data),
  updateException: (exceptionId, exception) =>
    api
      .put(`/availability/exceptions/${exceptionId}`, exception)
      .then((r) => r.data),
  deleteRule: (ruleId) =>
    api.delete(`/availability/rules/${ruleId}`).then((r) => r.data),
  deleteException: (exceptionId) =>
    api.delete(`/availability/exceptions/${exceptionId}`).then((r) => r.data),
};

// --- PAYMENTS (/api/payments) ---
export const Payments = {
  getById: (id) => api.get(`/payments/${id}`).then((r) => r.data),
  /** admin list or user scoped based on backend service; params require sort-by, sort-dir, page, size */
  listAll: (params, filter) =>
    api
      .post(`/payments/allTransactions`, filter ?? {}, { params })
      .then((r) => r.data),
  /** @param {{serviceId:number, slot:{startTime:string,endTime:string}, pricePerHour:number}} payload */
  createOrder: (payload) =>
    api.post(`/payments/createOrder`, payload).then((r) => r.data),
  processPayment: (request) =>
    api.post(`/payments/processPayment`, request).then((r) => r.data),
  /** Retry a payment transaction by its transaction id. Backend must expose this endpoint.
   * Default path: POST /payments/{transactionId}/retry -> TransactionDto
   * If your backend uses a different path, update here accordingly.
   */
  retry: (transactionId) =>
    api.post(`/payments/${transactionId}/retry`).then((r) => r.data),
};

// --- REVIEWS (/api/reviews) ---
export const Reviews = {
  myReviewsProvider: (params) =>
    api
      .get(`/reviews/serviceProvider/myReviews`, { params })
      .then((r) => r.data),
  byService: (serviceId, params) =>
    api.get(`/reviews/${serviceId}/service`, { params }).then((r) => r.data),
  myReviewsCustomer: (params) =>
    api.get(`/reviews/customer/myReviews`, { params }).then((r) => r.data),
  add: (review) => api.post(`/reviews`, review).then((r) => r.data),
  update: (reviewId, review) =>
    api.put(`/reviews/${reviewId}`, review).then((r) => r.data),
  remove: (reviewId) => api.delete(`/reviews/${reviewId}`).then((r) => r.data),
  averageForService: (serviceId) =>
    api.get(`/reviews/services/${serviceId}/average`).then((r) => r.data),
  providerAggregates: (serviceProviderIds) =>
    api
      .post(`/reviews/providers/aggregate`, serviceProviderIds)
      .then((r) => r.data),
  byServiceIds: (serviceIds) =>
    api.post(`/reviews/getByServiceIds`, serviceIds).then((r) => r.data),
  aggregatesByServiceIds: (serviceIds) =>
    api
      .post(`/reviews/getAggregatesByServiceIds`, serviceIds)
      .then((r) => r.data),
};

// Aggregate export if you prefer a single import
export const API = {
  Auth,
  Users,
  Services,
  Bookings,
  Availability,
  Payments,
  Reviews,
};
