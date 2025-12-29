package com.pippobet.controller;

import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import com.pippobet.repository.mongo.BetMongoRepository;
import com.pippobet.view.BetView;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.InetSocketAddress;
import java.util.List;
import org.junit.AfterClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BetControllerIT
{
    public static final String DB_NAME = "test";
    private static MongoServer server;
    private static InetSocketAddress serverAddress;

    private MongoClient mongoClient;
    private BetController controller;
    private BetView view;
    private BetRepository repository;


    @BeforeAll
    public static void setupServer()
    {
        server = new MongoServer(new MemoryBackend());
        serverAddress = server.bind();
    }


    @AfterClass
    public static void shutdownServer()
    {
        server.shutdown();
        serverAddress = null;
    }


    @BeforeEach
    public void setUp()
    {
        this.mongoClient = MongoClients.create("mongodb://%s:%d/".formatted(serverAddress.getHostName(), serverAddress.getPort()));
        var betCollection = this.mongoClient.getDatabase(DB_NAME).getCollection("bet");
        this.repository = new BetMongoRepository(betCollection);
        this.view = Mockito.mock(BetView.class);
        this.controller = new BetController(repository, view);
    }


    @AfterEach
    void afterEach()
    {
        this.mongoClient.getDatabase(DB_NAME).drop();
        this.mongoClient.close();
    }


    @Test
    void testGetAllEvents()
    {
        var bet = new Bet("home", "away", "X", 1.2);
        this.repository.save(bet);

        controller.showAllBets();

        Mockito.verify(view).updateBets(List.of(bet));
    }


    @Test
    void testAddNewBet()
    {
        var bet = new Bet("add", "bet", "X", 1.2);

        controller.addBet(bet);

        Mockito.verify(view).updateBets(List.of(bet));
    }


    @Test
    void testDeleteBet()
    {
        var bet = new Bet("bet", "delete", "X", 1.2);
        repository.save(bet);

        controller.deleteBet(bet);
        
        Mockito.verify(view).updateBets(List.of());
    }
}
