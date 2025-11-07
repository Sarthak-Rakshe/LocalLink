package com.sarthak.BookingService.mapper;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.dto.CustomerDto;
import com.sarthak.BookingService.dto.ServiceDto;
import com.sarthak.BookingService.dto.ServiceItemDto;
import com.sarthak.BookingService.dto.ServiceProviderDto;
import com.sarthak.BookingService.dto.response.BookingResponse;
import com.sarthak.BookingService.dto.response.UsernameResponse;
import com.sarthak.BookingService.model.Booking;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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

    public BookingResponse toResponse(
            BookingDto bookingDto,
            Map<Long, UsernameResponse> usernameResponseMap,
            Map<Long, ServiceItemDto> serviceItemResponseMap
    ) {
        BookingResponse bookingResponse = new BookingResponse();

        // Map basic identifiers and timestamps
        bookingResponse.setBookingId(bookingDto.bookingId());
        bookingResponse.setBookingDate(bookingDto.bookingDate());
        bookingResponse.setBookingStartTime(bookingDto.bookingStartTime());
        bookingResponse.setBookingEndTime(bookingDto.bookingEndTime());
        bookingResponse.setBookingStatus(bookingDto.bookingStatus().name());
        bookingResponse.setCreatedAt(bookingDto.createdAt());
        bookingResponse.setRescheduledToId(bookingDto.rescheduledToId());

        // You can compute or set amount if you have it elsewhere
        bookingResponse.setAmount(null); // or calculate based on service price * duration

        // Map related DTOs
        UsernameResponse customerUsername = usernameResponseMap.get(bookingDto.customerId());
        UsernameResponse providerUsername = usernameResponseMap.get(bookingDto.serviceProviderId());
        ServiceItemDto serviceItemDto = serviceItemResponseMap.get(bookingDto.serviceId());

        if (customerUsername != null) {
            CustomerDto customerDto = new CustomerDto(
                    customerUsername.userId(),
                    customerUsername.username(),
                    customerUsername.userContact()
            );
            bookingResponse.setCustomer(customerDto);
        }

        if (providerUsername != null) {
            ServiceProviderDto serviceProviderDto = new ServiceProviderDto(
                    providerUsername.userId(),
                    providerUsername.username(),
                    providerUsername.userContact()
            );
            bookingResponse.setServiceProvider(serviceProviderDto);
        }

        if (serviceItemDto != null) {
            double amount = serviceItemDto.servicePricePerHour() *
                    (Double.parseDouble(bookingDto.bookingEndTime().split(":")[0]) -
                            Double.parseDouble(bookingDto.bookingStartTime().split(":")[0]));
            ServiceDto serviceDto = new ServiceDto(
                    serviceItemDto.serviceId(),
                    serviceItemDto.serviceName(),
                    serviceItemDto.serviceCategory(),
                    amount
            );
            bookingResponse.setService(serviceDto);
        }

        return bookingResponse;
    }

}
