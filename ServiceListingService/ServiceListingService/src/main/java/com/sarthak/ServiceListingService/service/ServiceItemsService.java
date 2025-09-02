package com.sarthak.ServiceListingService.service;

import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.exception.ServiceNotFoundException;
import com.sarthak.ServiceListingService.mapper.ServiceItemsMapper;
import com.sarthak.ServiceListingService.model.ServiceItem;
import com.sarthak.ServiceListingService.repository.ServiceItemRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceItemsService {

    private final ServiceItemRepository serviceItemRepository;
    private final ServiceItemsMapper serviceItemsMapper;

    public ServiceItemsService(ServiceItemRepository serviceItemRepository, ServiceItemsMapper serviceItemsMapper) {
        this.serviceItemRepository = serviceItemRepository;
        this.serviceItemsMapper = serviceItemsMapper;
    }

    public ServiceItemDto getServiceById(Long id){
        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(()-> new ServiceNotFoundException("Service not found"));
        return serviceItemsMapper.entityToDto(serviceItem);
    }

    public ServiceItemDto createService(ServiceItemDto serviceItemDto){
        ServiceItem serviceItem = serviceItemsMapper.dtoToEntity(serviceItemDto);
        ServiceItem savedServiceItem = serviceItemRepository.save(serviceItem);
        return serviceItemsMapper.entityToDto(savedServiceItem);
    }

    public String deleteService(Long id){
        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(()-> new ServiceNotFoundException("Service not found"));
        serviceItemRepository.delete(serviceItem);
        return "Service deleted successfully";
    }
}
