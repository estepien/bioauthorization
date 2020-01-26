package com.bioauthorization.proxy.api.rest;

import com.bioauthorization.proxy.api.rest.model.AdditionalRegistrationData;
import com.bioauthorization.proxy.api.rest.model.RegistrationResponse;
import com.bioauthorization.proxy.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class Registration {
    private RegistrationService registrationService;

    public Registration(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping(value = "/r", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationResponse startRegistration(@RequestParam String userId) {
        log.info("Registration request occurred for IDP user with id: " + userId);


        return registrationService.registerUser(userId);
    }

    @PutMapping(value = "/r/{userId}/p", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String registerUserPhone(
            @PathVariable String userId,
            @RequestPart(value = "pk") MultipartFile publicKey,
            @RequestPart("additional") AdditionalRegistrationData data
            ) {
        log.info("Registration user phone request occurred for Bio user with id: " + userId);
        return registrationService.registerUserPhone(userId, publicKey, data);
    }
}
