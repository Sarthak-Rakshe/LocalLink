package com.sarthak.BookingService.mapper;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.model.Booking;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking){

        return BookingDto.builder()
                .bookingId(booking.getBookingId())
                .serviceProviderId(booking.getServiceProviderId())
                .serviceId(booking.getServiceId())
                .customerId(booking.getCustomerId())
                .serviceCategory(booking.getServiceCategory())
                .bookingDate(booking.getBookingDate().toString())
                .bookingStartTime(booking.getBookingStartTime().toString())
                .bookingEndTime(booking.getBookingEndTime().toString())
                .bookingStatus(booking.getBookingStatus())
                .createdAt(booking.getCreatedAt().toString())
                .rescheduledToId(booking.getRescheduledToId())
                .build();
    }

    public Booking toEntity(BookingDto bookingDto){
        return Booking.builder()
                .bookingId(bookingDto.bookingId())
                .serviceProviderId(bookingDto.serviceProviderId())
                .serviceId(bookingDto.serviceId())
                .customerId(bookingDto.customerId())
                .serviceCategory(bookingDto.serviceCategory())
                .bookingDate(LocalDate.parse(bookingDto.bookingDate()))
                .bookingStartTime(LocalTime.parse(bookingDto.bookingStartTime()))
                .bookingEndTime(LocalTime.parse(bookingDto.bookingEndTime()))
                .bookingStatus(bookingDto.bookingStatus())
                .build();
    }

    public List<BookingDto> toDtoList(List<Booking> bookings){
        return bookings.stream().map(this::toDto).toList();
    }
}
