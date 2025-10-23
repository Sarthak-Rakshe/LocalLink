package com.sarthak.UserService.client;

import com.sarthak.UserService.dto.ProviderReviewAggregateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@FeignClient("REVIEW-SERVICE")
public interface ReviewServiceClient {

    @PostMapping("api/reviews/providers/aggregate")
    Map<Long, ProviderReviewAggregateResponse> getProviderReviewAggregates(List<Long> providerIds);
}
