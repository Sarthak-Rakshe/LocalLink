package com.sarthak.ServiceListingService.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum SortFields {
    NAME("service_name"),
    PROVIDER("service_provider_id"),
    CATEGORY("service_category"),
    PRICE("price_per_hour"),
    LOCATION("city");

    private final String field;

    SortFields(String field) {
        this.field = field;
    }

    public static SortFields fromString(String field) {
        for (SortFields sortField : SortFields.values()) {
            if (sortField.field.equalsIgnoreCase(field)) {
                return sortField;
            }
        }
        log.warn("Invalid sort field: {}. Defaulting to NAME", field);
        return NAME; // default sort field
    }
}
