package com.example.throttleresource.rest.resource;

import com.example.throttleresource.service.RestResourceLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(LimitedResource.RESOURCE_URI)
@RequiredArgsConstructor
public class LimitedResource {

    public static final String RESOURCE_URI = "/api/v1/limited-resource";

    private final RestResourceLimitService restResourceLimitService;

    @GetMapping
    public ResponseEntity<Void> get(final HttpServletRequest request) {
        if (restResourceLimitService.isTooManyRequests(request)) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
        return ResponseEntity.ok().build();
    }
}
