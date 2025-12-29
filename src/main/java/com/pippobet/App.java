package com.pippobet;

import com.pippobet.controller.BetController;
import com.pippobet.exception.DatabasePortInvalidException;
import com.pippobet.exception.EmptyInputParameterException;
import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import com.pippobet.repository.mongo.BetMongoRepository;
import com.pippobet.view.fx.BetFXViewBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.bson.Document;

public class App extends Application
{
    public static final String BETS_TABLE_ID = "bets_table";
    public static final String BET_COLLECTION = "bet";

    private static final String DEFAULT_DB_HOST = "localhost";
    private static final int DEFAULT_DB_PORT = 27017;
    private static final String DEFAULT_DB_NAME = "bet";

    private static final String DB_HOST_INPUT_PARAM = "dbHost";
    private static final String DB_PORT_INPUT_PARAM = "dbPort";
    private static final String DB_NAME_INPUT_PARAM = "dbName";
    private static final String CONN_STRING = "mongodb://%s:%d/";


    @Override
    public void start(Stage primaryStage) throws DatabasePortInvalidException, EmptyInputParameterException
    {
        Map<String, String> namedParams = getParameters().getNamed();

        String dbHost = DEFAULT_DB_HOST;
        if (namedParams.containsKey(DB_HOST_INPUT_PARAM))
        {
            dbHost = namedParams.get(DB_HOST_INPUT_PARAM);
            if (dbHost.isBlank())
            {
                throw new EmptyInputParameterException(DB_HOST_INPUT_PARAM);
            }
        }

        int dbPort = DEFAULT_DB_PORT;
        if (namedParams.containsKey(DB_PORT_INPUT_PARAM))
        {
            String rawPort = namedParams.get(DB_PORT_INPUT_PARAM);
            if (rawPort.isBlank())
            {
                throw new EmptyInputParameterException(DB_PORT_INPUT_PARAM);
            }
            try
            {
                dbPort = Integer.parseInt(rawPort);
            }
            catch (NumberFormatException e)
            {
                throw new DatabasePortInvalidException(e);
            }
        }

        String dbName = DEFAULT_DB_NAME;
        if (namedParams.containsKey(DB_NAME_INPUT_PARAM))
        {
            dbName = namedParams.get(DB_NAME_INPUT_PARAM);
            if (dbName.isBlank())
            {
                throw new EmptyInputParameterException(DB_NAME_INPUT_PARAM);
            }
        }

        String mongoConnString = String.format(CONN_STRING, dbHost, dbPort);
        MongoClient client = MongoClients.create(new ConnectionString(mongoConnString));
        MongoDatabase db = client.getDatabase(dbName);
        MongoCollection<Document> betCollection = db.getCollection(BET_COLLECTION);
        BetRepository betRepository = new BetMongoRepository(betCollection);

        TableView<Bet> table = new TableView<>();
        table.setEditable(true);
        table.setId(BETS_TABLE_ID);
        BetFXViewBuilder view = new BetFXViewBuilder(table);
        BetController controller = new BetController(betRepository, view);
        view.setEventController(controller);
        Scene scene = new Scene(view.build());
        primaryStage.setScene(scene);

        controller.run(primaryStage);
    }
}
