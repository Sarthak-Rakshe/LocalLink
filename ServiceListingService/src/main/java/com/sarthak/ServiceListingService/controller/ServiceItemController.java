package com.sarthak.ServiceListingService.controller;

import com.sarthak.ServiceListingService.config.shared.UserPrincipal;
import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.dto.response.PagedResponse;
import com.sarthak.ServiceListingService.service.ServiceItemsService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
public class ServiceItemController {

    private final ServiceItemsService serviceItemsService;

    public ServiceItemController(ServiceItemsService serviceItemsService) {
        this.serviceItemsService = serviceItemsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceItemDto> getById(@PathVariable Long id){
        ServiceItemDto serviceItemDto = serviceItemsService.getServiceById(id);
        return ResponseEntity.ok(serviceItemDto);
    }

    @GetMapping()
    public PagedResponse<ServiceItemDto> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        var servicesPage = serviceItemsService.getAllServices(page, size, sortBy, sortDir);
        return new PagedResponse<>(
                servicesPage.getContent(),
                servicesPage.getNumber(),
                servicesPage.getSize(),
                servicesPage.getTotalElements(),
                servicesPage.getTotalPages()
        );
    }

    @GetMapping("/provider/{providerId}")
    public PagedResponse<ServiceItemDto> getServicesByProviderId(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        var servicesPage = serviceItemsService.getServicesByProviderId(providerId, page, size, sortBy, sortDir);
        return new PagedResponse<>(
                servicesPage.getContent(),
                servicesPage.getNumber(),
                servicesPage.getSize(),
                servicesPage.getTotalElements(),
                servicesPage.getTotalPages()
        );
    }

    @GetMapping("/category/{category}")
    public PagedResponse<ServiceItemDto> getServicesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        var servicesPage = serviceItemsService.getServicesByCategory(category, page, size, sortBy, sortDir);
        return new PagedResponse<>(
                servicesPage.getContent(),
                servicesPage.getNumber(),
                servicesPage.getSize(),
                servicesPage.getTotalElements(),
                servicesPage.getTotalPages()
        );
    }

    @GetMapping("/nearby")
    public PagedResponse<ServiceItemDto> getNearbyServices(
            @RequestParam Double userLatitude,
            @RequestParam Double userLongitude,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        var servicesPage = serviceItemsService.getNearbyService(userLatitude, userLongitude, page, size, sortBy, sortDir);
        return new PagedResponse<>(
                servicesPage.getContent(),
                servicesPage.getNumber(),
                servicesPage.getSize(),
                servicesPage.getTotalElements(),
                servicesPage.getTotalPages()
        );
    }

    @PostMapping()
    @PreAuthorize("principal.userType.equals('PROVIDER')")
    public ResponseEntity<ServiceItemDto> addService(@RequestBody ServiceItemDto serviceItemDto){
        ServiceItemDto saved = serviceItemsService.createService(serviceItemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{serviceId}")
    @PreAuthorize("principal.userType.equals('PROVIDER')")
    public ResponseEntity<ServiceItemDto> updateService(@PathVariable Long serviceId,
                                                        @RequestBody ServiceItemDto serviceItemDto,
                                                        Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ServiceItemDto updated = serviceItemsService.updateService(serviceId, serviceItemDto, userPrincipal.getUserId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("principal.userType.equals('PROVIDER')")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        serviceItemsService.deleteService(serviceId, userPrincipal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
