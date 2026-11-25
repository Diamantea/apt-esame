package com.pippobet.repository.mongo;

import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    @Override
    public Bet saveBet(Bet bet)
    {
        return mongoTemplate.save(bet);
    }

    @Override
    public void deleteBet(ObjectId id)
    {
        mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), Bet.class);
    }
}
