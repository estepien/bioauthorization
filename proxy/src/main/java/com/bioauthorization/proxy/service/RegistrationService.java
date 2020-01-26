package com.bioauthorization.proxy.service;

import com.bioauthorization.proxy.api.rest.model.AdditionalRegistrationData;
import com.bioauthorization.proxy.api.rest.model.RegistrationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface RegistrationService {
    /**
     * Registers idp user id, generates bioauthorization service identifier and totp seed value
     *
     * @param idpUserId User identifier value from identity provider
     * @return {@link RegistrationResponse}
     */
    RegistrationResponse registerUser(String idpUserId);

    /**
     * Registers user phone with rsa public key generated on this phone.
     * Validate totp value enciphered with rsa key.
     * Saves push notification data.
     *
     * @param userId User identifier value from bioauthorization service
     * @param publicKey public rsa key
     * @param data {@link AdditionalRegistrationData}
     * @return {@link String} status value
     */
    String registerUserPhone(String userId, MultipartFile publicKey, AdditionalRegistrationData data);
}
