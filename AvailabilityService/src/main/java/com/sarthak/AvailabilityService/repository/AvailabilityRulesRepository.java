package com.sarthak.AvailabilityService.repository;

import com.sarthak.AvailabilityService.model.AvailabilityRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface AvailabilityRulesRepository extends JpaRepository<AvailabilityRules, Long> {

    List<AvailabilityRules> findByServiceProviderIdAndDayOfWeek(Long serviceProviderId, DayOfWeek day);
}
