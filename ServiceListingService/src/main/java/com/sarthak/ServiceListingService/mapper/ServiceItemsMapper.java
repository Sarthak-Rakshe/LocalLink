package com.sarthak.ServiceListingService.mapper;

import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.model.ServiceItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceItemsMapper {

    public ServiceItemDto entityToDto(ServiceItem serviceItem){

        return new ServiceItemDto(
                serviceItem.getServiceId(),
                serviceItem.getServiceName(),
                serviceItem.getServiceDescription(),
                serviceItem.getServiceCategory(),
                serviceItem.getServicePricePerHour(),
                serviceItem.getServiceProviderId(),
                serviceItem.getLatitude(),
                serviceItem.getLongitude()
        );
    }

    public ServiceItem dtoToEntity(ServiceItemDto dto){
        return ServiceItem.builder()
                .serviceName(dto.serviceName())
                .serviceCategory(dto.serviceCategory())
                .serviceDescription(dto.serviceDescription())
                .servicePricePerHour(dto.servicePricePerHour())
                .serviceProviderId(dto.serviceProviderId())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .build();
    }

    public List<ServiceItemDto> toDtoList(List<ServiceItem> serviceItems){
        return serviceItems.stream().map(this::entityToDto).toList();
    }
}
