package com.sarthak.PaymentService.repository.specification;

import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.model.Transaction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

public class TransactionSpecification {
    public static Specification<Transaction> hasPaymentStatus(PaymentStatus status){
        return ((root, query, criteriaBuilder) -> status == null
                ? null : criteriaBuilder.equal(root.get("paymentStatus"), status));
    }

    public static Specification<Transaction> hasPaymentMethod(PaymentMethod method){
        return ((root, query, criteriaBuilder) -> method == null
                ? null : criteriaBuilder.equal(root.get("paymentMethod"), method));
    }

    public static Specification<Transaction> resolveByUserType(String userType, Long userId){
        return ((root, query, criteriaBuilder) -> {
            if(userType == null || userId == null){
                throw new AccessDeniedException("Invalid access: User type or User ID is null");
            }
            return switch (userType.toUpperCase()) {
                case "CUSTOMER" -> criteriaBuilder.equal(root.get("customerId"), userId);
                case "PROVIDER" -> criteriaBuilder.equal(root.get("serviceProviderId"), userId);
                default -> throw  new AccessDeniedException("Invalid access: Unknown user type " + userType);
            };
        });
    }
}
