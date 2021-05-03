package com.sadoon.cbotback.user;

import com.sadoon.cbotback.user.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    User getUserByUsername(String username);
}
