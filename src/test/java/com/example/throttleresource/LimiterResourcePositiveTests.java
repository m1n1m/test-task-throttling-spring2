package com.example.throttleresource;

import com.example.throttleresource.rest.resource.LimitedResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext
class LimiterResourcePositiveTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    public void oneClientGetOk() {
        mockMvc.perform(get(LimitedResource.RESOURCE_URI))
               .andExpect(status().isOk());
    }
}
