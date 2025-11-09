package com.sarthak.BookingService.repository;

import com.sarthak.BookingService.dto.QueryFilter;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Service
public class BookingSpecification {

    public static Specification<Booking> getBookingsByServiceProviderId(Long serviceProviderId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("serviceProviderId"), serviceProviderId);
    }

    public static Specification<Booking> getBookingsByCustomerId(Long customerId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("customerId"), customerId);
    }

    public static Specification<Booking> getBookingsByServiceId(Long serviceId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("serviceId"), serviceId);
    }

    public static Specification<Booking> getBookingsByBookingStatus(String bookingStatus) {
        BookingStatus bookingStatusEnum;
        try {
            bookingStatusEnum = BookingStatus.valueOf(bookingStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.info("Invalid booking status provided: {}", bookingStatus);
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("bookingStatus"), bookingStatusEnum);
    }

    public static Specification<Booking> getBookingsByServiceCategory(String serviceCategory) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("serviceCategory"), serviceCategory);
    }

    public static Specification<Booking> getBookingsForDays(Instant from, Instant to){
        return (root, query, criteriaBuilder) -> {
            if(from != null && to != null){
                return criteriaBuilder.between(root.get("createdAt"), from, to);
            } else if(from != null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
            } else if(to != null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
            } else {
                return null;
            }
        };
    }

    public static Specification<Booking> buildSpecification(QueryFilter queryFilter){
        if(queryFilter == null){
            return null;
        }
        Specification<Booking> spec = null;
        if(queryFilter.bookingStatus() != null){
            spec = getBookingsByBookingStatus(queryFilter.bookingStatus());
        }
        if(queryFilter.serviceCategory() != null){
            Specification<Booking> serviceCategorySpec = getBookingsByServiceCategory(queryFilter.serviceCategory());
            spec = (spec == null) ? serviceCategorySpec : spec.and(serviceCategorySpec);
        }
        if(queryFilter.dateFrom() != null || queryFilter.dateTo() != null){
            Instant from = queryFilter.dateFrom() != null ? Instant.parse(queryFilter.dateFrom()) : null;
            Instant to = queryFilter.dateTo() != null ? Instant.parse(queryFilter.dateTo()) : null;
            Specification<Booking> dateSpec = getBookingsForDays(from, to);
            spec = (spec == null) ? dateSpec : spec.and(dateSpec);
        }
        if(queryFilter.serviceProviderId() != null){
            Specification<Booking> serviceProviderSpec = getBookingsByServiceProviderId(queryFilter.serviceProviderId());
            spec = (spec == null) ? serviceProviderSpec : spec.and(serviceProviderSpec);
        }
        if(queryFilter.customerId() != null){
            Specification<Booking> customerSpec = getBookingsByCustomerId(queryFilter.customerId());
            spec = (spec == null) ? customerSpec : spec.and(customerSpec);
        }
        if(queryFilter.serviceId() != null){
            Specification<Booking> serviceSpec = getBookingsByServiceId(queryFilter.serviceId());
            spec = (spec == null) ? serviceSpec : spec.and(serviceSpec);
        }
        return spec;
    }

}
