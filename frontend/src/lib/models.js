/**
 * Frontend POJOs (Plain JS Objects) and mappers for LocalLink
 *
 * This file centralizes the data shapes used across the app and
 * provides robust fromJSON(...) mappers so UI code stays clean.
 * Uses JSDoc typedefs for great IntelliSense in JS projects.
 */

// ===============
// Helper functions
// ===============

/** @param {any} v */
const toNumber = (v) => (v == null || v === "" ? undefined : Number(v));
/** @param {any} v */
const toInt = (v) => (v == null || v === "" ? undefined : parseInt(v, 10));
/** @param {any} v */
const toBool = (v) =>
  typeof v === "boolean"
    ? v
    : v === "true"
    ? true
    : v === "false"
    ? false
    : undefined;
/** @param {any} v */
const toString = (v) => (v == null ? undefined : String(v));
/** @param {any} v */
const toDate = (v) => {
  if (!v) return undefined;
  try {
    const d = new Date(v);
    return isNaN(d.getTime()) ? undefined : d;
  } catch {
    return undefined;
  }
};

/**
 * Map a Spring Data paged response to a normalized object.
 * @template T
 * @param {any} json Raw API payload
 * @param {(item:any)=>T} mapper Function to map each item in content
 * @returns {{ content: T[], page: number, size: number, totalElements: number, totalPages: number, first?: boolean, last?: boolean, numberOfElements?: number, empty?: boolean }}
 */
export function mapPagedResponse(json, mapper) {
  const content = Array.isArray(json?.content) ? json.content.map(mapper) : [];
  const page = toInt(json?.number) ?? toInt(json?.page) ?? 0;
  const size = toInt(json?.size) ?? 0;
  const totalElements = toInt(json?.totalElements) ?? content.length;
  const totalPages = toInt(json?.totalPages) ?? 1;
  return {
    content,
    page,
    size,
    totalElements,
    totalPages,
    first: Boolean(json?.first),
    last: Boolean(json?.last),
    numberOfElements: toInt(json?.numberOfElements) ?? content.length,
    empty: Boolean(json?.empty ?? content.length === 0),
  };
}

// ======
// Enums
// ======

export const UserRole = Object.freeze({
  CUSTOMER: "CUSTOMER",
  PROVIDER: "PROVIDER",
  ADMIN: "ADMIN",
});

export const BookingStatus = Object.freeze({
  PENDING: "PENDING",
  CONFIRMED: "CONFIRMED",
  CANCELLED: "CANCELLED",
  COMPLETED: "COMPLETED",
});

export const PaymentStatus = Object.freeze({
  CREATED: "CREATED",
  PENDING: "PENDING",
  SUCCESS: "SUCCESS",
  FAILED: "FAILED",
  REFUNDED: "REFUNDED",
});

export const DayOfWeek = Object.freeze({
  MONDAY: "MONDAY",
  TUESDAY: "TUESDAY",
  WEDNESDAY: "WEDNESDAY",
  THURSDAY: "THURSDAY",
  FRIDAY: "FRIDAY",
  SATURDAY: "SATURDAY",
  SUNDAY: "SUNDAY",
});

// =============
// Common types
// =============

/**
 * @typedef {Object} Coordinates
 * @property {number=} latitude
 * @property {number=} longitude
 */

/**
 * @typedef {Object} Address
 * @property {string=} line1
 * @property {string=} line2
 * @property {string=} city
 * @property {string=} state
 * @property {string=} country
 * @property {string=} postalCode
 */

/**
 * @typedef {Object} ReviewAggregate
 * @property {number=} averageRating
 * @property {number=} totalReviews
 */

// =====
// User
// =====

/**
 * @typedef {Object} User
 * @property {number|string=} id
 * @property {string=} name
 * @property {string=} email
 * @property {string=} phone
 * @property {string=} role
 * @property {boolean=} active
 * @property {string=} avatarUrl
 * @property {string=} bio
 */

export class UserModel {
  /** @param {Partial<User>=} init */
  constructor(init = {}) {
    /** @type {number|string|undefined} */ this.id = init.id;
    /** @type {string|undefined} */ this.name = init.name;
    /** @type {string|undefined} */ this.email = init.email;
    /** @type {string|undefined} */ this.phone = init.phone;
    /** @type {string|undefined} */ this.role = init.role;
    /** @type {boolean|undefined} */ this.active = init.active;
    /** @type {string|undefined} */ this.avatarUrl = init.avatarUrl;
    /** @type {string|undefined} */ this.bio = init.bio;
  }

