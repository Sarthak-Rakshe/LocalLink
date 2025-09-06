package com.sarthak.BookingService.service;

import com.sarthak.BookingService.client.AvailabilityServiceClient;
import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.exception.BookingNotFoundException;
import com.sarthak.BookingService.exception.ProviderNotAvailableForGivenTimeSlotException;
import com.sarthak.BookingService.exception.TimeSlotAlreadyBookedException;
import com.sarthak.BookingService.exception.UnknownAvailabilityStatusException;
import com.sarthak.BookingService.mapper.BookingMapper;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import com.sarthak.BookingService.repository.BookingRepository;
import com.sarthak.BookingService.request.AvailabilityStatusRequest;
import com.sarthak.BookingService.response.AvailabilityStatusResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final AvailabilityServiceClient availabilityServiceClient;

    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper, AvailabilityServiceClient availabilityServiceClient) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.availabilityServiceClient = availabilityServiceClient;
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

    public List<BookingDto> getAllByServiceProviderIdAndDate(Long serviceProviderId, LocalDate date){
        List<Booking> bookings = bookingRepository.findAllByServiceProviderIdAndDate(serviceProviderId,
                        date);
        return bookings.stream().map(bookingMapper::toDto).toList();
    }

    @Transactional
    public BookingDto bookService(BookingDto bookingDto){
        Booking booking = bookingMapper.toEntity(bookingDto);
        AvailabilityStatusResponse response = availabilityServiceClient.getAvailabilityStatus(
                new AvailabilityStatusRequest(
                        bookingDto.getServiceProviderId(),
                        LocalTime.parse(bookingDto.getBookingStartTime()),
                        LocalTime.parse(bookingDto.getBookingEndTime()),
                        LocalDate.parse(bookingDto.getBookingDate())
                )
        );

        if(response.getStatus().equals("BLOCKED") ||
           response.getStatus().equals("OUTSIDE_WORKING_HOURS")){
            throw new ProviderNotAvailableForGivenTimeSlotException("Time slot not available");
        }else if (response.getStatus().equals("AVAILABLE")) {
            List<Booking> existingBookings = bookingRepository.findAllByServiceProviderIdAndDate(
                    booking.getServiceProviderId(), booking.getBookingDate());
            for (Booking existingBooking : existingBookings) {
                if (booking.getBookingStartTime().isBefore(existingBooking.getBookingEndTime()) &&
                        booking.getBookingEndTime().isAfter(existingBooking.getBookingStartTime())) {
                    throw new TimeSlotAlreadyBookedException("Time slot already booked");
                }
            }
        }else {
            throw new UnknownAvailabilityStatusException("Unknown availability status");
        }
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);

    }

    public BookingDto cancelBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(updatedBooking);
    }

    public BookingDto completeBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(BookingStatus.COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(updatedBooking);
    }


}
