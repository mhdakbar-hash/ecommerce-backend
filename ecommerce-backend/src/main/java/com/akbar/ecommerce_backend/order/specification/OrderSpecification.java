package com.akbar.ecommerce_backend.order.specification;

import com.akbar.ecommerce_backend.order.entity.OrderEntity;
import com.akbar.ecommerce_backend.order.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<OrderEntity> hasStatus(OrderStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }


    public static Specification<OrderEntity> hasOrderNumber(String orderNumber) {

        return (root, query, criteriaBuilder) -> {

            String searchOrderNumber = "%" + orderNumber.toLowerCase() + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("orderNumber")),
                    searchOrderNumber);
        };
    }


    public static Specification<OrderEntity> hasUserEmail(String userEmail) {

        return (root, query, criteriaBuilder) -> {

            String searchEmail = "%" + userEmail.toLowerCase() + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("email")),
                    searchEmail);
        };
    }


    public static Specification<OrderEntity> createdAtGreaterThanOrEqualTo(LocalDateTime startDate) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        startDate);
    }


    public static Specification<OrderEntity> createdAtLessThanOrEqualTo(LocalDateTime endDate) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        endDate);
    }

}