package com.sarthak.ServiceListingService.service;

import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.exception.DuplicateServiceException;
import com.sarthak.ServiceListingService.exception.ServiceNotFoundException;
import com.sarthak.ServiceListingService.mapper.ServiceItemsMapper;
import com.sarthak.ServiceListingService.model.ServiceItem;
import com.sarthak.ServiceListingService.model.SortFields;
import com.sarthak.ServiceListingService.repository.ServiceItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.Objects;

@Slf4j
@Service
public class ServiceItemsService {

    private final ServiceItemRepository serviceItemRepository;
    private final ServiceItemsMapper serviceItemsMapper;

    public ServiceItemsService(ServiceItemRepository serviceItemRepository, ServiceItemsMapper serviceItemsMapper) {
        this.serviceItemRepository = serviceItemRepository;
        this.serviceItemsMapper = serviceItemsMapper;
    }

    public ServiceItemDto getServiceById(Long id){
        log.info("Fetching service with id: {}", id);
        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(()-> new ServiceNotFoundException("Service not found for id: " + id));
        log.info("Service found: {}", serviceItem);
        return serviceItemsMapper.entityToDto(serviceItem);
    }

    public Page<ServiceItemDto> getAllServices(int page, int size, String sortBy, String sortDir){
        log.info("Fetching all services - page: {}, size: {}", page, size);

        Pageable pageable = getPageable(page, size, sortBy, sortDir);

        Page<ServiceItem> services = serviceItemRepository.findAll(pageable);
        log.debug("Fetched {} services", services.getNumberOfElements());
        return services.map(serviceItemsMapper::entityToDto);
    }

    public Page<ServiceItemDto> getServicesByProviderId(Long providerId, int page, int size, String sortBy, String sortDir){
        log.info("Fetching services for provider id: {} - page: {}, size: {}", providerId, page, size);

        Pageable pageable = getPageable(page, size, sortBy, sortDir);

        Page<ServiceItem> services = serviceItemRepository.findAllByServiceProviderId(providerId, pageable);
        log.debug("Fetched {} services for provider id: {}", services.getNumberOfElements(), providerId);
        return services.map(serviceItemsMapper::entityToDto);
    }

    public Page<ServiceItemDto> getServicesByCategory(String category, int page, int size, String sortBy, String sortDir){
        log.info("Fetching services for category: {} - page: {}, size: {}", category, page, size);

        Pageable pageable = getPageable(page, size, sortBy, sortDir);

        

        Page<ServiceItem> services = serviceItemRepository.findAllByServiceCategoryIgnoreCase(category, pageable);
        log.debug("Fetched {} services for category: {}", services.getNumberOfElements(), category);
        return services.map(serviceItemsMapper::entityToDto);
    }

    public Page<ServiceItemDto> getNearbyService(Double userLatitude, Double userLongitude, int page, int size, String sortBy, String sortDir){
        if(userLatitude == null || userLongitude == null){
            throw new IllegalArgumentException("User latitude and longitude must be provided");
        }

        log.info("Fetching nearby services for location: ({}, {}) - page: {}, size: {}", userLatitude, userLongitude, page, size);

        Pageable pageable = getPageable(page, size, sortBy, sortDir);
        

        // Assuming a default radius of 5 km for nearby services
        Double radiusInKm = 5.0;
        Page<ServiceItem> services = serviceItemRepository.findNearbyServices(userLatitude, userLongitude, radiusInKm, pageable);
        log.debug("Fetched {} nearby services", services.getNumberOfElements());
        return services.map(serviceItemsMapper::entityToDto);
    }

    @Transactional
    public ServiceItemDto createService(ServiceItemDto serviceItemDto){
        log.info("Creating service: {}", serviceItemDto);
        ServiceItem serviceItem = serviceItemsMapper.dtoToEntity(serviceItemDto);

        ServiceItem existingService = serviceItemRepository.findByServiceNameAndServiceProviderId(
                serviceItem.getServiceName(), serviceItem.getServiceProviderId()
        ).orElse(null);

        if (existingService != null){
            log.error("Service with name '{}' already exists for provider id {}",
                      serviceItem.getServiceName(), serviceItem.getServiceProviderId());
            throw new DuplicateServiceException("Service with the same name already exists for this provider");
        }

        ServiceItem savedService = serviceItemRepository.save(serviceItem);
        log.info("Service created with id: {}", savedService.getServiceId());
        return serviceItemsMapper.entityToDto(savedService);
    }

