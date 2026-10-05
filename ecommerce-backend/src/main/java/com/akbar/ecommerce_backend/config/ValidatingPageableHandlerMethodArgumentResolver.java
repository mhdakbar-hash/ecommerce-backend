package com.akbar.ecommerce_backend.config;

import com.akbar.ecommerce_backend.exception.InvalidPaginationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.core.MethodParameter;

public class ValidatingPageableHandlerMethodArgumentResolver
        extends PageableHandlerMethodArgumentResolver {

    private static final int MAX_PAGE_SIZE = 100;

    @Override
    @Nullable
    public Pageable resolveArgument(
            MethodParameter methodParameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) {

        validatePageParameter(webRequest);
        validateSizeParameter(webRequest);

        return super.resolveArgument(
                methodParameter,
                mavContainer,
                webRequest,
                binderFactory
        );
    }

    private void validatePageParameter(NativeWebRequest webRequest) {
        String pageParameter = webRequest.getParameter("page");

        if (pageParameter == null) {
            return;
        }

        try {
            int page = Integer.parseInt(pageParameter);

            if (page < 0) {
                throw new InvalidPaginationException(
                        "Page number must be greater than or equal to 0."
                );
            }

        } catch (NumberFormatException exception) {
            throw new InvalidPaginationException(
                    "Page number must be a valid integer."
            );
        }
    }

    private void validateSizeParameter(NativeWebRequest webRequest) {
        String sizeParameter = webRequest.getParameter("size");

        if (sizeParameter == null) {
            return;
        }

        try {
            int size = Integer.parseInt(sizeParameter);

            if (size <= 0) {
                throw new InvalidPaginationException(
                        "Page size must be greater than 0."
                );
            }

            if (size > MAX_PAGE_SIZE) {
                throw new InvalidPaginationException(
                        "Page size cannot exceed " + MAX_PAGE_SIZE + "."
                );
            }

        } catch (NumberFormatException exception) {
            throw new InvalidPaginationException(
                    "Page size must be a valid integer."
            );
        }
    }
}