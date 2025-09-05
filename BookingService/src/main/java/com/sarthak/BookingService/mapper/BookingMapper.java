package com.sarthak.BookingService.mapper;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.model.Booking;
import org.springframework.stereotype.Component;

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
        booking.setBookingDate(bookingDto.getBookingDate());
        booking.setBookingStartTime(bookingDto.getBookingStartTime());
        booking.setBookingEndTime(bookingDto.getBookingEndTime());
        booking.setBookingStatus(bookingDto.getBookingStatus());

        return booking;
    }
}