    @Transactional
    public ServiceItemDto updateService(Long id, ServiceItemDto serviceItemDto, Long userId){
        log.info("Updating service with id: {}", id);
        ServiceItem existingService = serviceItemRepository.findById(id)
                .orElseThrow(()-> new ServiceNotFoundException("Service not found for id: " + id));
        if(!Objects.equals(userId, existingService.getServiceProviderId())){
            log.error("User id {} is not authorized to update service id {}", userId, id);
            throw new AccessDeniedException("You are not authorized to update this service");
        }

        boolean existsWithSameName = serviceItemRepository
                        .findByServiceNameAndServiceProviderId(serviceItemDto.serviceName(),
                                serviceItemDto.serviceProviderId())
                        .filter(s -> !s.getServiceId().equals(id))
                        .isPresent();

        if (existsWithSameName){
            log.error("Duplicate service name '{}' found for provider id {} during update",
                      serviceItemDto.serviceName(), existingService.getServiceProviderId());
            throw new DuplicateServiceException("Another service with the same name exists for this provider");
        }else {
            existingService.setServiceName(serviceItemDto.serviceName() != null
                    ? serviceItemDto.serviceName() : existingService.getServiceName());
        }
        existingService.setServiceDescription(serviceItemDto.serviceDescription() != null
                ? serviceItemDto.serviceDescription() : existingService.getServiceDescription());
        existingService.setServiceCategory(serviceItemDto.serviceCategory() != null
                ? serviceItemDto.serviceCategory() : existingService.getServiceCategory());
        existingService.setServicePricePerHour(serviceItemDto.servicePricePerHour() != 0.0
                ? serviceItemDto.servicePricePerHour() : existingService.getServicePricePerHour());
        existingService.setLatitude(serviceItemDto.latitude() != null
                ? serviceItemDto.latitude() : existingService.getLatitude());
        existingService.setLongitude(serviceItemDto.longitude() != null
                ? serviceItemDto.longitude() : existingService.getLongitude());

        ServiceItem updatedService = serviceItemRepository.save(existingService);
        log.info("Service with id {} updated successfully", id);
        return serviceItemsMapper.entityToDto(updatedService);
    }

    @Transactional
    public void deleteService(Long id, Long userId){
        log.info("Deleting service with id: {}", id);
        if (!serviceItemRepository.existsById(id)){
            log.error("Service with id {} not found for deletion", id);
            throw new ServiceNotFoundException("Service not found for id: " + id);
        }
        ServiceItem existingService = serviceItemRepository.findById(id).get();
        if(!Objects.equals(userId, existingService.getServiceProviderId())){
            log.error("User id {} is not authorized to delete service id {}", userId, id);
            throw new AccessDeniedException("You are not authorized to delete this service");
        }
        serviceItemRepository.deleteById(id);
        log.info("Service with id {} deleted successfully", id);
    }

    private SortFields validateSort(String sortBy){
        return SortFields.fromString(sortBy);
    }

    private Pageable getPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0) {
            log.warn("Invalid page number: {}. Defaulting to 0.", page);
            page = 0;
        }
        if (size <= 0) {
            log.warn("Invalid page size: {}. Defaulting to 10.", size);
            size = 10;
        }
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            log.warn("Invalid sort direction: {}. Defaulting to 'asc'.", sortDir);
            sortDir = "asc";
        }

        SortFields sortField = validateSort(sortBy);
        log.info("Sorting by field: {} in {} order", sortField.getField(), sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField.getField()).ascending()
                : Sort.by(sortField.getField()).descending();

        return PageRequest.of(page, size, sort);
    }
}
