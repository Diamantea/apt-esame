package com.pippobet.repository.mongo;

import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import com.mongodb.client.MongoCollection;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;
import org.bson.Document;

public class BetMongoRepository implements BetRepository
{
    public static final String HOME_TEAM_ATTR = "home_team";
    public static final String AWAY_TEAM_ATTR = "away_team";
    public static final String OUTCOME_ATTR = "outcome";
    public static final String ODD_ATTR = "odd";

    private final MongoCollection<Document> betCollection;


    public BetMongoRepository(MongoCollection<Document> betCollection)
    {
        this.betCollection = betCollection;
    }


    @Override
    public List<Bet> findAllBets()
    {
        return toBet(betCollection.find().spliterator());
    }


    @Override
    public void save(Bet newBet)
    {
        betCollection.insertOne(toDocument(newBet));
    }


    @Override
    public void delete(Bet bet)
    {
        betCollection.deleteOne(toDocument(bet));
    }


    public static List<Bet> toBet(Spliterator<Document> iterator)
    {
        return StreamSupport.stream(iterator, false)
            .map(BetMongoRepository::toBet)
            .toList();
    }


    public static Bet toBet(Document doc)
    {
        return new Bet(doc.getString(HOME_TEAM_ATTR), doc.getString(AWAY_TEAM_ATTR),
            doc.getString(OUTCOME_ATTR), doc.getDouble(ODD_ATTR));
    }


    public static Document toDocument(Bet bet)
    {
        return (new Document())
            .append(BetMongoRepository.HOME_TEAM_ATTR, bet.getHomeTeam())
            .append(BetMongoRepository.AWAY_TEAM_ATTR, bet.getAwayTeam())
            .append(BetMongoRepository.OUTCOME_ATTR, bet.getOutcome())
            .append(BetMongoRepository.ODD_ATTR, bet.getOdd());
    }
}
