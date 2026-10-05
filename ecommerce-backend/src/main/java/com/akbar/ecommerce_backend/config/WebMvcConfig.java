package com.akbar.ecommerce_backend.config;

import com.akbar.ecommerce_backend.security.RateLimitInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    private static final int MAX_PAGE_SIZE = 100;


    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/users/register"
                );
    }



    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

        ValidatingPageableHandlerMethodArgumentResolver resolver =
                new ValidatingPageableHandlerMethodArgumentResolver();

        resolver.setMaxPageSize(MAX_PAGE_SIZE);

        resolvers.add(resolver);
    }

}