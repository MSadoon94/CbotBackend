package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.brokerage.model.Brokerage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BrokerageRepository extends MongoRepository<Brokerage, String> {

    Brokerage getBrokerageByName(String name);

}
