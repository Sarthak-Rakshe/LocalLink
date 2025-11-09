package com.sarthak.BookingService.service;

import com.sarthak.BookingService.client.AvailabilityServiceClient;
import com.sarthak.BookingService.client.ServiceListingClient;
import com.sarthak.BookingService.client.UserServiceClient;
import com.sarthak.BookingService.config.shared.UserPrincipal;
import com.sarthak.BookingService.dto.AvailabilityStatus;
import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.dto.BookingStatusCount;
import com.sarthak.BookingService.dto.QueryFilter;
import com.sarthak.BookingService.dto.ServiceItemDto;
import com.sarthak.BookingService.dto.ServiceListingQueryFilter;
import com.sarthak.BookingService.dto.Slot;
import com.sarthak.BookingService.dto.request.BookingRescheduleRequest;
import com.sarthak.BookingService.dto.response.BookedSlotsResponse;
import com.sarthak.BookingService.dto.response.BookingResponse;
import com.sarthak.BookingService.dto.response.BookingsSummaryResponse;
import com.sarthak.BookingService.dto.response.UsernameResponse;
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
import com.sarthak.BookingService.repository.BookingSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sarthak.BookingService.model.BookingStatus.*;

@Service
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final AvailabilityServiceClient availabilityServiceClient;
    private final ServiceListingClient serviceListingClient;
    private final UserServiceClient userServiceClient;
    private final Set<String> ALLOWED_SORT_FIELDS = Set.of("bookingId", "serviceProviderId", "serviceId",
            "customerId", "bookingDate", "bookingStartTime", "bookingEndTime", "bookingStatus", "createdAt");
    private final Set<BookingStatus> EXCLUDED_STATUSES_FOR_OVERLAP_CHECK = Set.of(CANCELLED, DELETED);
    private final Set<BookingStatus> INCLUDED_STATUSES_FOR_BOOKED_SLOTS = Set.of(PENDING, CONFIRMED);

    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper,
            AvailabilityServiceClient availabilityServiceClient, ServiceListingClient serviceListingClient, UserServiceClient userServiceClient) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.availabilityServiceClient = availabilityServiceClient;
        this.serviceListingClient = serviceListingClient;
        this.userServiceClient = userServiceClient;
    }

    public BookingDto getBookingDetails(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        log.info("Fetched booking details for bookingId: {}", bookingId);
        return bookingMapper.toDto(booking);
    }

    public List<BookingDto> getAllByServiceProviderIdAndDate(Long serviceProviderId, LocalDate date) {
        List<Booking> bookings = bookingRepository.findAllByServiceProviderIdAndBookingDateOrderByBookingStartTime(
                serviceProviderId,
                date);
        log.info("Fetched {} bookings for serviceProviderId: {} on date: {}", bookings.size(), serviceProviderId, date);
        return bookingMapper.toDtoList(bookings);
    }

    public Page<BookingDto> getAllByCustomerId(Long customerId, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = getPageable(page, size, sortBy, sortDir);
        Page<Booking> bookings = bookingRepository.findByCustomerId(customerId, pageable);
        log.info("Fetched {} bookings for customerId: {}", bookings.getTotalElements(), customerId);
        return bookings.map(bookingMapper::toDto);
    }

    public Page<BookingDto> getAllBookings(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = getPageable(page, size, sortBy, sortDir);
        Page<Booking> bookings = bookingRepository.findAll(pageable);
        log.info("Fetched {} bookings", bookings.getTotalElements());
        return bookings.map(bookingMapper::toDto);
    }

    public Page<BookingDto> getAllByServiceProviderId(Long serviceProviderId, int page, int size, String sortBy,
            String sortDir) {
        Pageable pageable = getPageable(page, size, sortBy, sortDir);
        Page<Booking> bookings = bookingRepository.findByServiceProviderId(serviceProviderId, pageable);
        log.info("Fetched {} bookings for serviceProviderId: {}", bookings.getTotalElements(), serviceProviderId);
        return bookings.map(bookingMapper::toDto);
    }

    public BookedSlotsResponse getBookedSlotsForProviderOnDate(Long serviceProviderId, Long serviceId, LocalDate date) {
        List<Booking> bookings = bookingRepository
                .findBookedSlotsForProviderByDate(serviceProviderId,
                        serviceId, date, INCLUDED_STATUSES_FOR_BOOKED_SLOTS);

        List<Slot> bookedSlots = new ArrayList<>();

        if (bookings.isEmpty()) {
            log.info("No bookings found for serviceProviderId: {} and serviceId: {} on date: {}", serviceProviderId,
                    serviceId, date);
            return BookedSlotsResponse.builder()
                    .serviceProviderId(serviceProviderId)
                    .serviceId(serviceId)
                    .bookedSlots(bookedSlots)
                    .date(date.toString())
                    .build();
        }
        log.debug("Fetched {} bookings for serviceProviderId: {} and serviceId: {} on date: {}", bookings.size(),
                serviceProviderId, serviceId, date);

        bookedSlots = bookings.stream()
                .map(b -> Slot.builder()
                        .startTime(b.getBookingStartTime())
                        .endTime(b.getBookingEndTime())
                        .build())
                .toList();

        bookedSlots = mergeBookedSlots(bookedSlots);

        log.info("Mapped bookings to {} booked slots for serviceProviderId: {} and serviceId: {} on date: {}",
                bookedSlots.size(), serviceProviderId, serviceId, date);

        return BookedSlotsResponse.builder()
                .serviceProviderId(serviceProviderId)
                .serviceId(serviceId)
                .bookedSlots(bookedSlots)
                .date(date.toString())
                .build();
    }

    private List<Slot> mergeBookedSlots(List<Slot> slots) {
        if (slots.isEmpty()) return List.of();

        List<Slot> mergedSlots = new ArrayList<>();

        LocalTime start = slots.getFirst().startTime();
        LocalTime end = slots.getFirst().endTime();

        for (int i = 1; i < slots.size(); i++) {
            Slot current = slots.get(i);

            if (!current.startTime().isAfter(end)) {
                // overlapping or touching
                if (current.endTime().isAfter(end)) {
                    end = current.endTime();
                }
            } else {
                mergedSlots.add(Slot.builder().startTime(start).endTime(end).build());
                start = current.startTime();
                end = current.endTime();
            }
        }

        // add the final merged slot
        mergedSlots.add(Slot.builder().startTime(start).endTime(end).build());
        return mergedSlots;
    }


    @Transactional
    public BookingDto bookService(BookingDto bookingDto, UserPrincipal userPrincipal) {
        if (Objects.equals(bookingDto.serviceProviderId(), userPrincipal.getUserId())) {
            throw new IllegalStateException("Service provider cannot book their own service");
        }
        Booking booking = bookingMapper.toEntity(bookingDto);

        ZoneId zone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime bookingZdt = ZonedDateTime.of(booking.getBookingDate(), booking.getBookingStartTime(), zone);
        if (bookingZdt.isBefore(ZonedDateTime.now(zone))) {
            throw new IllegalStateException("Booking start time cannot be in the past");
        }
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
                        .build());

        log.info("Availability status for serviceId: {} with providerId: {} on date: {} from {} to {} is {}",
                booking.getServiceId(), booking.getServiceProviderId(), booking.getBookingDate(),
                booking.getBookingStartTime(), booking.getBookingEndTime(), response.status());

        if (response.status().equals(AvailabilityStatus.BLOCKED) ||
                response.status().equals(AvailabilityStatus.OUTSIDE_WORKING_HOURS)) {
            throw new ProviderNotAvailableForGivenTimeSlotException("Time slot not available");
        } else if (response.status().equals(AvailabilityStatus.AVAILABLE)) {
            boolean hasOverlappingBooking = bookingRepository
                    .hasOverlappingBooking(booking.getServiceProviderId(), booking.getServiceId(),
                            booking.getBookingDate(), booking.getBookingStartTime(), booking.getBookingEndTime(), EXCLUDED_STATUSES_FOR_OVERLAP_CHECK);

            log.info("Checking for existing bookings for serviceId: {} with providerId: {} on date: {} from {} to {}",
                    booking.getServiceId(), booking.getServiceProviderId(), booking.getBookingDate(),
                    booking.getBookingStartTime(), booking.getBookingEndTime());

            if (hasOverlappingBooking) {
                log.error("Time slot already booked for serviceId: {} with providerId: {} on date: {} from {} to {}",
                        booking.getServiceId(), booking.getServiceProviderId(), booking.getBookingDate(),
                        booking.getBookingStartTime(), booking.getBookingEndTime());
                throw new TimeSlotAlreadyBookedException("Time slot already booked");
            }

        } else {
            log.error(
                    "Unknown availability status received: {} for serviceId: {} with providerId: {} on date: {} from {} to {}",
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

    // After payment is successful
    @Transactional
    private BookingDto confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed with bookingId: {}", bookingId);
        return bookingMapper.toDto(updatedBooking);
    }

    // For cancelling a booking
    @Transactional
    private BookingDto cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking cancelled with bookingId: {}", bookingId);
        return bookingMapper.toDto(updatedBooking);
    }

    // After service is delivered
    @Transactional
    private BookingDto completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        booking.setBookingStatus(COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking completed with bookingId: {}", bookingId);
        return bookingMapper.toDto(updatedBooking);
    }

    // For deleting a booking record (soft delete)
    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        booking.setBookingStatus(BookingStatus.DELETED);
        log.info("Booking deleted with bookingId: {}", bookingId);
        bookingRepository.save(booking);
    }

    public BookingsSummaryResponse getBookingSummary(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        String userType = userPrincipal.getUserType();
        List<BookingStatusCount> rows;
        if(userType.equalsIgnoreCase("CUSTOMER")){
            rows = bookingRepository.countBookingsByStatusGroupedForCustomer(userId);
        }else if (userType.equalsIgnoreCase("PROVIDER")){
            log.info("Generating booking summary for serviceProviderId: {}", userId);
            rows = bookingRepository.countBookingsByStatusGroupedForProvider(userId);
            log.info("Fetched booking status counts: {}", rows);
        }else {
            rows = new ArrayList<>();
        }

        long completed = 0, confirmed = 0, pending = 0, cancelled = 0, deleted = 0, rescheduled = 0;
        log.info("Processing booking status counts");
        for (BookingStatusCount r : rows) {
            switch (r.status()) {
                case COMPLETED -> completed = r.count();
                case PENDING -> pending = r.count();
                case CONFIRMED -> confirmed = r.count();
                case CANCELLED -> cancelled = r.count();
                case DELETED -> deleted = r.count();
                case RESCHEDULED -> rescheduled = r.count();
                default -> {
                }
            }
        }
        log.info(
                "Processed booking status counts - Completed: {}, Pending: {}, Confirmed: {}, Cancelled: {}, Deleted:{}, Rescheduled: {}",
                completed, pending, confirmed, cancelled, deleted, rescheduled);

        long total = completed + pending + cancelled + confirmed;

        return BookingsSummaryResponse.builder()
                .totalBookings(total)
                .completedBookings(completed)
                .pendingBookings(pending)
                .confirmedBookings(confirmed)
                .cancelledBookings(cancelled)
                .deletedBookings(deleted)
                .rescheduledBookings(rescheduled)
                .build();
    }

    @Transactional
    public BookingDto rescheduleBooking(Long bookingId, BookingRescheduleRequest request) {
        log.info("Rescheduling booking with bookingId: {}", bookingId);

        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        log.info("Existing booking details: {}", existingBooking);

        LocalDate changedDate = request.newBookingDate() != null ? request.newBookingDate()
                : existingBooking.getBookingDate();
        LocalTime changedStartTime = request.newBookingStartTime() != null ? request.newBookingStartTime()
                : existingBooking.getBookingStartTime();
        LocalTime changedEndTime = request.newBookingEndTime() != null ? request.newBookingEndTime()
                : existingBooking.getBookingEndTime();

        ZoneId zone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime bookingZdt = ZonedDateTime.of(changedDate, changedStartTime, zone);
        if (bookingZdt.isBefore(ZonedDateTime.now(zone))) {
            throw new IllegalStateException("Booking start time cannot be in the past");
        }

        log.info("Changed booking details - Date: {}, StartTime: {}, EndTime: {}", changedDate, changedStartTime,
                changedEndTime);

        AvailabilityStatusResponse response = availabilityServiceClient.getAvailabilityStatus(
                AvailabilityStatusRequest.builder()
                        .serviceProviderId(existingBooking.getServiceProviderId())
                        .serviceId(existingBooking.getServiceId())
                        .startTime(changedStartTime)
                        .endTime(changedEndTime)
                        .date(changedDate)
                        .build());

        log.info(
                "Availability status for rescheduling - serviceId: {} with providerId: {} on date: {} from {} to {} is {}",
                existingBooking.getServiceId(), existingBooking.getServiceProviderId(), changedDate,
                changedStartTime, changedEndTime, response.status());

        if (response.status().equals(AvailabilityStatus.BLOCKED) ||
                response.status().equals(AvailabilityStatus.OUTSIDE_WORKING_HOURS)) {
            throw new TimeSlotAlreadyBookedException("Time slot not available");
        } else if (response.status().equals(AvailabilityStatus.AVAILABLE)) {
            boolean hasOverlappingBookings = bookingRepository
                    .hasOverlappingBooking(
                            existingBooking.getServiceProviderId(), existingBooking.getServiceId(),
                            changedDate, changedStartTime, changedEndTime, EXCLUDED_STATUSES_FOR_OVERLAP_CHECK);

            log.info(
                    " Checking for conflicting bookings for rescheduling - serviceId: {} with providerId: {} on date: {} from {} to {}",
                    existingBooking.getServiceId(), existingBooking.getServiceProviderId(), changedDate,
                    changedStartTime, changedEndTime);

            if (hasOverlappingBookings) {
                log.error(
                        "Time slot already booked for rescheduling - serviceId: {} with providerId: {} on date: {} from {} to {}",
                        existingBooking.getServiceId(), existingBooking.getServiceProviderId(), changedDate,
                        changedStartTime, changedEndTime);
                throw new TimeSlotAlreadyBookedException("Time slot already booked");
            }
        } else {
            log.error(
                    "Unknown availability status received: {} for rescheduling - serviceId: {} with providerId: {} on date: {} from {} to {}",
                    response.status(), existingBooking.getServiceId(), existingBooking.getServiceProviderId(),
                    changedDate,
                    changedStartTime, changedEndTime);
            throw new UnknownAvailabilityStatusException("Unknown availability status");
        }

        log.info("Marking existing booking as RESCHEDULED for bookingId: {}", bookingId);
        Booking newBooking = Booking.builder()
                .serviceProviderId(existingBooking.getServiceProviderId())
                .serviceId(existingBooking.getServiceId())
                .serviceCategory(existingBooking.getServiceCategory())
                .customerId(existingBooking.getCustomerId())
                .bookingDate(changedDate)
                .bookingStartTime(changedStartTime)
                .bookingEndTime(changedEndTime)
                .bookingStatus(existingBooking.getBookingStatus())
                .build();

        existingBooking.setBookingStatus(RESCHEDULED);
        Booking savedBooking = bookingRepository.save(newBooking);
        log.info("Created new booking with bookingId: {} as part of rescheduling", savedBooking.getBookingId());
        existingBooking.setRescheduledToId(String.valueOf(savedBooking.getBookingId()));
        bookingRepository.save(existingBooking);
        log.info("Updated existing booking with rescheduledToId: {}", existingBooking.getRescheduledToId());

        return bookingMapper.toDto(savedBooking);
    }

    public BookingDto updateBookingStatus(Long bookingId, String status) {
        log.info("Updating booking status for bookingId: {} to status: {}", bookingId, status);
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        String currentStatus = existingBooking.getBookingStatus().name();
        if (currentStatus.equals("COMPLETED") || currentStatus.equals("DELETED")) {
            log.error("Cannot update status for completed or deleted booking with bookingId: {}", bookingId);
            throw new IllegalStateException("Cannot update status for completed or deleted booking");
        }
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> confirmBooking(bookingId);
            case "CANCELLED" -> cancelBooking(bookingId);
            case "COMPLETED" -> completeBooking(bookingId);
            case "DELETED" -> {
                deleteBooking(bookingId);
                yield null;
            }
            default -> throw new IllegalArgumentException("Invalid booking status: " + status);
        };
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> getBookingList(QueryFilter queryFilter, int page, int size, String sortBy, String sortDir){
        Pageable pageable = getPageable(page, size, sortBy, sortDir);

        Specification<Booking> specification = BookingSpecification.buildSpecification(queryFilter);
        Page<Booking> bookings = bookingRepository.findAll(specification, pageable);
        log.info("Fetched {} bookings with applied filters", bookings.getTotalElements());

        return bookings.map(bookingMapper::toDto);
    }

    public Page<BookingResponse> getBookingResponse(Page<BookingDto> bookings) {
        List<BookingDto> bookingDtoList = bookings.getContent();
        Set<Long> serviceIdSet = new HashSet<>();
        List<Long> userIdList = new ArrayList<>();
        for(BookingDto bookingDto : bookingDtoList){
            serviceIdSet.add(bookingDto.serviceId());
            userIdList.add(bookingDto.customerId());
            userIdList.add(bookingDto.serviceProviderId());
        }
        ServiceListingQueryFilter serviceListingQueryFilter = new ServiceListingQueryFilter(
                serviceIdSet, null, null, null, null, null);

        Page<ServiceItemDto> serviceItemsPage = serviceListingClient.getServiceDetails(serviceListingQueryFilter, 0,
                1000, "serviceId", "asc");
        List<ServiceItemDto> serviceItemDtoList = serviceItemsPage.getContent();
        log.info("Fetched {} service details from Service Listing Service", serviceItemDtoList.size());
        Map<Long, ServiceItemDto> serviceItemDtoMap = serviceItemDtoList.stream()
                .collect(Collectors.toMap(ServiceItemDto::serviceId, item -> item));

        List<UsernameResponse> usernameResponses = userServiceClient.getUsernameByUserId(userIdList);
        Map<Long, UsernameResponse> usernameResponseMap = usernameResponses.stream()
                .collect(Collectors.toMap(UsernameResponse::userId, item -> item));
        log.info("Fetched {} usernames from User Service", usernameResponses.size());
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for(BookingDto bookingDto : bookingDtoList){
            bookingResponses.add(bookingMapper.toResponse(bookingDto, usernameResponseMap, serviceItemDtoMap));
        }

        return new PageImpl<>(bookingResponses, bookings.getPageable(), bookings.getTotalElements());
    }

    private Pageable getPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0)
            page = 0;
        if (size <= 0)
            size = 10; // default page size
        if (sortBy == null || sortBy.isEmpty())
            sortBy = "createdAt";
        String sortDirNormalized = (sortDir != null) ? sortDir.toLowerCase() : "desc";

        if (!sortDirNormalized.equals("asc") && !sortDirNormalized.equals("desc")) {
            sortDirNormalized = "desc"; // default to descending if invalid
        }

        String sortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        Sort sort = sortDirNormalized.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        return PageRequest.of(page, size, sort);
    }

}
