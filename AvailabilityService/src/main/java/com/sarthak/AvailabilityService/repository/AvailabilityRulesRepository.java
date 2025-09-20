package com.sarthak.AvailabilityService.repository;

import com.sarthak.AvailabilityService.model.AvailabilityRules;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRulesRepository extends JpaRepository<AvailabilityRules, Long> {

    @Query(value = "SELECT * FROM availability_rules ar"+
            " WHERE (ar.days_of_week & (1 << (:day_of_week % 7))) != 0" +
            " AND (:start_time IS NULL OR ar.start_time <= :start_time)" +
            " AND (:end_time IS NULL OR ar.end_time >= :end_time)",
            countQuery = "SELECT COUNT(*) FROM availability_rules ar"+
                    " WHERE (ar.days_of_week & (1 << (:day_of_week % 7))) != 0" +
                    " AND (:start_time IS NULL OR ar.start_time <= :start_time)" +
                    " AND (:end_time IS NULL OR ar.end_time >= :end_time)",
            nativeQuery = true
    )
    Page<AvailabilityRules> findAvailableOnDayAndTime(
            @Param("day_of_week") int dayOfWeek,
            @Param("start_time") LocalTime startTime,
            @Param("end_time") LocalTime endTime,
            Pageable pageable
    );

    List<AvailabilityRules> findAllByServiceProviderIdAndServiceId(Long serviceProviderId, Long serviceId);

    Optional<AvailabilityRules> findByServiceProviderIdAndServiceIdAndStartTimeAndEndTime(@NotNull Long serviceProviderId,
                                                                               @NotNull Long serviceId,
                                                                               @NotNull LocalTime startTime,
                                                                               @NotNull LocalTime endTime);

    List<AvailabilityRules> findAllByServiceProviderId(Long serviceProviderId);

    Optional<AvailabilityRules> findByServiceId(Long serviceId);

    @Query("""
                SELECT availability FROM AvailabilityRules availability
                WHERE availability.serviceProviderId = :serviceProviderId
                  AND availability.serviceId = :serviceId
                  AND (availability.daysOfWeek & (1 << (:dayOfWeek % 7))) != 0
                  ORDER BY availability.startTime ASC
                """)
    List<AvailabilityRules> findByServiceProviderAndServiceAndDayOrdered(@Param("serviceProviderId") Long serviceProviderId,
                                                                                @Param("serviceId") Long serviceId,
                                                                                @Param("dayOfWeek") byte day);
}
