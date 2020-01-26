package com.bioauthorization.proxy.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Contains data of user bioauthorization service identifier and generated totp seed value.
 */
@Getter
@AllArgsConstructor
public class RegistrationResponse {

    private String userBioId;

    private String totpSeed;
}
