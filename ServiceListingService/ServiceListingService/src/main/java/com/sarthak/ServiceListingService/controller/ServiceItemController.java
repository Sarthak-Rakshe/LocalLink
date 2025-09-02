package com.sarthak.ServiceListingService.controller;

import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.service.ServiceItemsService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping()
    public ResponseEntity<ServiceItemDto> createService(@RequestBody ServiceItemDto serviceItemDto){
        ServiceItemDto createdService = serviceItemsService.createService(serviceItemDto);
        return ResponseEntity.status(201).body(createdService);
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteService(@RequestParam Long id){
        String response = serviceItemsService.deleteService(id);
        return ResponseEntity.ok(response);
    }
}
