package com.pippobet.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.pippobet.model.Bet;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Unit tests of BetMongoRepository against an in-memory MongoDB
 * (mongo-java-server), used as a fake: no Docker, no mocks of MongoTemplate.
 */
class BetMongoRepositoryTest {
    private static final String DB_NAME = "pippobet";
    private static final String BET_COLLECTION = "bet";

    private static MongoServer server;
    private static String connectionString;

    private MongoClient client;
    private MongoTemplate mongoTemplate;
    private BetMongoRepository repository;

    @BeforeAll
    static void startServer() {
        server = new MongoServer(new MemoryBackend());
        // bind on a random local port
        connectionString = server.bindAndGetConnectionString();
    }

    @AfterAll
    static void stopServer() {
        server.shutdown();
    }

    @BeforeEach
    void setUp() {
        client = MongoClients.create(connectionString);
        mongoTemplate = new MongoTemplate(client, DB_NAME);
        // always start with an empty collection
        mongoTemplate.dropCollection(BET_COLLECTION);
        repository = new BetMongoRepository(mongoTemplate);
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    void testFindAllBetsWhenDatabaseIsEmpty() {
        assertThat(repository.findAllBets()).isEmpty();
    }

    @Test
    void testFindAllBetsWhenDatabaseIsNotEmpty() {
        Bet bet1 = mongoTemplate.save(new Bet("home 1", "away 1", "1", 1.5));
        Bet bet2 = mongoTemplate.save(new Bet("home 2", "away 2", "X", 2.0));

        assertThat(repository.findAllBets())
            .containsExactly(bet1, bet2)
            .extracting(Bet::getId)
            .containsExactly(bet1.getId(), bet2.getId());
    }

    @Test
    void testSaveBetStoresTheDocumentAndAssignsTheId() {
        Bet saved = repository.saveBet(new Bet("Inter", "Milan", "1", 1.75));

        assertThat(saved.getId()).isNotNull();
        Document document = client.getDatabase(DB_NAME)
            .getCollection(BET_COLLECTION).find().first();
        assertThat(document).isNotNull();
        assertThat(document.getObjectId("_id")).isEqualTo(saved.getId());
        assertThat(document.getString("home_team")).isEqualTo("Inter");
        assertThat(document.getString("away_team")).isEqualTo("Milan");
        assertThat(document.getString("outcome")).isEqualTo("1");
        assertThat(document.getDouble("odd")).isEqualTo(1.75);
    }

    @Test
    void testDeleteBetRemovesOnlyThatBet() {
        Bet toDelete = mongoTemplate.save(new Bet("home 1", "away 1", "1", 1.5));
        Bet toKeep = mongoTemplate.save(new Bet("home 2", "away 2", "X", 2.0));

        repository.deleteBet(toDelete.getId());

        assertThat(mongoTemplate.findAll(Bet.class))
            .extracting(Bet::getId)
            .containsExactly(toKeep.getId());
    }
}
