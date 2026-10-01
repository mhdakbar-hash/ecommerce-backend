package com.akbar.ecommerce_backend.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();

        RATE_LIMIT_SCRIPT.setScriptText(
                "local current = redis.call('INCR', KEYS[1]) " +
                        "if current == 1 then " +
                        "    redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                        "end " +
                        "return current"
        );

        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }



    public boolean isAllowed(
            String key,
            int maxRequests,
            Duration window) {


        if (maxRequests <= 0 || window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Invalid rate limit configuration.");
        }

        Long currentCount = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(window.toSeconds()));


        if (currentCount == null) {
            throw new IllegalStateException("Unable to retrieve rate limit counter from Redis.");}

        return currentCount <= maxRequests;
    }

}