  /** @param {any} json */
  static fromJSON(json) {
    return new UserModel({
      id: json?.id ?? json?.userId ?? json?.providerId,
      name: json?.name ?? json?.fullName ?? json?.displayName ?? json?.username,
      email: toString(json?.email),
      phone: toString(json?.phone),
      role: toString(json?.role) ?? UserRole.CUSTOMER,
      active: typeof json?.active === "boolean" ? json.active : undefined,
      avatarUrl: toString(json?.avatarUrl),
      bio: toString(json?.bio),
    });
  }
}

// =========
// Service
// =========

/**
 * Matches DTOs used by the frontend components (ServiceCard, ServiceTile, ServiceDetails).
 * Keep field names aligned with current usage to avoid extra mapping in UI code.
 *
 * @typedef {Object} Service
 * @property {number|string=} serviceId
 * @property {string=} serviceName
 * @property {string=} serviceDescription
 * @property {string=} serviceCategory
 * @property {number=} servicePricePerHour
 * @property {number=} latitude
 * @property {number=} longitude
 * @property {number=} distanceKm
 * @property {number=} providerId
 * @property {string=} providerName
 * @property {ReviewAggregate=} reviewAggregate
 */

export class ServiceModel {
  /** @param {Partial<Service>=} init */
  constructor(init = {}) {
    /** @type {number|string|undefined} */ this.serviceId = init.serviceId;
    /** @type {string|undefined} */ this.serviceName = init.serviceName;
    /** @type {string|undefined} */ this.serviceDescription =
      init.serviceDescription;
    /** @type {string|undefined} */ this.serviceCategory = init.serviceCategory;
    /** @type {number|undefined} */ this.servicePricePerHour =
      init.servicePricePerHour;
    /** @type {number|undefined} */ this.latitude = init.latitude;
    /** @type {number|undefined} */ this.longitude = init.longitude;
    /** @type {number|undefined} */ this.distanceKm = init.distanceKm;
    /** @type {number|undefined} */ this.providerId = init.providerId;
    /** @type {string|undefined} */ this.providerName = init.providerName;
    /** @type {ReviewAggregate|undefined} */ this.reviewAggregate =
      init.reviewAggregate;
    // Derived helpers used by UI sorting
    /** @type {number|undefined} */ this._averageRating = init._averageRating;
    /** @type {number|undefined} */ this._ratingCount = init._ratingCount;
  }

  /** @param {any} json */
  static fromJSON(json) {
    const reviewAggregate = json?.reviewAggregate
      ? ReviewAggregateModel.fromJSON(json.reviewAggregate)
      : undefined;
    const avg = reviewAggregate?.averageRating ?? toNumber(json?.averageRating);
    const count = reviewAggregate?.totalReviews ?? toInt(json?.ratingCount);
    return new ServiceModel({
      serviceId: json?.serviceId ?? json?.id,
      serviceName: toString(json?.serviceName ?? json?.name),
      serviceDescription: toString(
        json?.serviceDescription ?? json?.description
      ),
      serviceCategory: toString(json?.serviceCategory ?? json?.category),
      servicePricePerHour: toNumber(
        json?.servicePricePerHour ?? json?.pricePerHour ?? json?.price
      ),
      latitude: toNumber(json?.latitude ?? json?.lat),
      longitude: toNumber(json?.longitude ?? json?.lng ?? json?.lon),
      distanceKm: toNumber(json?.distanceKm ?? json?.distance_km),
      providerId: toInt(json?.providerId ?? json?.serviceProviderId),
      providerName: toString(json?.providerName),
      reviewAggregate,
      _averageRating: typeof avg === "number" ? avg : undefined,
      _ratingCount: typeof count === "number" ? count : undefined,
    });
  }
}

export class ReviewAggregateModel {
  /** @param {Partial<ReviewAggregate>=} init */
  constructor(init = {}) {
    /** @type {number|undefined} */ this.averageRating = init.averageRating;
    /** @type {number|undefined} */ this.totalReviews = init.totalReviews;
  }
  /** @param {any} json */
  static fromJSON(json) {
    return new ReviewAggregateModel({
      averageRating: toNumber(json?.averageRating ?? json?.avg ?? json),
      totalReviews: toInt(json?.totalReviews ?? json?.count),
    });
  }
}

