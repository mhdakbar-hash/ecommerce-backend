package com.akbar.ecommerce_backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Value("${app.rate-limit.login.max-requests}")
    private int loginMaxRequests;

    @Value("${app.rate-limit.login.window}")
    private Duration loginWindow;


    @Value("${app.rate-limit.refresh.max-requests}")
    private int refreshMaxRequests;

    @Value("${app.rate-limit.refresh.window}")
    private Duration refreshWindow;


    @Value("${app.rate-limit.register.max-requests}")
    private int registerMaxRequests;

    @Value("${app.rate-limit.register.window}")
    private Duration registerWindow;


    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }



    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        String clientIp = request.getRemoteAddr();
        String requestURI = request.getRequestURI();

        String apiName;
        int maxRequests;
        Duration window;


        switch (requestURI) {

            case "/api/v1/auth/login" -> {
                apiName = "login";
                maxRequests = loginMaxRequests;
                window = loginWindow;
            }

            case "/api/v1/auth/refresh" -> {
                apiName = "refresh";
                maxRequests = refreshMaxRequests;
                window = refreshWindow;
            }

            case "/api/v1/users/register" -> {
                apiName = "register";
                maxRequests = registerMaxRequests;
                window = registerWindow;
            }

            default -> {
                return true;
            }
        }


        String key = "rate-limit:" + apiName + ":" + clientIp;

        try {

            boolean allowed = rateLimitService.isAllowed(
                    key,
                    maxRequests,
                    window);


            if (!allowed) {

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        """
                        {
                            "status": 429,
                            "message": "Too many requests. Please try again later."
                        }
                        """
                );

                return false;
            }

            return true;

        } catch (DataAccessException | IllegalStateException ex) {

            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    """
                    {
                        "status": 503,
                        "message": "Rate limiting service is temporarily unavailable."
                    }
                    """
            );

            return false;
        }
    }
}