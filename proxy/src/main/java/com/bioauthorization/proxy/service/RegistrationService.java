package com.bioauthorization.proxy.service;

import com.bioauthorization.proxy.api.rest.model.RegistrationResponse;

public interface RegistrationService {
    RegistrationResponse registerUser(String idpUserId);
}
