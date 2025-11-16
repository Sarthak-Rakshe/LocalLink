package com.sarthak.UserService.client;

import com.sarthak.UserService.dto.ProviderReviewAggregateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "REVIEW-SERVICE", url = "${feign.client.config.review-service.url}")
public interface ReviewServiceClient {

    @PostMapping("api/reviews/providers/aggregate")
    Map<Long, ProviderReviewAggregateResponse> getProviderReviewAggregates(List<Long> providerIds);
}