// ===========
// Availability
// ===========

/**
 * @typedef {Object} AvailabilityRule
 * @property {number|string=} id
 * @property {number|string=} serviceProviderId
 * @property {number|string=} serviceId
 * @property {string=} dayOfWeek // DayOfWeek enum string
 * @property {string=} startTime // HH:mm
 * @property {string=} endTime   // HH:mm
 * @property {number=} slotDurationMinutes
 * @property {string=} timeZone
 */

export class AvailabilityRuleModel {
  /** @param {Partial<AvailabilityRule>=} init */
  constructor(init = {}) {
    this.id = init.id;
    this.serviceProviderId = init.serviceProviderId;
    this.serviceId = init.serviceId;
    this.dayOfWeek = init.dayOfWeek;
    this.startTime = init.startTime;
    this.endTime = init.endTime;
    this.slotDurationMinutes = init.slotDurationMinutes;
    this.timeZone = init.timeZone;
  }
  /** @param {any} json */
  static fromJSON(json) {
    return new AvailabilityRuleModel({
      id: json?.id ?? json?.ruleId,
      serviceProviderId: json?.serviceProviderId ?? json?.providerId,
      serviceId: json?.serviceId,
      dayOfWeek: toString(json?.dayOfWeek ?? json?.day),
      startTime: toString(json?.startTime),
      endTime: toString(json?.endTime),
      slotDurationMinutes: toInt(
        json?.slotDurationMinutes ?? json?.slotMinutes
      ),
      timeZone: toString(json?.timeZone ?? json?.timezone),
    });
  }
}

/**
 * @typedef {Object} AvailabilityException
 * @property {number|string=} id
 * @property {number|string=} serviceProviderId
 * @property {number|string=} serviceId
 * @property {string=} date // YYYY-MM-DD
 * @property {string=} startTime // HH:mm
 * @property {string=} endTime   // HH:mm
 * @property {boolean=} available
 */

export class AvailabilityExceptionModel {
  /** @param {Partial<AvailabilityException>=} init */
  constructor(init = {}) {
    this.id = init.id;
    this.serviceProviderId = init.serviceProviderId;
    this.serviceId = init.serviceId;
    this.date = init.date;
    this.startTime = init.startTime;
    this.endTime = init.endTime;
    this.available = init.available;
  }
  /** @param {any} json */
  static fromJSON(json) {
    return new AvailabilityExceptionModel({
      id: json?.id ?? json?.exceptionId,
      serviceProviderId: json?.serviceProviderId ?? json?.providerId,
      serviceId: json?.serviceId,
      date: toString(json?.date),
      startTime: toString(json?.startTime),
      endTime: toString(json?.endTime),
      available: toBool(json?.available ?? json?.isAvailable),
    });
  }
}

/**
 * @typedef {Object} AvailableSlot
 * @property {string=} date // YYYY-MM-DD
 * @property {string=} startTime // HH:mm
 * @property {string=} endTime   // HH:mm
 * @property {boolean=} booked
 */

export class SlotModel {
  /** @param {Partial<AvailableSlot>=} init */
  constructor(init = {}) {
    this.date = init.date;
    this.startTime = init.startTime;
    this.endTime = init.endTime;
    this.booked = init.booked;
  }
  /** @param {any} json */
  static fromJSON(json) {
    return new SlotModel({
      date: toString(json?.date),
      startTime: toString(json?.startTime ?? json?.from),
      endTime: toString(json?.endTime ?? json?.to),
      booked: Boolean(json?.booked ?? json?.isBooked),
    });
  }
}

// ========
// Booking
// ========

/**
 * @typedef {Object} Booking
 * @property {number|string=} bookingId
 * @property {number|string=} serviceId
 * @property {number|string=} serviceProviderId
 * @property {number|string=} customerId
 * @property {string=} status // BookingStatus enum string
 * @property {string|Date=} startTime
 * @property {string|Date=} endTime
 * @property {number=} amount
 * @property {string=} currency
 * @property {number|string=} paymentId
 * @property {string=} notes
 * @property {Date=} createdAt
 * @property {Date=} updatedAt
 */

