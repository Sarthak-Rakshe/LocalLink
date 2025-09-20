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
                serviceItem.getCity()
        );
    }

    public ServiceItem dtoToEntity(ServiceItemDto serviceItemDto){
        return  new ServiceItem(
                serviceItemDto.serviceId(),
                serviceItemDto.serviceName(),
                serviceItemDto.serviceDescription(),
                serviceItemDto.serviceCategory(),
                serviceItemDto.servicePricePerHour(),
                serviceItemDto.serviceProviderId(),
                serviceItemDto.city()
        );
    }

    public List<ServiceItemDto> toDtoList(List<ServiceItem> serviceItems){
        return serviceItems.stream().map(this::entityToDto).toList();
    }
}
