package com.example.throttleresource.service;

import com.example.throttleresource.config.ThrottleConfig;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
        final Duration duration = throttleConfig.getDuration();
        requestCountsPerIpAddress = Caffeine
                .newBuilder()
                .expireAfterWrite(duration.toMillis(), TimeUnit.MILLISECONDS)
                .build(key -> new AtomicInteger());
    }

    public boolean isTooManyRequests(final HttpServletRequest request) {
        final String clientIp = request.getRemoteAddr();
        final AtomicInteger requestsCountAtomic = requestCountsPerIpAddress.get(clientIp);
        final int requestsCount = requestsCountAtomic.incrementAndGet();
        final boolean tooMany = requestsCount > throttleConfig.getLimit();
        if (log.isDebugEnabled()) {
            log.debug("Client IP {}\t requests count {}, too many {}", clientIp, requestsCount, tooMany);
        }
        return tooMany;
    }
}
