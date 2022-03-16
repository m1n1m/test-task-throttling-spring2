package com.example.throttleresource;

import com.example.throttleresource.rest.resource.LimitedResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "throttle.limit=" + LimiterResourceTests.REQUESTS_LIMIT,
        "throttle.duration=" + LimiterResourceTests.DURATION_STR
})
class LimiterResourceTests {

    @Autowired
    private MockMvc mockMvc;

    static final int REQUESTS_LIMIT = 10;
    static final String DURATION_STR = "PT1S";

    @DisplayName("Проверка того что один клиент получает ответ 200 и при превышении "
                 + "количества запросов 502")
    @Test
    public void oneClientGeneratesTooManyRequests() {
      performAndAssertRequests("127.0.0.1");
    }

    @DisplayName("Проверка работы ограничений при параллелизме")
    @Test
    @SneakyThrows
    public void manyClientsGeneratesTooManyRequests() {
        final int CLIENTS_COUNT = 10;
        final ExecutorService es = Executors.newFixedThreadPool(CLIENTS_COUNT);

        for (int i = 0; i < CLIENTS_COUNT; i++) {
            final int finalI = i;
            es.execute(() -> {
                performAndAssertRequests(String.format("127.0.1.%d", finalI + 1));
            });
        }
        es.shutdown();

        final boolean finished = es.awaitTermination(1, TimeUnit.MINUTES);
        Assertions.assertTrue(finished, "All clients finished requests");
    }

    @DisplayName("Проверка того что один клиент получает ответ 502, "
                 + "а затем после таймаута опять может выполнить запрос")
    @Test
    @SneakyThrows
    public void checkLimitResetAfterTimeout() {
        final String clientAddress = "127.0.0.32";
        performAndAssertRequests(clientAddress);

        final Duration duration = Duration.parse(DURATION_STR);
        Thread.sleep(duration.toMillis() + 10); // погрешность
        final MockHttpServletRequestBuilder requestBuilder = get(LimitedResource.RESOURCE_URI)
                .with(remoteHost(clientAddress));
        mockMvc.perform(requestBuilder).andExpect(status().isOk());
    }

    @SneakyThrows
    private void performAndAssertRequests(final String clientAddress) {
        final MockHttpServletRequestBuilder requestBuilder = get(LimitedResource.RESOURCE_URI)
                .with(remoteHost(clientAddress));

        for (int i = 0; i < REQUESTS_LIMIT; i++) {
            mockMvc.perform(requestBuilder).andExpect(status().isOk());
        }
        mockMvc.perform(requestBuilder).andExpect(status().isBadGateway());
    }

    private RequestPostProcessor remoteHost(final String remoteHost) {
        return request -> {
            request.setRemoteAddr(remoteHost);
            return request;
        };
    }
}
