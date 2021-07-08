package com.sadoon.cbotback.security.token;

import com.sadoon.cbotback.security.token.models.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    @Override
    Optional<RefreshToken> findById(String id);

    Optional<RefreshToken> findByToken(String token);
}
