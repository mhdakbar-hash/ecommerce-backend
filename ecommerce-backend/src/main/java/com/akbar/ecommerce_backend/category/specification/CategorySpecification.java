package com.akbar.ecommerce_backend.category.specification;

import com.akbar.ecommerce_backend.category.entity.CategoryEntity;
import com.akbar.ecommerce_backend.category.enums.CategoryStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {

    public static Specification<CategoryEntity> hasKeyword(String keyword) {

        return (root, query, criteriaBuilder) -> {

            String searchKeyword = "%" + keyword.toLowerCase() + "%";

            Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), searchKeyword);

            Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchKeyword);

            return criteriaBuilder.or(namePredicate, descriptionPredicate);
        };
    }



    public static Specification<CategoryEntity> hasStatus(CategoryStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }



    public static Specification<CategoryEntity> isNotDeleted() {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("status"), CategoryStatus.DELETED);
    }


}
