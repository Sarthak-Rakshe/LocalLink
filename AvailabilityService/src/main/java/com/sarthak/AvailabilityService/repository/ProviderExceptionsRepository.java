package com.sarthak.AvailabilityService.repository;

import com.sarthak.AvailabilityService.model.ProviderExceptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProviderExceptionsRepository extends JpaRepository<ProviderExceptions, Long> {

    List<ProviderExceptions> findAllByServiceProviderIdAndExceptionDate(Long serviceProviderId, LocalDate date);
}
