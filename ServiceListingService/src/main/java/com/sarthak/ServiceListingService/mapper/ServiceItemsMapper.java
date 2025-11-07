package com.sarthak.ServiceListingService.mapper;

import com.sarthak.ServiceListingService.dto.ReviewAggregateResponse;
import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.model.ServiceItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceItemsMapper {

    public ServiceItemDto entityToDto(ServiceItem serviceItem, ReviewAggregateResponse reviewAggregateResponse){
        if (reviewAggregateResponse == null){
            reviewAggregateResponse = new ReviewAggregateResponse(0L, serviceItem.getServiceId(), serviceItem.getServiceProviderId(), 0.0, 0L);
        }
        return ServiceItemDto.builder()
                .serviceId(serviceItem.getServiceId())
                .serviceName(serviceItem.getServiceName())
                .serviceDescription(serviceItem.getServiceDescription())
                .serviceCategory(serviceItem.getServiceCategory())
                .servicePricePerHour(serviceItem.getServicePricePerHour())
                .serviceProviderId(serviceItem.getServiceProviderId())
                .latitude(serviceItem.getLatitude())
                .longitude(serviceItem.getLongitude())
                .reviewAggregate(reviewAggregateResponse)
                .build();

    }

    public ServiceItem dtoToEntity(ServiceItemDto dto){
        return ServiceItem.builder()
                .serviceName(dto.getServiceName())
                .serviceCategory(dto.getServiceCategory())
                .serviceDescription(dto.getServiceDescription())
                .servicePricePerHour(dto.getServicePricePerHour())
                .serviceProviderId(dto.getServiceProviderId())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }

    public List<ServiceItemDto> toDtoList(List<ServiceItem> serviceItems){
        return serviceItems.stream()
                .map(serviceItem -> entityToDto(serviceItem, null))
                .toList();
    }
}
