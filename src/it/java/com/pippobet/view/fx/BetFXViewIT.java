package com.pippobet.view.fx;

import com.pippobet.controller.BetController;
import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import com.pippobet.repository.mongo.BetMongoRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.InetSocketAddress;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import org.junit.AfterClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BetFXViewIT extends ApplicationTest
{
    public static final String DB_NAME = "test";
    private static MongoServer server;
    private static InetSocketAddress serverAddress;

    private BetFXViewBuilder view;
    private TableView<Bet> table;
    private BetController controller;
    private BetRepository betRepository;
    private Region viewRegion;
    private MongoClient mongoClient;


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
    void setUp()
    {
        table = new TableView<>();
        table.setEditable(true);

        view = new BetFXViewBuilder(table);

        this.mongoClient = MongoClients.create("mongodb://%s:%d/".formatted(serverAddress.getHostName(), serverAddress.getPort()));
        var betCollection = this.mongoClient.getDatabase(DB_NAME).getCollection("bet");
        this.betRepository = new BetMongoRepository(betCollection);
        controller = new BetController(betRepository, view);
        view.setEventController(controller);

        this.viewRegion = view.build();
    }


    @AfterEach
    void afterEach()
    {
        this.mongoClient.getDatabase(DB_NAME).drop();
        this.mongoClient.close();
    }


    @Test
    void testUpdateBetsUpdatesTable()
    {
        Bet bet1 = new Bet("Team A", "Team B", "1", 2.5);
        Bet bet2 = new Bet("Team C", "Team D", "X", 3.1);
        List<Bet> bets = List.of(bet1, bet2);

        Platform.runLater(() -> view.updateBets(bets));

        Platform.runLater(() -> {
            assertEquals(2, table.getItems().size());
            assertEquals(bet1, table.getItems().get(0));
            assertEquals(bet2, table.getItems().get(1));
        });
    }
}

