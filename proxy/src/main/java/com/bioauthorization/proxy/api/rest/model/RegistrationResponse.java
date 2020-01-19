package com.bioauthorization.proxy.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrationResponse {

    private String userBioId;

    private String totpSeed;
}
