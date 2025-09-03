package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.exception.ServiceProviderNotAvailableException;
import com.sarthak.AvailabilityService.mapper.AvailabilityMapper;
import com.sarthak.AvailabilityService.model.Availability;
import com.sarthak.AvailabilityService.repository.AvailabilityRepository;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final AvailabilityMapper availabilityMapper;

    // REQUIRES BOOKING SERVICE TO CHECK FOR DOUBLE BOOKING, WILL IMPLEMENT LATER

    public AvailabilityService(AvailabilityRepository availabilityRepository, AvailabilityMapper availabilityMapper) {
        this.availabilityRepository = availabilityRepository;
        this.availabilityMapper = availabilityMapper;
    }

    public String checkProviderAvailability(Long serviceProviderId, String date) {
        Availability availability = availabilityRepository.findByServiceProviderId()
                .orElseThrow(() -> new ServiceProviderNotAvailableException("Service provider is not available in given slot" + serviceProviderId));

        Boolean isDateValid = availability.getAvailabilityDate().toString().equals(date);
        Boolean isProviderIdValid = availability.getServiceProviderId().equals(serviceProviderId);


        return "Not implemented";
    }


}
