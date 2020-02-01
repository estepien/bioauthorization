package com.bioauthorization.proxy.api.rest;

import com.bioauthorization.proxy.ProxyApplication;
import com.bioauthorization.proxy.api.rest.model.AdditionalRegistrationData;
import com.bioauthorization.proxy.api.rest.model.RegistrationResponse;
import com.bioauthorization.proxy.database.model.User;
import com.bioauthorization.proxy.database.repository.UserRepository;
import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest(classes = ProxyApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "/application.yml")
class RegistrationControllerE2eTest {
    public static final String TEST_PUSH_NOTIFICATION_DATA = "testPushNotificationData";
    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper om = new ObjectMapper();

    @Container
    public static GenericContainer mongo = new GenericContainer("mongo:latest")
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example")
            .withEnv("MONGO_INITDB_DATABASE", "proxy");

    @BeforeEach
    void setUp() {
        mongo.start();
        System.setProperty("spring.data.mongodb.port", String.valueOf(mongo.getMappedPort(27017)));
    }

    @Test
    public void fullRegistrationTest() throws Exception {
        //given-when
        MvcResult result = mvc.perform(
                post("/r")
                        .param("userId", "testIdpId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        RegistrationResponse response = om.readValue(result.getResponse().getContentAsString(), RegistrationResponse.class);

        //Phone behaviour
        KeyPair keys = generateKeys();
        Totp totp = new Totp(response.getTotpSeed());

        AdditionalRegistrationData data = new AdditionalRegistrationData(
                Base64.getEncoder().encodeToString(
                        encrypt(totp.now(), keys.getPrivate())
                ),
                TEST_PUSH_NOTIFICATION_DATA
        );

        MockMultipartFile dataJsonPart = new MockMultipartFile("additional", null, MediaType.APPLICATION_JSON_VALUE, om.writeValueAsBytes(data));

        mvc.perform(
                multipart("/r/{userId}/p", response.getUserBioId())
                        .file("pk", Base64.getEncoder().encode(keys.getPublic().getEncoded()))
                        .file(dataJsonPart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());

        //then
        Optional<User> optionalUser = userRepository.findById(response.getUserBioId());
        assertTrue(optionalUser.isPresent());

        User resultUser = optionalUser.get();
        assertAll("databaseChecks",
                () -> assertEquals(resultUser.getPushNotificationData(), TEST_PUSH_NOTIFICATION_DATA),
                () -> assertEquals(((RSAPublicKey) retrievePubKeyFromBytes(resultUser.getRsaKey())).getPublicExponent(), ((RSAPublicKey) keys.getPublic()).getPublicExponent()),
                () -> assertEquals(resultUser.getSeed(), response.getTotpSeed())
        );
    }

    private PublicKey retrievePubKeyFromBytes(byte[] rsaKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rsaKey));
    }

    private KeyPair generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator kgp = KeyPairGenerator.getInstance("RSA");
        kgp.initialize(2048);
        return kgp.generateKeyPair();
    }

    public static byte[] encrypt(String data, Key privateKey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data.getBytes());
    }

    @AfterEach
    void cleanAfter() {
        userRepository.deleteAll();
    }
}