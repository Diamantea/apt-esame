package com.pippobet.repository.mongo;

import com.pippobet.model.Bet;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BetMongoRepositoryTest
{
    private MongoCollection<Document> betCollection;
    private BetMongoRepository repository;


    @BeforeEach
    public void setUp()
    {
        this.betCollection = (MongoCollection<Document>) Mockito.mock(MongoCollection.class);
        this.repository = new BetMongoRepository(betCollection);
    }


    @Test
    void testGetAllBetsWithEmptyCollectionShouldReturnEmptyList()
    {
        var betsAsDoc = List.<Document>of();
        var findIterable = (FindIterable<Document>) Mockito.mock(FindIterable.class);
        Mockito.when(findIterable.spliterator()).thenReturn(betsAsDoc.spliterator());
        Mockito.when(this.betCollection.find()).thenReturn(findIterable);

        var actualBets = this.repository.findAllBets();

        var expectedBets = List.<Bet>of();
        Assertions.assertEquals(expectedBets, actualBets);
    }


    @Test
    void testGetAllBetsWithTwoDocumentInCollectionShouldReturnBothBets()
    {
        Document betOne = (new Document())
            .append(BetMongoRepository.HOME_TEAM_ATTR, "home 1")
            .append(BetMongoRepository.AWAY_TEAM_ATTR, "away 1")
            .append(BetMongoRepository.OUTCOME_ATTR, "outcome 1")
            .append(BetMongoRepository.ODD_ATTR, 1.);
        Document betTwo = (new Document())
            .append(BetMongoRepository.HOME_TEAM_ATTR, "home 2")
            .append(BetMongoRepository.AWAY_TEAM_ATTR, "away 2")
            .append(BetMongoRepository.OUTCOME_ATTR, "outcome 2")
            .append(BetMongoRepository.ODD_ATTR, 2.);
        var betsAsDoc = List.of(betOne, betTwo);
        var findIterable = (FindIterable<Document>) Mockito.mock(FindIterable.class);
        Mockito.when(findIterable.spliterator()).thenReturn(betsAsDoc.spliterator());
        Mockito.when(this.betCollection.find()).thenReturn(findIterable);

        var actualBets = this.repository.findAllBets();

        var expectedBets = BetMongoRepository.toBet(betsAsDoc.spliterator());
        Assertions.assertEquals(expectedBets, actualBets);
    }


    @Test
    void testSaveDocument()
    {
        var betToSave = new Bet("home 1", "away 1", "X", 1.7);
        var betToSaveDoc = BetMongoRepository.toDocument(betToSave);

        this.repository.save(betToSave);

        Mockito.verify(this.betCollection).insertOne(betToSaveDoc);
    }


    @Test
    void testDeleteDocument()
    {
        var bet = new Bet("home 1", "away 1", "X", 1.7);
        var betDoc = BetMongoRepository.toDocument(bet);

        this.repository.delete(bet);

        Mockito.verify(this.betCollection).deleteOne(betDoc);
    }
}
