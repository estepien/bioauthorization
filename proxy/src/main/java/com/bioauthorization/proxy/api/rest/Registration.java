package com.bioauthorization.proxy.api.rest;

import com.bioauthorization.proxy.api.rest.model.RegistrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class Registration {

    @PostMapping(value = "/register")
    public RegistrationResponse startRegistration(@RequestParam String userId) {
        log.info("Registration request occurred for IDP user with id: " + userId);

        // TODO: Service which generates totp seed, and saves userId to database

        return new RegistrationResponse("testId", "testSeed");
    }
}
