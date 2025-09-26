package com.sarthak.ServiceListingService.repository;

import com.sarthak.ServiceListingService.model.ServiceItem;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    Optional<ServiceItem> findByServiceNameAndServiceProviderId(@NotNull String serviceName, @NotNull Long serviceProviderId);

    Page<ServiceItem> findAllByServiceProviderId(@NotNull Long providerId, Pageable pageable);

    Page<ServiceItem> findAllByServiceCategoryIgnoreCase(String category, Pageable pageable);
    
    Page<ServiceItem> findAllByServicePricePerHourBetween(Double minPrice, Double maxPrice, Pageable pageable);
    
    
    @Query(value = """
            SELECT * FROM service_items s
            WHERE (6371 * acos(
                cos(radians(:userLat)) * cos(radians(s.latitude)) *
                cos(radians(s.longitude) - radians(:userLong)) +
                sin(radians(:userLat)) * sin(radians(s.latitude))
            )) < :radius
            """,
           countQuery = """
            SELECT COUNT(*) FROM service_items s
            WHERE (6371 * acos(
                cos(radians(:userLat)) * cos(radians(s.latitude)) *
                cos(radians(s.longitude) - radians(:userLong)) +
                sin(radians(:userLat)) * sin(radians(s.latitude))
            )) < :radius
            """,
           nativeQuery = true
    )
    Page<ServiceItem> findNearbyServices(@Param("userLat") Double userLat,
                                         @Param("userLong") Double userLong,
                                         @Param("radius") Double radiusInKm, 
                                         Pageable pageable);


}
