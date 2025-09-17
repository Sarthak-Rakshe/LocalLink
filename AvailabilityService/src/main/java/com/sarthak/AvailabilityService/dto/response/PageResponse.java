package com.sarthak.AvailabilityService.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        long totalElements,
        int totalPages,
        int size
) {
}
