package com.sarthak.BookingService.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
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

    @BeforeEach
    void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private BookingDto buildDto(Long id) {
        BookingDto dto = new BookingDto();
        dto.setBookingId(id);
        dto.setCustomerId(11L);
        dto.setServiceId(22L);
        dto.setServiceProviderId(33L);
        dto.setServiceCategory("Plumbing");
        dto.setBookingStatus(BookingStatus.PENDING);
        dto.setBookingTime("Monday, January 1, 2024 at 1:00 PM");
        return dto;
    }

    @Test
    @DisplayName("GET /api/bookings/{id} 200 when found")
    void getBookingDetails_found() throws Exception {
        when(bookingService.getBookingDetails(1L)).thenReturn(buildDto(1L));
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(1)))
                .andExpect(jsonPath("$.serviceCategory", is("Plumbing")))
                .andExpect(jsonPath("$.bookingStatus", is("PENDING")));
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
    @DisplayName("GET /api/bookings returns list")
    void getAllBookings() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(List.of(buildDto(1L), buildDto(2L)));
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].bookingId", is(1)));
    }

    @Test
    @DisplayName("POST /api/bookings returns 201")
    void bookService() throws Exception {
        when(bookingService.bookService()).thenReturn("Under Development");
        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Under Development"));
    }
}
