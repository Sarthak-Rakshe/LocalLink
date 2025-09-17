package com.sarthak.BookingService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.exception.BookingNotFoundException;
import com.sarthak.BookingService.exception.GlobalExceptionHandler;
import com.sarthak.BookingService.model.BookingStatus;
import com.sarthak.BookingService.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private BookingDto buildDto(Long id) {
        return BookingDto.builder()
                .bookingId(id)
                .customerId(11L)
                .serviceId(22L)
                .serviceProviderId(33L)
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingDate("2024-01-01")
                .bookingStartTime("13:00")
                .bookingEndTime("14:00")
                .createdAt("2024-01-01T00:00:00Z")
                .rescheduledToId("N/A")
                .build();
    }

    @Test
    @DisplayName("GET /api/bookings/{id} 200 when found")
    void getBookingDetails_found() throws Exception {
        when(bookingService.getBookingDetails(1L)).thenReturn(buildDto(1L));
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(1)))
                .andExpect(jsonPath("$.bookingStatus", is("CONFIRMED")))
                .andExpect(jsonPath("$.bookingDate", is("2024-01-01")));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} 404 when not found")
    void getBookingDetails_notFound() throws Exception {
        when(bookingService.getBookingDetails(99L)).thenThrow(new BookingNotFoundException("Booking not found"));
        mockMvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is("404")))
                .andExpect(jsonPath("$.message", is("Booking not found")));
    }

    @Test
    @DisplayName("GET /api/bookings returns paged list")
    void getAllBookings() throws Exception {
        Page<BookingDto> page = new PageImpl<>(List.of(buildDto(1L), buildDto(2L)));
        when(bookingService.getAllBookings(0, 10)).thenReturn(page);
        mockMvc.perform(get("/api/bookings?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].bookingId", is(1)));
    }

    @Test
    @DisplayName("GET /api/bookings/service-provider/{id}?date=YYYY-MM-DD returns list")
    void getBookingsByServiceProviderId_andDate() throws Exception {
        when(bookingService.getAllByServiceProviderIdAndDate(33L, LocalDate.parse("2024-01-01")))
                .thenReturn(List.of(buildDto(10L), buildDto(11L)));

        mockMvc.perform(get("/api/bookings/service-provider/33")
                        .param("date", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].bookingId", is(10)));
    }

    @Test
    @DisplayName("GET /api/bookings/customer/{id}?page&size returns paged list")
    void getBookingsByCustomerId_paged() throws Exception {
        Page<BookingDto> page = new PageImpl<>(List.of(buildDto(21L)));
        when(bookingService.getAllByCustomerId(11L, 0, 1)).thenReturn(page);

        mockMvc.perform(get("/api/bookings/customer/11")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(1)));
    }

    @Test
    @DisplayName("GET /api/bookings/summary/{serviceProviderId} returns summary")
    void getBookingSummaryForProvider() throws Exception {
        var response = com.sarthak.BookingService.dto.response.BookingsSummaryResponse.builder()
                .requesterId(33L)
                .totalBookings(5L)
                .completedBookings(2L)
                .pendingBookings(2L)
                .cancelledBookings(1L)
                .deletedBookings(0L)
                .rescheduledBookings(0L)
                .build();
        when(bookingService.getBookingSummaryForServiceProvider(33L)).thenReturn(response);

        mockMvc.perform(get("/api/bookings/summary/33"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requesterId", is(33)))
                .andExpect(jsonPath("$.totalBookings", is(5)))
                .andExpect(jsonPath("$.completedBookings", is(2)))
                .andExpect(jsonPath("$.pendingBookings", is(2)))
                .andExpect(jsonPath("$.cancelledBookings", is(1)));
    }

    @Test
    @DisplayName("POST /api/bookings returns 201 with created dto")
    void bookService_created() throws Exception {
        BookingDto request = BookingDto.builder()
                .customerId(11L)
                .serviceId(22L)
                .serviceProviderId(33L)
                .bookingDate("2024-01-01")
                .bookingStartTime("13:00")
                .bookingEndTime("14:00")
                .bookingStatus(BookingStatus.PENDING)
                .build();

        BookingDto response = buildDto(123L);
        when(bookingService.bookService(any(BookingDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId", is(123)))
                .andExpect(jsonPath("$.bookingStatus", is("CONFIRMED")));
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/confirm returns CONFIRMED")
    void confirmBooking() throws Exception {
        when(bookingService.confirmBooking(1L)).thenReturn(buildDto(1L));
        mockMvc.perform(post("/api/bookings/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(1)))
                .andExpect(jsonPath("$.bookingStatus", is("CONFIRMED")));
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/cancel returns CANCELLED")
    void cancelBooking() throws Exception {
        BookingDto cancelled = BookingDto.builder()
                .bookingId(2L)
                .customerId(11L).serviceId(22L).serviceProviderId(33L)
                .bookingStatus(BookingStatus.CANCELLED)
                .bookingDate("2024-01-01").bookingStartTime("13:00").bookingEndTime("14:00")
                .createdAt("2024-01-01T00:00:00Z").rescheduledToId("N/A")
                .build();
        when(bookingService.cancelBooking(2L)).thenReturn(cancelled);
        mockMvc.perform(post("/api/bookings/2/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(2)))
                .andExpect(jsonPath("$.bookingStatus", is("CANCELLED")));
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/complete returns COMPLETED")
    void completeBooking() throws Exception {
        BookingDto completed = BookingDto.builder()
                .bookingId(3L)
                .customerId(11L).serviceId(22L).serviceProviderId(33L)
                .bookingStatus(BookingStatus.COMPLETED)
                .bookingDate("2024-01-01").bookingStartTime("13:00").bookingEndTime("14:00")
                .createdAt("2024-01-01T00:00:00Z").rescheduledToId("N/A")
                .build();
        when(bookingService.completeBooking(3L)).thenReturn(completed);
        mockMvc.perform(post("/api/bookings/3/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(3)))
                .andExpect(jsonPath("$.bookingStatus", is("COMPLETED")));
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/reschedule returns new booking dto")
    void rescheduleBooking() throws Exception {
        BookingDto newBooking = BookingDto.builder()
                .bookingId(200L)
                .customerId(11L).serviceId(22L).serviceProviderId(33L)
                .bookingStatus(BookingStatus.PENDING)
                .bookingDate("2024-01-02").bookingStartTime("15:00").bookingEndTime("16:00")
                .createdAt("2024-01-01T00:00:00Z").rescheduledToId("N/A")
                .build();
        when(bookingService.rescheduleBooking(any(Long.class), any())).thenReturn(newBooking);

        String body = "{"+
                "\"bookingId\": 1,"+
                "\"newBookingDate\": \"2024-01-02\","+
                "\"newBookingStartTime\": \"15:00\","+
                "\"newBookingEndTime\": \"16:00\""+
                "}";

        mockMvc.perform(post("/api/bookings/1/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(200)))
                .andExpect(jsonPath("$.bookingStatus", is("PENDING")))
                .andExpect(jsonPath("$.bookingDate", is("2024-01-02")));
    }

    @Test
    @DisplayName("DELETE /api/bookings/{id} returns 204")
    void deleteBooking() throws Exception {
        mockMvc.perform(delete("/api/bookings/4"))
                .andExpect(status().isNoContent());
    }
}
