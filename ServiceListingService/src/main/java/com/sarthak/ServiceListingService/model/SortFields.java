package com.sarthak.ServiceListingService.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum SortFields {
    ID("serviceId"),
    NAME("serviceName"),
    PROVIDER("serviceProviderId"),
    CATEGORY("serviceCategory"),
    PRICE("servicePricePerHour");

    private final String field;

    SortFields(String field) {
        this.field = field;
    }

    public String toNativeColumn() {
        return switch (this) {
            case ID -> "service_id";
            case NAME -> "service_name";
            case PROVIDER -> "service_provider_id";
            case CATEGORY -> "service_category";
            case PRICE -> "service_price_per_hour";
            default -> "service_name";
        };
    }


    public static SortFields fromString(String field) {
        if (field == null) {
            log.warn("Invalid sort field: null. Defaulting to NAME");
            return NAME;
        }

        String normalized = field.trim().toLowerCase();
        // Accept common aliases and snake_case inputs from API/clients
        switch (normalized) {
            case "id":
            case "serviceid":
            case "service_id":
                return ID;
            case "name":
            case "servicename":
            case "service_name":
                return NAME;
            case "provider":
            case "providerid":
            case "serviceproviderid":
            case "service_provider_id":
                return PROVIDER;
            case "category":
            case "servicecategory":
            case "service_category":
                return CATEGORY;
            case "price":
            case "priceperhour":
            case "servicepriceperhour":
            case "service_price_per_hour":
                return PRICE;
            default:
                // If the caller already passed a valid entity property name, try to match directly
                for (SortFields sortField : SortFields.values()) {
                    if (sortField.field.equalsIgnoreCase(field)) {
                        return sortField;
                    }
                }
                log.warn("Invalid sort field: {}. Defaulting to NAME", field);
                return NAME; // default sort field
        }
    }
}
