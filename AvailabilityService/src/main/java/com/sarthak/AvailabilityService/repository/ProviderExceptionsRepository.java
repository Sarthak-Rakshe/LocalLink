package com.sarthak.AvailabilityService.repository;

import com.sarthak.AvailabilityService.model.ProviderExceptions;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderExceptionsRepository extends JpaRepository<ProviderExceptions, Long> {

    List<ProviderExceptions> findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(Long serviceProviderId, LocalDate date);

    Optional<ProviderExceptions> findAllByServiceProviderIdAndServiceIdAndExceptionDateAndNewStartTimeAndNewEndTime(@NotNull Long serviceProviderId, 
                                                                                                                    @NotNull Long serviceId, 
                                                                                                                    @NotNull LocalDate exceptionDate, 
                                                                                                                    @NotNull LocalTime newStartTime, 
                                                                                                                    @NotNull LocalTime newEndTime);

    List<ProviderExceptions> findAllByServiceProviderId(Long serviceProviderId);

    List<ProviderExceptions> findAllByServiceProviderIdAndServiceId(Long serviceProviderId, Long serviceId);
}
