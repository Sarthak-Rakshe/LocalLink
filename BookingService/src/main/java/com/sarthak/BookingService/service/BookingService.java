package com.sarthak.BookingService.service;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.exception.BookingNotFoundException;
import com.sarthak.BookingService.mapper.BookingMapper;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
    }

    public String bookService(){

        return "Under Development";
    }

    public BookingDto getBookingDetails(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        return bookingMapper.toDto(booking);
    }

    public List<BookingDto> getAllBookings(){
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(bookingMapper::toDto).toList();
    }

    public List<BookingDto> getAllBookingsByCustomerId(Long customerId){
        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
        return bookings.stream().map(bookingMapper::toDto).toList();
    }

    //WILL GENERALLY BE USED BY OTHER SERVICES
    public List<BookingDto> getAllBookingsByServiceProviderId(Long serviceProviderId, LocalDate date){
        List<Booking> bookings = bookingRepository.findAllByServiceProviderIdAndDate(serviceProviderId,
                        date);
        return bookings.stream().map(bookingMapper::toDto).toList();
    }
}
