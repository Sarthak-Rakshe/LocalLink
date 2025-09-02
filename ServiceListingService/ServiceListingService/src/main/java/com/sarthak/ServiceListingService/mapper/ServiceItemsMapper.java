package com.sarthak.ServiceListingService.mapper;

import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.model.ServiceItem;
import org.springframework.stereotype.Component;

@Component
public class ServiceItemsMapper {

    public ServiceItemDto entityToDto(ServiceItem serviceItem){
        ServiceItemDto serviceItemDto = new ServiceItemDto();
        serviceItemDto.setServiceId(serviceItem.getServiceId());
        serviceItemDto.setServiceName(serviceItem.getServiceName());
        serviceItemDto.setServiceCategory(serviceItem.getServiceCategory());
        serviceItemDto.setServiceDescription(serviceItem.getServiceDescription());
        serviceItemDto.setServicePricePerHour(serviceItem.getServicePricePerHour());
        serviceItemDto.setServiceProviderId(serviceItem.getServiceProviderId());
        serviceItemDto.setServiceRadius(serviceItem.getServiceRadius());

        return serviceItemDto;
    }

    public ServiceItem dtoToEntity(ServiceItemDto serviceItemDto){
        ServiceItem serviceItem = new ServiceItem();
        serviceItem.setServiceId(serviceItemDto.getServiceId());
        serviceItem.setServiceName(serviceItemDto.getServiceName());
        serviceItem.setServiceCategory(serviceItemDto.getServiceCategory());
        serviceItem.setServiceDescription(serviceItemDto.getServiceDescription());
        serviceItem.setServicePricePerHour(serviceItemDto.getServicePricePerHour());
        serviceItem.setServiceProviderId(serviceItemDto.getServiceProviderId());
        serviceItem.setServiceRadius(serviceItemDto.getServiceRadius());

        return serviceItem;
    }
}