export class BookingModel {
  /** @param {Partial<Booking>=} init */
  constructor(init = {}) {
    this.bookingId = init.bookingId;
    this.serviceId = init.serviceId;
    this.serviceProviderId = init.serviceProviderId;
    this.customerId = init.customerId;
    this.status = init.status;
    this.startTime = init.startTime;
    this.endTime = init.endTime;
    this.amount = init.amount;
    this.currency = init.currency;
    this.paymentId = init.paymentId;
    this.notes = init.notes;
    this.createdAt = init.createdAt;
    this.updatedAt = init.updatedAt;
  }
  /** @param {any} json */
  static fromJSON(json) {
    return new BookingModel({
      bookingId: json?.bookingId ?? json?.id,
      serviceId: json?.serviceId,
      serviceProviderId: json?.serviceProviderId ?? json?.providerId,
      customerId: json?.customerId ?? json?.userId,
      status: toString(json?.status) ?? BookingStatus.PENDING,
      startTime:
        toDate(json?.startTime) ??
        toDate(json?.start) ??
        toString(json?.startTime),
      endTime:
        toDate(json?.endTime) ?? toDate(json?.end) ?? toString(json?.endTime),
      amount: toNumber(json?.amount ?? json?.price),
      currency: toString(json?.currency) ?? "INR",
      paymentId: json?.paymentId,
      notes: toString(json?.notes),
      createdAt: toDate(json?.createdAt),
      updatedAt: toDate(json?.updatedAt),
    });
  }
}

// ========
// Reviews
// ========

/**
 * @typedef {Object} Review
 * @property {number|string=} reviewId
 * @property {number|string=} serviceId
 * @property {number|string=} reviewerId
 * @property {number=} rating
 * @property {string=} comment
 * @property {Date=} createdAt
 */

export class ReviewModel {
  /** @param {Partial<Review>=} init */
  constructor(init = {}) {
    this.reviewId = init.reviewId;
    this.serviceId = init.serviceId;
    this.reviewerId = init.reviewerId;
    this.rating = init.rating;
    this.comment = init.comment;
    this.createdAt = init.createdAt;
  }
  /** @param {any} json */
  static fromJSON(json) {
    return new ReviewModel({
      reviewId: json?.reviewId ?? json?.id,
      serviceId: json?.serviceId,
      reviewerId: json?.reviewerId ?? json?.customerId ?? json?.userId,
      rating: toNumber(json?.rating),
      comment: toString(json?.comment ?? json?.text),
      createdAt: toDate(json?.createdAt ?? json?.created_on),
    });
  }
}

// =========
// Payments
// =========

/**
 * @typedef {Object} Payment
 * @property {number|string=} paymentId
 * @property {string=} provider // e.g., RAZORPAY, STRIPE
 * @property {string=} orderId
 * @property {number|string=} bookingId
 * @property {number=} amount
 * @property {string=} currency
 * @property {string=} status // PaymentStatus enum string
 * @property {Date=} createdAt
 * @property {any=} metadata
 */

export class PaymentModel {
  /** @param {Partial<Payment>=} init */
  constructor(init = {}) {
    this.paymentId = init.paymentId;
    this.provider = init.provider;
    this.orderId = init.orderId;
    this.bookingId = init.bookingId;
    this.amount = init.amount;
    this.currency = init.currency;
    this.status = init.status;
    this.createdAt = init.createdAt;
    this.metadata = init.metadata;
  }
  /** @param {any} json */
  static fromJSON(json) {
    return new PaymentModel({
      paymentId: json?.paymentId ?? json?.id,
      provider: toString(json?.provider),
      orderId: toString(json?.orderId),
      bookingId: json?.bookingId,
      amount: toNumber(json?.amount),
      currency: toString(json?.currency) ?? "INR",
      status: toString(json?.status) ?? PaymentStatus.PENDING,
      createdAt: toDate(json?.createdAt),
      metadata: json?.metadata,
    });
  }
}

// =====================
// Request/Response DTOs
// =====================

// ---- Response DTO typedefs (documentation) ----

/**
 * @template T
 * @typedef {Object} PagedResponse
 * @property {T[]} content
 * @property {number} page
 * @property {number} size
 * @property {number} totalElements
 * @property {number} totalPages
 * @property {boolean=} first
 * @property {boolean=} last
 * @property {number=} numberOfElements
 * @property {boolean=} empty
 */

