package com.sadoon.cbotback.home;

import com.sadoon.cbotback.home.models.BrokerageCard;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HomeRepository extends MongoRepository<BrokerageCard, String> {

    BrokerageCard getBrokerageCardByAccount(String account);
}
