package com.sarthak.ServiceListingService.client;

import com.sarthak.ServiceListingService.dto.ReviewAggregateResponse;
import com.sarthak.ServiceListingService.dto.ReviewDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@FeignClient("REVIEW-SERVICE")
public interface ReviewServiceClient {

    @PostMapping("/api/reviews/getByServiceIds")
    public List<ReviewDto> getByServiceIds(List<Long> serviceIds);

    @PostMapping("/api/reviews/getAggregatesByServiceIds")
    public Map<Long, ReviewAggregateResponse> getAggregatesByServiceIds(List<Long> serviceIds);

}