/** @typedef {Service} ServiceResponse */
/** @typedef {User} UserResponse */
/** @typedef {Booking} BookingResponse */
/** @typedef {Review} ReviewResponse */
/** @typedef {Payment} PaymentResponse */
/** @typedef {AvailabilityRule} AvailabilityRuleResponse */
/** @typedef {AvailabilityException} AvailabilityExceptionResponse */
/** @typedef {AvailableSlot} SlotResponse */

// ---- Request DTO typedefs (documentation) ----

/**
 * @typedef {Object} LoginRequest
 * @property {string} emailOrUsername
 * @property {string} password
 */

/**
 * @typedef {Object} RegisterRequest
 * @property {string} name
 * @property {string} email
 * @property {string} password
 * @property {string=} phone
 * @property {"CUSTOMER"|"PROVIDER"|"ADMIN"=} role
 */

/**
 * @typedef {Object} CreateServiceRequest
 * @property {string} serviceName
 * @property {string} serviceDescription
 * @property {string} serviceCategory
 * @property {number} servicePricePerHour
 * @property {number} latitude
 * @property {number} longitude
 */

/**
 * @typedef {Object} UpdateServiceRequest
 * @property {string=} serviceName
 * @property {string=} serviceDescription
 * @property {string=} serviceCategory
 * @property {number=} servicePricePerHour
 * @property {number=} latitude
 * @property {number=} longitude
 */

/**
 * @typedef {Object} CreateBookingRequest
 * @property {number|string} serviceId
 * @property {number|string} serviceProviderId
 * @property {number|string} customerId
 * @property {string} startTime // ISO8601
 * @property {string} endTime   // ISO8601
 * @property {string=} notes
 */

/**
 * @typedef {Object} UpdateBookingStatusRequest
 * @property {keyof typeof BookingStatus} status
 */

/**
 * @typedef {Object} RescheduleBookingRequest
 * @property {string} startTime // ISO8601
 * @property {string} endTime   // ISO8601
 */

/**
 * @typedef {Object} CreateReviewRequest
 * @property {number|string} serviceId
 * @property {number} rating // 1..5
 * @property {string=} comment
 */

/**
 * @typedef {Object} UpdateReviewRequest
 * @property {number=} rating
 * @property {string=} comment
 */

/**
 * @typedef {Object} CreatePaymentOrderRequest
 * @property {number} amount
 * @property {string=} currency // default INR
 */

/**
 * @typedef {Object} ProcessPaymentRequest
 * @property {string} orderId
 * @property {string} paymentId
 * @property {string=} signature
 * @property {string=} provider
 */

/**
 * @typedef {Object} AvailabilityRuleRequest
 * @property {number|string} serviceProviderId
 * @property {number|string=} serviceId
 * @property {string} dayOfWeek
 * @property {string} startTime
 * @property {string} endTime
 * @property {number} slotDurationMinutes
 * @property {string=} timeZone
 */

/**
 * @typedef {Object} AvailabilityExceptionRequest
 * @property {number|string} serviceProviderId
 * @property {number|string=} serviceId
 * @property {string} date
 * @property {string} startTime
 * @property {string} endTime
 * @property {boolean} available
 */

// ---- Tiny builders to construct request payloads consistently ----

/** @param {Partial<LoginRequest>} r */
export const buildLoginRequest = (r = {}) => ({
  emailOrUsername: String(r.emailOrUsername || ""),
  password: String(r.password || ""),
});

/** @param {Partial<RegisterRequest>} r */
export const buildRegisterRequest = (r = {}) => ({
  name: String(r.name || ""),
  email: String(r.email || ""),
  password: String(r.password || ""),
  phone: r.phone != null ? String(r.phone) : undefined,
  role: r.role || UserRole.CUSTOMER,
});

/** @param {Partial<CreateServiceRequest>|Service} s */
export const buildCreateServiceRequest = (s = {}) => ({
  serviceName: String(s.serviceName || s.name || ""),
  serviceDescription: String(s.serviceDescription || s.description || ""),
  serviceCategory: String(s.serviceCategory || s.category || ""),
  servicePricePerHour: Number(
    s.servicePricePerHour ?? s.pricePerHour ?? s.price ?? 0
  ),
  latitude: Number(s.latitude ?? 0),
  longitude: Number(s.longitude ?? 0),
});

