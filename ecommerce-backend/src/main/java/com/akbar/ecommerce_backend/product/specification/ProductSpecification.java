package com.akbar.ecommerce_backend.product.specification;

import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<ProductEntity> hasStatus(ProductStatus status){

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }



    public static Specification<ProductEntity> hasKeyword(String keyword) {

        return (root, query, criteriaBuilder) -> {

            String searchKeyword = "%" + keyword.toLowerCase() + "%";

            Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), searchKeyword
            );

            Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchKeyword
            );

            Predicate skuPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("sku")), searchKeyword
            );

            return criteriaBuilder.or(
                    namePredicate,
                    descriptionPredicate,
                    skuPredicate
            );
        };
    }



    public static Specification<ProductEntity> isNotDeleted(){

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(
                        root.get("status"),
                        ProductStatus.DELETED);
    }



}
