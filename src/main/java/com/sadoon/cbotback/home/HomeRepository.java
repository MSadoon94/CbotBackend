package com.sadoon.cbotback.home;

import com.sadoon.cbotback.home.models.KrakenCard;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HomeRepository extends MongoRepository<KrakenCard, String> {
    KrakenCard getBrokerageCardByAccount(String account);
}
