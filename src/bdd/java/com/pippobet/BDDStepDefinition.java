package com.pippobet;

import com.pippobet.model.Bet;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

@CucumberContextConfiguration
public class BDDStepDefinition extends CucumberSpringConfiguration {
    private static final String BET_COLLECTION = "bet";

    @Autowired
    private MongoTemplate mongoTemplate;

    @LocalServerPort
    private int port;

    private Response response;
    private List<Bet> expectedBets;

    @Before
    public void setUp() {
        RestAssured.port = port;

        response = null;
        expectedBets = new ArrayList<>();

        mongoTemplate.getDb().drop();
        mongoTemplate.getDb().createCollection(BET_COLLECTION);
    }

    @Given("{int} bets in the Database")
    public void givenNBetsInDB(int betCount) {
        for (int i = 0; i < betCount; i++) {
            Bet bet = new Bet("home team " + i,
                    "away team " + i,
                    "outcome " + i,
                    1.);

            mongoTemplate.save(bet);
            expectedBets.add(bet);
        }
    }

    @When("get all bets from REST endpoint")
    public void whenGetAllBetsFromRESTEndpoint() throws InterruptedException {
        response = RestAssured.get("/api/bets").andReturn();
    }

    @Then("bets are correctly returned in the response")
    public void thenBetsAreCorrectlyReturnedInResponse() {
        response.then().statusCode(200);
        var bets = response.jsonPath().getList("", Bet.class);
        Assertions.assertEquals(expectedBets, bets);
    }

}
