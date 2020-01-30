package com.bioauthorization.proxy.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Contains data of user bioauthorization service identifier and generated totp seed value.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private String userBioId;

    private String totpSeed;
}
