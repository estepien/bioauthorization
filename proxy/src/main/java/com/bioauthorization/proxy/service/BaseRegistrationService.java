package com.bioauthorization.proxy.service;

import com.bioauthorization.proxy.api.rest.model.AdditionalRegistrationData;
import com.bioauthorization.proxy.api.rest.model.RegistrationResponse;
import com.bioauthorization.proxy.database.model.User;
import com.bioauthorization.proxy.database.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BaseRegistrationService implements RegistrationService {
    private UserRepository userRepository;

    public BaseRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public RegistrationResponse registerUser(String idpUserId) {
        User savedUser = userRepository.save(User.builder()
                .idpId(idpUserId)
                .build()
        );
        return new RegistrationResponse(savedUser.getId(), "test");
    }

    @Override
    public String registerUserPhone(String userId, MultipartFile publicKey, AdditionalRegistrationData data) {


        return "OK";
    }
}