/** @param {Partial<UpdateServiceRequest>|Service} s */
export const buildUpdateServiceRequest = (s = {}) => ({
  serviceName: s.serviceName != null ? String(s.serviceName) : undefined,
  serviceDescription:
    s.serviceDescription != null ? String(s.serviceDescription) : undefined,
  serviceCategory:
    s.serviceCategory != null ? String(s.serviceCategory) : undefined,
  servicePricePerHour:
    s.servicePricePerHour != null ? Number(s.servicePricePerHour) : undefined,
  latitude: s.latitude != null ? Number(s.latitude) : undefined,
  longitude: s.longitude != null ? Number(s.longitude) : undefined,
});

/** @param {Partial<CreateBookingRequest>} b */
export const buildCreateBookingRequest = (b = {}) => ({
  serviceId: b.serviceId,
  serviceProviderId: b.serviceProviderId,
  customerId: b.customerId,
  startTime: String(b.startTime || ""),
  endTime: String(b.endTime || ""),
  notes: b.notes != null ? String(b.notes) : undefined,
});

/** @param {Partial<UpdateBookingStatusRequest>} b */
export const buildUpdateBookingStatusRequest = (b = {}) => ({
  status: b.status || BookingStatus.PENDING,
});

/** @param {Partial<RescheduleBookingRequest>} r */
export const buildRescheduleBookingRequest = (r = {}) => ({
  startTime: String(r.startTime || ""),
  endTime: String(r.endTime || ""),
});

/** @param {Partial<CreateReviewRequest>} r */
export const buildCreateReviewRequest = (r = {}) => ({
  serviceId: r.serviceId,
  rating: Number(r.rating ?? 0),
  comment: r.comment != null ? String(r.comment) : undefined,
});

/** @param {Partial<UpdateReviewRequest>} r */
export const buildUpdateReviewRequest = (r = {}) => ({
  rating: r.rating != null ? Number(r.rating) : undefined,
  comment: r.comment != null ? String(r.comment) : undefined,
});

/** @param {Partial<CreatePaymentOrderRequest>} p */
export const buildCreatePaymentOrderRequest = (p = {}) => ({
  amount: Number(p.amount ?? 0),
  currency: p.currency || "INR",
});

/** @param {Partial<ProcessPaymentRequest>} p */
export const buildProcessPaymentRequest = (p = {}) => ({
  orderId: String(p.orderId || ""),
  paymentId: String(p.paymentId || ""),
  signature: p.signature != null ? String(p.signature) : undefined,
  provider: p.provider != null ? String(p.provider) : undefined,
});

/** @param {Partial<AvailabilityRuleRequest>} r */
export const buildAvailabilityRuleRequest = (r = {}) => ({
  serviceProviderId: r.serviceProviderId,
  serviceId: r.serviceId,
  dayOfWeek: String(r.dayOfWeek || ""),
  startTime: String(r.startTime || ""),
  endTime: String(r.endTime || ""),
  slotDurationMinutes: Number(r.slotDurationMinutes ?? 30),
  timeZone: r.timeZone != null ? String(r.timeZone) : undefined,
});

/** @param {Partial<AvailabilityExceptionRequest>} r */
export const buildAvailabilityExceptionRequest = (r = {}) => ({
  serviceProviderId: r.serviceProviderId,
  serviceId: r.serviceId,
  date: String(r.date || ""),
  startTime: String(r.startTime || ""),
  endTime: String(r.endTime || ""),
  available: Boolean(r.available ?? false),
});

// ======================
// Convenience re-exports
// ======================

export default {
  // enums
  UserRole,
  BookingStatus,
  PaymentStatus,
  DayOfWeek,
  // models
  UserModel,
  ServiceModel,
  ReviewAggregateModel,
  AvailabilityRuleModel,
  AvailabilityExceptionModel,
  SlotModel,
  BookingModel,
  ReviewModel,
  PaymentModel,
  // helpers
  mapPagedResponse,
  // request builders
  buildLoginRequest,
  buildRegisterRequest,
  buildCreateServiceRequest,
  buildUpdateServiceRequest,
  buildCreateBookingRequest,
  buildUpdateBookingStatusRequest,
  buildRescheduleBookingRequest,
  buildCreateReviewRequest,
  buildUpdateReviewRequest,
  buildCreatePaymentOrderRequest,
  buildProcessPaymentRequest,
  buildAvailabilityRuleRequest,
  buildAvailabilityExceptionRequest,
};
