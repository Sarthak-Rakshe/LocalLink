package com.sarthak.ServiceListingService.repository;

import com.sarthak.ServiceListingService.dto.QueryFilter;
import com.sarthak.ServiceListingService.model.ServiceItem;
import org.springframework.data.jpa.domain.Specification;

public class ServiceSpecification {

    public static Specification<ServiceItem> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> category == null
                ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("serviceCategory")),
                "%" + category.toLowerCase() + "%");
    }

    public static Specification<ServiceItem> hasServiceName(String serviceName) {
        return (root, query, criteriaBuilder) -> serviceName == null
                ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("serviceName")), "%" + serviceName.toLowerCase() + "%");
    }

    public static Specification<ServiceItem> hasPriceBetween(Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return null;
            } else if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("servicePricePerHour"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("servicePricePerHour"), minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("servicePricePerHour"), maxPrice);
            }
        };
    }

    public static Specification<ServiceItem> byServiceProviderId(Long serviceProviderId) {
        return (root, query, criteriaBuilder) -> serviceProviderId == null
                ? null : criteriaBuilder.equal(root.get("serviceProviderId"), serviceProviderId);
    }

    public static Specification<ServiceItem> byServiceIdInSet(java.util.Set<Long> serviceIdSet) {
        return (root, query, criteriaBuilder) -> {
            if (serviceIdSet == null || serviceIdSet.isEmpty()) {
                return null;
            }
            return root.get("serviceId").in(serviceIdSet);
        };
    }

    public static Specification<ServiceItem> buildSpecification(QueryFilter queryFilter) {
        Specification<ServiceItem> spec = null;
        if(queryFilter == null) {
            return null;
        }
        if(queryFilter.category() != null){
            spec = ServiceSpecification.hasCategory(queryFilter.category());
        }
        if(queryFilter.serviceName() != null){
            if(spec == null){
                spec = ServiceSpecification.hasServiceName(queryFilter.serviceName());
            }else{
                spec = spec.and(ServiceSpecification.hasServiceName(queryFilter.serviceName()));
            }
        }
        if(queryFilter.minPrice() != null || queryFilter.maxPrice() != null) {
            if(spec == null){
                spec = ServiceSpecification.hasPriceBetween(queryFilter.minPrice(), queryFilter.maxPrice());
            }else{
                spec = spec.and(ServiceSpecification.hasPriceBetween(queryFilter.minPrice(), queryFilter.maxPrice()));
            }
        }
        if(queryFilter.serviceProviderId() != null){
            if(spec == null){
                spec = ServiceSpecification.byServiceProviderId(queryFilter.serviceProviderId());
            }else{
                spec = spec.and(ServiceSpecification.byServiceProviderId(queryFilter.serviceProviderId()));
            }
        }
        if (queryFilter.serviceIdSet() != null && !queryFilter.serviceIdSet().isEmpty()) {
            if (spec == null) {
                spec = ServiceSpecification.byServiceIdInSet(queryFilter.serviceIdSet());
            } else {
                spec = spec.and(ServiceSpecification.byServiceIdInSet(queryFilter.serviceIdSet()));
            }
        }
        return spec;
    }
}
