package com.bioauthorization.proxy.service;

import com.bioauthorization.proxy.api.rest.model.AdditionalRegistrationData;
import com.bioauthorization.proxy.api.rest.model.RegistrationResponse;
import com.bioauthorization.proxy.database.model.User;
import com.bioauthorization.proxy.database.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

@Slf4j
@Service
public class BaseRegistrationService implements RegistrationService {
    private UserRepository userRepository;

    public BaseRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegistrationResponse registerUser(String idpUserId) {
        User savedUser = userRepository.save(User.builder()
                .idpId(idpUserId)
                .seed(Base32.random())
                .build()
        );
        return new RegistrationResponse(savedUser.getId(), savedUser.getSeed());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String registerUserPhone(String userId, MultipartFile publicKey, AdditionalRegistrationData data) {
        Optional<User> optionalUser = userRepository.findById(userId);

        optionalUser.ifPresentOrElse(
                user -> {
                    Totp totp = new Totp(user.getSeed());
                    if (!totp.verify(data.getTotpValue())) throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
                    user.setRsaKey(checkRSAPublicKeyAndRetrieve(publicKey));
                    user.setPushNotificationData(data.getPushNotificationData());

                    userRepository.save(user);
                },
                () -> {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User not found");
                }
        );
        return "ok";
    }

    private byte[] checkRSAPublicKeyAndRetrieve(MultipartFile publicKeyData) {
        byte [] rsaKeybytes;
        try {
            PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyData.getBytes()));
            rsaKeybytes = pk.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new HttpClientErrorException(HttpStatus.NOT_IMPLEMENTED);
        } catch (IOException | InvalidKeySpecException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "RSA key not viable");
        }

        return rsaKeybytes;
    }
}
