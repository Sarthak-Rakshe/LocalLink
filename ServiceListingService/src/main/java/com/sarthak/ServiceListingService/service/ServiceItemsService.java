package com.sarthak.ServiceListingService.service;

import com.sarthak.ServiceListingService.client.ReviewServiceClient;
import com.sarthak.ServiceListingService.dto.QueryFilter;
import com.sarthak.ServiceListingService.dto.ReviewAggregateResponse;
import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.exception.DuplicateServiceException;
import com.sarthak.ServiceListingService.exception.ServiceNotFoundException;
import com.sarthak.ServiceListingService.mapper.ServiceItemsMapper;
import com.sarthak.ServiceListingService.model.ServiceItem;
import com.sarthak.ServiceListingService.model.SortFields;
import com.sarthak.ServiceListingService.repository.ServiceItemRepository;
import com.sarthak.ServiceListingService.repository.ServiceSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ServiceItemsService {

    private final ServiceItemRepository serviceItemRepository;
    private final ServiceItemsMapper serviceItemsMapper;
    private final ReviewServiceClient reviewServiceClient;

    public ServiceItemsService(ServiceItemRepository serviceItemRepository, ServiceItemsMapper serviceItemsMapper,
                               ReviewServiceClient reviewServiceClient) {
        this.serviceItemRepository = serviceItemRepository;
        this.serviceItemsMapper = serviceItemsMapper;
        this.reviewServiceClient = reviewServiceClient;
    }

    public ServiceItemDto getServiceById(Long id){
        log.info("Fetching service with id: {}", id);
        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(()-> new ServiceNotFoundException("Service not found for id: " + id));
        log.info("Service found: {}", serviceItem);
        Map<Long, ReviewAggregateResponse> reviewAggregateMap =
                reviewServiceClient.getAggregatesByServiceIds(List.of(id));
        ReviewAggregateResponse reviewAggregate = reviewAggregateMap.get(id);
        return serviceItemsMapper.entityToDto(serviceItem, reviewAggregate);
    }

    @Transactional(readOnly = true)
    public Page<ServiceItemDto> getAllServices(int page, int size, String sortBy, String sortDir, QueryFilter queryFilter){
        log.info("Fetching all services - page: {}, size: {}", page, size);

        Pageable pageable = getPageable(page, size, sortBy, sortDir, false);
        Specification<ServiceItem> spec = null;
        if (queryFilter != null) {
            spec = ServiceSpecification.buildSpecification(queryFilter);
        }

        Page<ServiceItem> services = serviceItemRepository.findAll(spec, pageable);
        log.debug("Fetched {} services", services.getNumberOfElements());

        return services.map(s-> serviceItemsMapper.entityToDto(s, null));
    }

    public Page<ServiceItemDto> addReviewAggregatesToServices(Page<ServiceItemDto> services){
        List<Long> serviceIds = services.map(ServiceItemDto::getServiceId).toList();
        Map<Long, ReviewAggregateResponse> reviewAggregates = reviewServiceClient.getAggregatesByServiceIds(serviceIds);
        List<ServiceItemDto> resultList = new ArrayList<>();
        for (ServiceItemDto service : services) {
            ReviewAggregateResponse reviewAggregate = reviewAggregates.get(service.getServiceId());
            service.setReviewAggregate(reviewAggregate);
            resultList.add(service);
        }
        return new PageImpl<>(resultList, services.getPageable(), services.getTotalElements());
    }

    public Page<ServiceItemDto> getNearbyService(Double userLatitude, Double userLongitude, int page, int size, String sortBy, String sortDir){
        if(userLatitude == null || userLongitude == null){
            throw new IllegalArgumentException("User latitude and longitude must be provided");
        }

        log.info("Fetching nearby services for location: ({}, {}) - page: {}, size: {}", userLatitude, userLongitude, page, size);

        Pageable pageable = getPageable(page, size, sortBy, sortDir, true);

        // Assuming a default radius of 5 km for nearby services
        Double radiusInKm = 5.0;
        Page<ServiceItem> services = serviceItemRepository.findNearbyServices(userLatitude, userLongitude, radiusInKm, pageable);
        log.debug("Fetched {} nearby services", services.getNumberOfElements());
        List<Long> serviceIds = services.map(ServiceItem::getServiceId).toList();
        Map<Long, ReviewAggregateResponse> reviewAggregates = reviewServiceClient.getAggregatesByServiceIds(serviceIds);
        List<ServiceItemDto> resultList = new ArrayList<>();
        for (ServiceItem service : services) {
            ReviewAggregateResponse reviewAggregate = reviewAggregates.get(service.getServiceId());
            resultList.add(serviceItemsMapper.entityToDto(service, reviewAggregate));
        }
        return new PageImpl<>(resultList, pageable, services.getTotalElements());
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
        return serviceItemsMapper.entityToDto(savedService, null);
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
                        .findByServiceNameAndServiceProviderId(serviceItemDto.getServiceName(),
                                serviceItemDto.getServiceProviderId())
                        .filter(s -> !s.getServiceId().equals(id))
                        .isPresent();

        if (existsWithSameName){
            log.error("Duplicate service name '{}' found for provider id {} during update",
                      serviceItemDto.getServiceName(), existingService.getServiceProviderId());
            throw new DuplicateServiceException("Another service with the same name exists for this provider");
        }else {
            existingService.setServiceName(serviceItemDto.getServiceName() != null
                    ? serviceItemDto.getServiceName() : existingService.getServiceName());
        }
        existingService.setServiceDescription(serviceItemDto.getServiceDescription() != null
                ? serviceItemDto.getServiceDescription() : existingService.getServiceDescription());
        existingService.setServiceCategory(serviceItemDto.getServiceCategory() != null
                ? serviceItemDto.getServiceCategory() : existingService.getServiceCategory());
        existingService.setServicePricePerHour(serviceItemDto.getServicePricePerHour() != 0.0
                ? serviceItemDto.getServicePricePerHour() : existingService.getServicePricePerHour());
        existingService.setLatitude(serviceItemDto.getLatitude() != null
                ? serviceItemDto.getLatitude() : existingService.getLatitude());
        existingService.setLongitude(serviceItemDto.getLongitude() != null
                ? serviceItemDto.getLongitude() : existingService.getLongitude());

        ServiceItem updatedService = serviceItemRepository.save(existingService);
        log.info("Service with id {} updated successfully", id);
        return serviceItemsMapper.entityToDto(updatedService, null);
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

    private Pageable getPageable(int page, int size, String sortBy, String sortDir, boolean isNative) {
        page = Math.max(page, 0);
        size = size > 0 ? size : 10;

        // Normalize sort direction
        boolean asc = !"desc".equalsIgnoreCase(sortDir);

        // Validate and map sort field
        SortFields sortField = validateSort(sortBy);
        String sortProperty = isNative ? sortField.toNativeColumn() : sortField.getField();

        // Create Sort object
        Sort sort = asc ? Sort.by(sortProperty).ascending() : Sort.by(sortProperty).descending();

        return PageRequest.of(page, size, sort);
    }
}
