package com.sarthak.BookingService.service;

import com.sarthak.BookingService.client.AvailabilityServiceClient;
import com.sarthak.BookingService.dto.AvailabilityStatus;
import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.dto.BookingStatusCount;
import com.sarthak.BookingService.dto.request.AvailabilityRequest;
import com.sarthak.BookingService.dto.request.BookingRescheduleRequest;
import com.sarthak.BookingService.dto.response.AvailabilityResponse;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("Fetched booking details for bookingId: {}", bookingId);
        return bookingMapper.toDto(booking);
    }


    public List<BookingDto> getAllByServiceProviderIdAndDate(Long serviceProviderId, LocalDate date){
        List<Booking> bookings = bookingRepository.findAllByServiceProviderIdAndBookingDate(serviceProviderId,
                        date);
        log.info("Fetched {} bookings for serviceProviderId: {} on date: {}", bookings.size(), serviceProviderId, date);
        return bookingMapper.toDtoList(bookings);
    }

    public Page<BookingDto> getAllByCustomerId(Long customerId, int page, int size){
        Page<Booking> bookings = bookingRepository.findByCustomerId(customerId, PageRequest.of(page, size));
        log.info("Fetched {} bookings for customerId: {}", bookings.getTotalElements(), customerId);
        return bookings.map(bookingMapper::toDto);
    }

    public Page<BookingDto> getAllBookings(int page, int size){
        Page<Booking> bookings = bookingRepository.findAll(PageRequest.of(page, size));
        log.info("Fetched {} bookings", bookings.getTotalElements());
        return bookings.map(bookingMapper::toDto);
    }

    public Page<BookingDto> getAllByServiceProviderId(Long serviceProviderId, int page, int size){
        Page<Booking> bookings = bookingRepository.findByServiceProviderId(serviceProviderId, PageRequest.of(page, size));
        log.info("Fetched {} bookings for serviceProviderId: {}", bookings.getTotalElements(), serviceProviderId);
        return bookings.map(bookingMapper::toDto);
    }

    public AvailabilityResponse getAvailabilitySlots(AvailabilityRequest request){
        LocalTime dayStart  = LocalTime.of(0,0);
        LocalTime dayEnd    = LocalTime.of(23,59);
    }


    @Transactional
    public BookingDto bookService(BookingDto bookingDto){
        Booking booking = bookingMapper.toEntity(bookingDto);
        log.info("Booking process starting for serviceId: {} with providerId: {} on date: {} from {} to {}",
                booking.getServiceId(), booking.getServiceProviderId(), booking.getBookingDate(),
                booking.getBookingStartTime(), booking.getBookingEndTime());

        AvailabilityStatusResponse response = availabilityServiceClient.getAvailabilityStatus(
                AvailabilityStatusRequest.builder()
                        .serviceProviderId(booking.getServiceProviderId())
                        .serviceId(booking.getServiceId())
                        .startTime(booking.getBookingStartTime())
                        .endTime(booking.getBookingEndTime())
                        .date(booking.getBookingDate())
                        .build()
        );

        log.info("Availability status for serviceId: {} with providerId: {} on date: {} from {} to {} is {}",
                booking.getServiceId(), booking.getServiceProviderId(), booking.getBookingDate(),
                booking.getBookingStartTime(), booking.getBookingEndTime(), response.status());

        if(response.status().equals(AvailabilityStatus.BLOCKED) ||
           response.status().equals(AvailabilityStatus.OUTSIDE_WORKING_HOURS)){
            throw new ProviderNotAvailableForGivenTimeSlotException("Time slot not available");
        }else if (response.status().equals(AvailabilityStatus.AVAILABLE)) {
            Optional<Booking> existingBooking =
                    bookingRepository.findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(
                    booking.getServiceProviderId(), booking.getServiceId(),booking.getBookingDate(), booking.getBookingStartTime(), booking.getBookingEndTime());

            log.info("Checking for existing bookings for serviceId: {} with providerId: {} on date: {} from {} to {}",
                    booking.getServiceId(), booking.getServiceProviderId(), booking.getBookingDate(),
                    booking.getBookingStartTime(), booking.getBookingEndTime());

            if(existingBooking.isPresent()){
                log.error("Time slot already booked for serviceId: {} with providerId: {} on date: {} from {} to {}",
                        booking.getServiceId(), booking.getServiceProviderId(), booking.getBookingDate(),
                        booking.getBookingStartTime(), booking.getBookingEndTime());
                throw new TimeSlotAlreadyBookedException("Time slot already booked");
            }
        }else {
            log.error("Unknown availability status received: {} for serviceId: {} with providerId: {} on date: {} from {} to {}",
                    response.status(), booking.getServiceId(), booking.getServiceProviderId(), booking.getBookingDate(),
                    booking.getBookingStartTime(), booking.getBookingEndTime());
            throw new UnknownAvailabilityStatusException("Unknown availability status");
        }
        booking.setBookingStatus(PENDING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with bookingId: {} for serviceId: {} with providerId: {} on date: {} from {} to {}",
                savedBooking.getBookingId(), savedBooking.getServiceId(), savedBooking.getServiceProviderId(),
                savedBooking.getBookingDate(), savedBooking.getBookingStartTime(), savedBooking.getBookingEndTime());

        return bookingMapper.toDto(savedBooking);

    }

    //After payment is successful
    @Transactional
    public BookingDto confirmBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed with bookingId: {}", bookingId);
        return bookingMapper.toDto(updatedBooking);
    }

    //For cancelling a booking
    @Transactional
    public BookingDto cancelBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking cancelled with bookingId: {}", bookingId);
        return bookingMapper.toDto(updatedBooking);
    }

    //After service is delivered
    @Transactional
    public BookingDto completeBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking completed with bookingId: {}", bookingId);
        return bookingMapper.toDto(updatedBooking);
    }

    //For deleting a booking record (soft delete)
    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(BookingStatus.DELETED);
        log.info("Booking deleted with bookingId: {}", bookingId);
        bookingRepository.save(booking);
    }

    public BookingsSummaryResponse getBookingSummaryForServiceProvider(Long serviceProviderId){
        log.info("Generating booking summary for serviceProviderId: {}", serviceProviderId);
        List<BookingStatusCount> rows =
                bookingRepository.countBookingsByStatusGrouped(serviceProviderId);
        log.info("Fetched booking status counts: {}", rows);

        long completed = 0, pending = 0, cancelled = 0, deleted = 0, rescheduled = 0;
        log.info("Processing booking status counts");
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
        log.info("Processed booking status counts - Completed: {}, Pending: {}, Cancelled: {}, Deleted: {}, Rescheduled: {}",
                completed, pending, cancelled, deleted, rescheduled);

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
        log.info("Rescheduling booking with bookingId: {}", bookingId);

        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new BookingNotFoundException("Booking not found"));

        log.info("Existing booking details: {}", existingBooking);

        LocalDate changedDate = request.newBookingDate() != null ? request.newBookingDate() : existingBooking.getBookingDate();
        LocalTime changedStartTime = request.newBookingStartTime() != null ? request.newBookingStartTime() : existingBooking.getBookingStartTime();
        LocalTime changedEndTime = request.newBookingEndTime() != null ? request.newBookingEndTime() : existingBooking.getBookingEndTime();

        log.info("Changed booking details - Date: {}, StartTime: {}, EndTime: {}", changedDate, changedStartTime, changedEndTime);

        AvailabilityStatusResponse response = availabilityServiceClient.getAvailabilityStatus(
                AvailabilityStatusRequest.builder()
                        .serviceProviderId(existingBooking.getServiceProviderId())
                        .serviceId(existingBooking.getServiceId())
                        .startTime(changedStartTime)
                        .endTime(changedEndTime)
                        .date(changedDate)
                        .build()
        );

        log.info("Availability status for rescheduling - serviceId: {} with providerId: {} on date: {} from {} to {} is {}",
                existingBooking.getServiceId(), existingBooking.getServiceProviderId(), changedDate,
                changedStartTime, changedEndTime, response.status());

        if (response.status().equals(AvailabilityStatus.BLOCKED) ||
                response.status().equals(AvailabilityStatus.OUTSIDE_WORKING_HOURS)){
            throw new TimeSlotAlreadyBookedException("Time slot not available");
        }else if (response.status().equals(AvailabilityStatus.AVAILABLE)) {
            Optional<Booking> conflictingBooking =
                    bookingRepository.findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(
                            existingBooking.getServiceProviderId(), existingBooking.getServiceId(),
                            changedDate, changedStartTime, changedEndTime);

            log.info(" Checking for conflicting bookings for rescheduling - serviceId: {} with providerId: {} on date: {} from {} to {}",
                    existingBooking.getServiceId(), existingBooking.getServiceProviderId(), changedDate,
                    changedStartTime, changedEndTime);

            if(conflictingBooking.isPresent() && !conflictingBooking.get().getBookingId().equals(bookingId)){
                log.error("Time slot already booked for rescheduling - serviceId: {} with providerId: {} on date: {} from {} to {}",
                        existingBooking.getServiceId(), existingBooking.getServiceProviderId(), changedDate,
                        changedStartTime, changedEndTime);
                throw new TimeSlotAlreadyBookedException("Time slot already booked");
            }
        }else {
            log.error("Unknown availability status received: {} for rescheduling - serviceId: {} with providerId: {} on date: {} from {} to {}",
                    response.status(), existingBooking.getServiceId(), existingBooking.getServiceProviderId(), changedDate,
                    changedStartTime, changedEndTime);
            throw new UnknownAvailabilityStatusException("Unknown availability status");
        }

        existingBooking.setBookingStatus(RESCHEDULED);
        log.info("Marking existing booking as RESCHEDULED for bookingId: {}", bookingId);
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
        log.info("Created new booking with bookingId: {} as part of rescheduling", savedBooking.getBookingId());
        existingBooking.setRescheduledToId(String.valueOf(savedBooking.getBookingId()));
        bookingRepository.save(existingBooking);
        log.info("Updated existing booking with rescheduledToId: {}", existingBooking.getRescheduledToId());

        return bookingMapper.toDto(savedBooking);
    }



}
