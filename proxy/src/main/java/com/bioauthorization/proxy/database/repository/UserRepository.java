package com.bioauthorization.proxy.database.repository;

import com.bioauthorization.proxy.database.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
