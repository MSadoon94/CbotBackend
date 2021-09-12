package com.sadoon.cbotback.strategy;

import com.mongodb.lang.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StrategyRepository extends MongoRepository<Strategy, String> {
    Strategy getStrategyByName(@NonNull String name);
}
