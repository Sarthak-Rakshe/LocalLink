import api from "./api";

export const bookingService = {
  async getBookingById(id) {
    const res = await api.get(`/api/bookings/${id}`);
    return res.data;
  },

  async getBookingsByCustomer(
    customerId,
    page = 0,
    size = 10,
    sortBy = "bookingDate",
    sortDir = "desc"
  ) {
    const res = await api.get(`/api/bookings/customer/${customerId}`, {
      params: { page, size, "sort-by": sortBy, "sort-dir": sortDir },
    });
    return res.data; // { content, pageNumber, totalElements, totalPages, pageSize }
  },

  async getBookingsByProvider(
    serviceProviderId,
    page = 0,
    size = 10,
    sortBy = "bookingDate",
    sortDir = "asc"
  ) {
    const res = await api.get(
      `/api/bookings/serviceProvider/${serviceProviderId}`,
      {
        params: { page, size, "sort-by": sortBy, "sort-dir": sortDir },
      }
    );
    return res.data; // { content, pageNumber, totalElements, totalPages, pageSize }
  },

  async createBooking({
    customerId,
    serviceId,
    serviceProviderId,
    serviceCategory,
    bookingDate,
    bookingStartTime,
    bookingEndTime,
  }) {
    const payload = {
      customerId,
      serviceId,
      serviceProviderId,
      serviceCategory,
      bookingDate,
      bookingStartTime,
      bookingEndTime,
    };
    const res = await api.post(`/api/bookings`, payload);
    return res.data; // BookingDto
  },

  async rescheduleBooking(
    bookingId,
    { newBookingDate, newBookingStartTime, newBookingEndTime }
  ) {
    const res = await api.post(`/api/bookings/${bookingId}/reschedule`, {
      bookingId,
      newBookingDate,
      newBookingStartTime,
      newBookingEndTime,
    });
    return res.data;
  },

  async updateBookingStatus(bookingId, status) {
    const res = await api.post(
      `/api/bookings/${bookingId}/updateStatus/${status}`
    );
    return res.data;
  },

  async cancelBooking(bookingId) {
    await api.delete(`/api/bookings/${bookingId}`);
  },

  async getBookedSlots(serviceProviderId, serviceId, date) {
    const res = await api.get(
      `/api/bookings/bookedSlots/${serviceProviderId}/${serviceId}`,
      { params: { date } }
    );
    return res.data; // { bookedSlots: [{startTime,endTime}], date }
  },
};
