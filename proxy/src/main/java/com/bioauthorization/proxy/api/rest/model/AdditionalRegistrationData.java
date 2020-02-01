package com.bioauthorization.proxy.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Contains generated totp value and push notification data
 */
@Data
@AllArgsConstructor
public class AdditionalRegistrationData {

    private String totpValue;

    private String pushNotificationData;
}
