package com.example.throttleresource.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "throttle")
public class ThrottleConfig {

    private Integer limit = 1000;

    private Duration duration = Duration.parse("PT1M");
}
