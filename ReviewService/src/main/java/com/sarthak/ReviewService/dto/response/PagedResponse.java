package com.sarthak.ReviewService.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedResponse <T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLastPage
) {}
