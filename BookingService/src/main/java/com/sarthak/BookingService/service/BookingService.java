package com.sarthak.BookingService.service;

import com.sarthak.BookingService.client.AvailabilityServiceClient;
import com.sarthak.BookingService.dto.AvailabilityStatus;
import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.dto.BookingStatusCount;
import com.sarthak.BookingService.dto.request.BookingRescheduleRequest;
import com.sarthak.BookingService.dto.response.BookingsSummaryResponse;
import com.sarthak.BookingService.exception.BookingNotFoundException;
import com.sarthak.BookingService.exception.ProviderNotAvailableForGivenTimeSlotException;
import com.sarthak.BookingService.exception.TimeSlotAlreadyBookedException;
import com.sarthak.BookingService.exception.UnknownAvailabilityStatusException;
import com.sarthak.BookingService.mapper.BookingMapper;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import com.sarthak.BookingService.repository.BookingRepository;
import com.sarthak.BookingService.dto.request.AvailabilityStatusRequest;
import com.sarthak.BookingService.dto.response.AvailabilityStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.sarthak.BookingService.model.BookingStatus.*;


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


    public List<BookingDto> getAllByServiceProviderIdAndDate(Long serviceProviderId, LocalDate date){
        List<Booking> bookings = bookingRepository.findAllByServiceProviderIdAndBookingDate(serviceProviderId,
                        date);
        return bookingMapper.toDtoList(bookings);
    }

    public Page<BookingDto> getAllByCustomerId(Long customerId, int page, int size){
        Page<Booking> bookings = bookingRepository.findByCustomerId(customerId, PageRequest.of(page, size));
        return bookings.map(bookingMapper::toDto);
    }

    public Page<BookingDto> getAllBookings(int page, int size){
        Page<Booking> bookings = bookingRepository.findAll(PageRequest.of(page, size));
        return bookings.map(bookingMapper::toDto);
    }

    public Page<BookingDto> getAllByServiceProviderId(Long serviceProviderId, int page, int size){
        Page<Booking> bookings = bookingRepository.findByServiceProviderId(serviceProviderId, PageRequest.of(page, size));
        return bookings.map(bookingMapper::toDto);
    }

    @Transactional
    public BookingDto bookService(BookingDto bookingDto){
        Booking booking = bookingMapper.toEntity(bookingDto);

        AvailabilityStatusResponse response = availabilityServiceClient.getAvailabilityStatus(
                AvailabilityStatusRequest.builder()
                        .serviceProviderId(booking.getServiceProviderId())
                        .serviceId(booking.getServiceId())
                        .startTime(booking.getBookingStartTime())
                        .endTime(booking.getBookingEndTime())
                        .date(booking.getBookingDate())
                        .build()
        );

        if(response.status().equals(AvailabilityStatus.BLOCKED) ||
           response.status().equals(AvailabilityStatus.OUTSIDE_WORKING_HOURS)){
            throw new ProviderNotAvailableForGivenTimeSlotException("Time slot not available");
        }else if (response.status().equals(AvailabilityStatus.AVAILABLE)) {
            Optional<Booking> existingBooking =
                    bookingRepository.findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(
                    booking.getServiceProviderId(), booking.getServiceId(),booking.getBookingDate(), booking.getBookingStartTime(), booking.getBookingEndTime());
            if(existingBooking.isPresent()){
                throw new TimeSlotAlreadyBookedException("Time slot already booked");
            }
        }else {
            throw new UnknownAvailabilityStatusException("Unknown availability status");
        }
        booking.setBookingStatus(PENDING);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);

    }

    //After payment is successful
    @Transactional
    public BookingDto confirmBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(updatedBooking);
    }

    //For cancelling a booking
    @Transactional
    public BookingDto cancelBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(updatedBooking);
    }

    //After service is delivered
    @Transactional
    public BookingDto completeBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(updatedBooking);
    }

    //For deleting a booking record (soft delete)
    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(BookingStatus.DELETED);
        bookingRepository.save(booking);
    }

    public BookingsSummaryResponse getBookingSummaryForServiceProvider(Long serviceProviderId){
        List<BookingStatusCount> rows =
                bookingRepository.countBookingsByStatusGrouped(serviceProviderId);

        long completed = 0, pending = 0, cancelled = 0, deleted = 0, rescheduled = 0;
        for (BookingStatusCount r : rows) {
            switch (r.status()) {
                case COMPLETED -> completed = r.count();
                case PENDING   -> pending   = r.count();
                case CANCELLED -> cancelled = r.count();
                case DELETED   -> deleted   = r.count();
                case RESCHEDULED -> rescheduled = r.count();
                default -> {}
            }
        }
        long total = completed + pending + cancelled ;

        return BookingsSummaryResponse.builder()
                .totalBookings(total)
                .completedBookings(completed)
                .pendingBookings(pending)
                .cancelledBookings(cancelled)
                .deletedBookings(deleted)
                .rescheduledBookings(rescheduled)
                .build();
    }

    @Transactional
    public BookingDto rescheduleBooking(Long bookingId, BookingRescheduleRequest request){
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));

        LocalDate changedDate = request.newBookingDate() != null ? request.newBookingDate() : existingBooking.getBookingDate();
        LocalTime changedStartTime = request.newBookingStartTime() != null ? request.newBookingStartTime() : existingBooking.getBookingStartTime();
        LocalTime changedEndTime = request.newBookingEndTime() != null ? request.newBookingEndTime() : existingBooking.getBookingEndTime();

        AvailabilityStatusResponse response = availabilityServiceClient.getAvailabilityStatus(
                AvailabilityStatusRequest.builder()
                        .serviceProviderId(existingBooking.getServiceProviderId())
                        .serviceId(existingBooking.getServiceId())
                        .startTime(changedStartTime)
                        .endTime(changedEndTime)
                        .date(changedDate)
                        .build()
        );

        if (response.status().equals(AvailabilityStatus.BLOCKED) ||
                response.status().equals(AvailabilityStatus.OUTSIDE_WORKING_HOURS)){
            throw new TimeSlotAlreadyBookedException("Time slot not available");
        }else if (response.status().equals(AvailabilityStatus.AVAILABLE)) {
            Optional<Booking> conflictingBooking =
                    bookingRepository.findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(
                            existingBooking.getServiceProviderId(), existingBooking.getServiceId(),
                            changedDate, changedStartTime, changedEndTime);
            if(conflictingBooking.isPresent() && !conflictingBooking.get().getBookingId().equals(bookingId)){
                throw new TimeSlotAlreadyBookedException("Time slot already booked");
            }
        }else {
            throw new UnknownAvailabilityStatusException("Unknown availability status");
        }

        existingBooking.setBookingStatus(RESCHEDULED);

        Booking newBooking = Booking.builder()
                .serviceProviderId(existingBooking.getServiceProviderId())
                .serviceId(existingBooking.getServiceId())
                .customerId(existingBooking.getCustomerId())
                .bookingDate(changedDate)
                .bookingStartTime(changedStartTime)
                .bookingEndTime(changedEndTime)
                .bookingStatus(PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(newBooking);
        existingBooking.setRescheduledToId(String.valueOf(savedBooking.getBookingId()));
        bookingRepository.save(existingBooking);

        return bookingMapper.toDto(savedBooking);
    }



}
