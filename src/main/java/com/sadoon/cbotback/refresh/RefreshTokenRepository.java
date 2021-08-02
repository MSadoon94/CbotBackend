package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.refresh.models.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    @Override
    Optional<RefreshToken> findById(String id);

    Optional<RefreshToken> findByToken(String token);
}
