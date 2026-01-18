package com.pippobet.repository.mongo;

import com.pippobet.model.Bet;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public class BetMongoRepositoryTest
{
    private MongoTemplate mongoTemplate;
    private BetMongoRepository repository;

    @BeforeEach
    public void setUp()
    {
        this.mongoTemplate = Mockito.mock(MongoTemplate.class);
        this.repository = new BetMongoRepository(mongoTemplate);
    }

    @Test
    void testFindAllBetsWithEmptyCollectionShouldReturnEmptyList()
    {
        Mockito.when(mongoTemplate.findAll(Bet.class)).thenReturn(List.of());

        var actualBets = repository.findAllBets();

        Assertions.assertEquals(List.of(), actualBets);
        Mockito.verify(mongoTemplate).findAll(Bet.class);
    }

    @Test
    void testFindAllBetsWithMultipleBetsShouldReturnAllBets()
    {
        var bet1 = new Bet("home 1", "away 1", "1", 1.5);
        var bet2 = new Bet("home 2", "away 2", "X", 2.0);
        var expectedBets = List.of(bet1, bet2);
        Mockito.when(mongoTemplate.findAll(Bet.class)).thenReturn(expectedBets);

        var actualBets = repository.findAllBets();

        Assertions.assertEquals(expectedBets, actualBets);
        Mockito.verify(mongoTemplate).findAll(Bet.class);
    }

    @Test
    void testDeleteBetShouldCallMongoTemplateRemove()
    {
        var betId = new ObjectId();

        repository.deleteBet(betId);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        Mockito.verify(mongoTemplate).remove(queryCaptor.capture(), Mockito.eq(Bet.class));
    }
}
