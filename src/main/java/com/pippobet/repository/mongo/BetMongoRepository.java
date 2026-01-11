package com.pippobet.repository.mongo;

import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BetMongoRepository implements BetRepository
{
    private final MongoTemplate mongoTemplate;


    public BetMongoRepository(MongoTemplate mongoTemplate)
    {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public List<Bet> findAllBets()
    {
        return mongoTemplate.findAll(Bet.class);
    }
}
