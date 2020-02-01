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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
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
                    PublicKey userPublicKey = checkRSAPublicKeyAndRetrieve(publicKey);

                    validateTotpValue(user.getSeed(), userPublicKey, data.getTotpValue());

                    user.setRsaKey(userPublicKey.getEncoded());
                    user.setPushNotificationData(data.getPushNotificationData());

                    userRepository.save(user);
                },
                () -> {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User not found");
                }
        );
        return "ok";
    }

    private void validateTotpValue(String seed, PublicKey userPublicKey, String totpValue) {
        String totpDecryptedValue = decrypt(totpValue, userPublicKey);

        Totp totp = new Totp(seed);
        if (!totp.verify(totpDecryptedValue)) throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
    }

    public static String decrypt(String data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "decryption fail");
        }
    }

    private PublicKey checkRSAPublicKeyAndRetrieve(MultipartFile publicKeyData) {
        PublicKey pk;
        try {
            pk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyData.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            throw new HttpClientErrorException(HttpStatus.NOT_IMPLEMENTED);
        } catch (IOException | InvalidKeySpecException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "RSA key not viable");
        }

        return pk;
    }
}
