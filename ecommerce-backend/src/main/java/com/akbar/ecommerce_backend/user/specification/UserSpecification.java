package com.akbar.ecommerce_backend.user.specification;

import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<UserEntity> hasStatus(UserStatus status){

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }


    public static Specification<UserEntity> hasKeyword(String keyword) {

        return (root, query, criteriaBuilder) -> {

            String searchKeyword = "%" + keyword.toLowerCase() + "%";

            Predicate firstNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")), searchKeyword
            );


            Predicate lastNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")), searchKeyword
            );


            Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), searchKeyword
            );

            Predicate phoneNumberPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("phoneNumber")), searchKeyword
            );

            return criteriaBuilder.or(
                    firstNamePredicate,
                    lastNamePredicate,
                    emailPredicate,
                    phoneNumberPredicate);
        };

    }

    public static Specification<UserEntity> isNotDeleted(){

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(
                        root.get("status"),
                        UserStatus.DELETED);

    }

}
