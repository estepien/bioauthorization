package com.bioauthorization.proxy.database.repository;

import com.bioauthorization.proxy.ProxyApplication;
import com.bioauthorization.proxy.database.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
        classes = ProxyApplication.class)
@TestPropertySource(locations = "/application.yml")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Container
    public static GenericContainer mongo = new GenericContainer("mongo:latest")
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example")
            .withEnv("MONGO_INITDB_DATABASE", "proxy");

    @BeforeAll
    static void setUp() {
        mongo.start();
        System.setProperty("spring.data.mongodb.port", String.valueOf(mongo.getMappedPort(27017)));
    }

    @Test
    public void checkSimpleTest(){
        userRepository.save(User.builder().idpId("test").build());
    }

}