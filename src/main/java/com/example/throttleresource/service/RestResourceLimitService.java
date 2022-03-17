package com.example.throttleresource.service;

import com.example.throttleresource.config.ThrottleConfig;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestResourceLimitService {

    private final ThrottleConfig throttleConfig;

    private LoadingCache<String, AtomicInteger> requestCountsPerIpAddress;

    @PostConstruct
    public void init() {
        requestCountsPerIpAddress = Caffeine
                .newBuilder()
                .expireAfterWrite(throttleConfig.getDuration().toMillis(), TimeUnit.MILLISECONDS)
                .build(key -> new AtomicInteger());
    }

    public boolean isTooManyRequests(final HttpServletRequest request) {
        final String clientIp = request.getRemoteAddr();
        final int requestsCount = requestCountsPerIpAddress.get(clientIp).incrementAndGet();
        final boolean tooMany = requestsCount > throttleConfig.getLimit();
        if (log.isDebugEnabled()) {
            log.debug("Client IP {}\t requests count {}\t {}", clientIp, requestsCount, tooMany);
        }
        return tooMany;
    }
}
