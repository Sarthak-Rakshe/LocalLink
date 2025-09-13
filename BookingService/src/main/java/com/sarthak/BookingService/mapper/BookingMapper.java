package com.sarthak.BookingService.mapper;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.model.Booking;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking){
        BookingDto bookingDto = new BookingDto();
        bookingDto.setBookingId(booking.getBookingId());
        bookingDto.setCustomerId(booking.getCustomerId());
        bookingDto.setServiceId(booking.getServiceId());
        bookingDto.setServiceProviderId(booking.getServiceProviderId());
        bookingDto.setServiceCategory(booking.getServiceCategory());
        bookingDto.setBookingDate(booking.getBookingDate().toString());
        bookingDto.setBookingStartTime(booking.getBookingStartTime().toString());
        bookingDto.setBookingEndTime(booking.getBookingEndTime().toString());
        bookingDto.setBookingStatus(booking.getBookingStatus());

        return bookingDto;
    }

    public Booking toEntity(BookingDto bookingDto){
        Booking booking = new Booking();
        booking.setCustomerId(bookingDto.getCustomerId());
        booking.setServiceId(bookingDto.getServiceId());
        booking.setServiceProviderId(bookingDto.getServiceProviderId());
        booking.setServiceCategory(bookingDto.getServiceCategory());

        // Parse date and time components from DTO and convert to proper types
        if (bookingDto.getBookingDate() != null) {
            LocalDate date = LocalDate.parse(bookingDto.getBookingDate());
            booking.setBookingDate(date);

            if (bookingDto.getBookingStartTime() != null) {
                LocalTime start = LocalTime.parse(bookingDto.getBookingStartTime());
                Instant startInstant = date.atTime(start).atZone(ZoneOffset.UTC).toInstant();
                booking.setBookingStartTime(startInstant);
            }
            if (bookingDto.getBookingEndTime() != null) {
                LocalTime end = LocalTime.parse(bookingDto.getBookingEndTime());
                Instant endInstant = date.atTime(end).atZone(ZoneOffset.UTC).toInstant();
                booking.setBookingEndTime(endInstant);
            }
        }

        booking.setBookingStatus(bookingDto.getBookingStatus());

        return booking;
    }
}
