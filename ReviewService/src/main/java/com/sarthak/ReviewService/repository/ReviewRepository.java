package com.sarthak.ReviewService.repository;

import com.sarthak.ReviewService.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByCustomerIdAndServiceId(Long customerId, Long serviceId);

    Page<Review> findAllByServiceId(Long serviceId, Pageable pageable);

    Page<Review> findAllByCustomerId(Long customerId, Pageable pageable);
}
