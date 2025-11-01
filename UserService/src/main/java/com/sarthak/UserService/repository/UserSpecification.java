package com.sarthak.UserService.repository;

import com.sarthak.UserService.dto.QueryFilter;
import com.sarthak.UserService.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasUsername(String username) {
        return (root, query, criteriaBuilder) -> username == null
                ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + username.toLowerCase() + "%");
    }

    public static Specification<User> hasUserEmail(String userEmail) {
        return (root, query, criteriaBuilder) -> userEmail == null
                ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("userEmail")), "%" + userEmail.toLowerCase() + "%");
    }

    public static Specification<User> restrictByTypeAndRoleAndActive(String userType, String userRole) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("userType"), userType),
                cb.equal(root.get("userRole"), userRole),
                cb.isTrue(root.get("isActive"))
        );
    }

    public static Specification<User> buildSpecification(QueryFilter queryFilter) {
        Specification<User> spec = restrictByTypeAndRoleAndActive("PROVIDER", "USER");

        if (queryFilter == null) {
            return spec;
        }
        if(queryFilter.providerName() != null){
            spec = spec.and(UserSpecification.hasUsername(queryFilter.providerName()));
        }
        if(queryFilter.providerEmail() != null){
            spec = spec.and(UserSpecification.hasUserEmail(queryFilter.providerEmail()));
        }
        return spec;
    }
}
