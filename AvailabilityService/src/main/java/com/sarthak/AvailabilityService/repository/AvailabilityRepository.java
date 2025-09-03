package com.sarthak.AvailabilityService.repository;

import com.sarthak.AvailabilityService.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    Optional<Availability> findByServiceProviderId();
}
