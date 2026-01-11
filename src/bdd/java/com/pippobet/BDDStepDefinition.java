package com.pippobet;

import com.pippobet.model.Bet;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
    private WebDriver driver;

    private Bet newBet;

    @Before
    public void setUp() {
        RestAssured.port = port;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);

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

    @When("get all bets from view endpoint")
    public void whenGetAllBetsFromViewEndpoint() {
        driver.navigate().to("http://localhost:" + port + "/bets");
    }

    @Then("view endpoint returns success response with bets displayed")
    public void thenViewEndpointReturnsSuccessResponseWithBetsDisplayed() {
        List<WebElement> tableRows = driver.findElements(By.xpath("//table/tbody/tr"));
        Assertions.assertEquals(expectedBets.size(), tableRows.size(),
                "Number of bets in table should match expected bets");

        for (int i = 0; i < expectedBets.size(); i++) {
            Bet expectedBet = expectedBets.get(i);
            WebElement row = tableRows.get(i);

            List<WebElement> cells = row.findElements(By.tagName("td"));
            String homeTeam = cells.get(0).getText();
            String awayTeam = cells.get(1).getText();
            String outcome = cells.get(2).getText();
            String odd = cells.get(3).getText();

            Assertions.assertEquals(expectedBet.getHomeTeam(), homeTeam);
            Assertions.assertEquals(expectedBet.getAwayTeam(), awayTeam);
            Assertions.assertEquals(expectedBet.getOutcome(), outcome);
            Assertions.assertEquals(String.valueOf(expectedBet.getOdd()), odd);
        }

        driver.quit();
    }

    @Given("a new bet with home team {string}, away team {string}, outcome {string}, and odd {double}")
    public void givenANewBet(String homeTeam, String awayTeam, String outcome, double odd) {
        this.newBet = new Bet(homeTeam, awayTeam, outcome, odd);
    }

    @When("the bet is created via REST endpoint")
    public void whenBetIsCreatedViaRESTEndpoint() {
        String betJson = String.format(
            "{\"home_team\":\"%s\",\"away_team\":\"%s\",\"outcome\":\"%s\",\"odd\":%s}",
            newBet.getHomeTeam(), newBet.getAwayTeam(), newBet.getOutcome(), newBet.getOdd()
        );

        response = RestAssured.given()
            .contentType("application/json")
            .body(betJson)
            .when()
            .post("/api/bets")
            .andReturn();
    }

    @Then("the bet is successfully created")
    public void thenBetIsSuccessfullyCreated() {
        response.then().statusCode(HttpStatus.SC_OK);

        Bet createdBet = response.jsonPath().getObject("", Bet.class);
        Assertions.assertEquals(newBet.getHomeTeam(), createdBet.getHomeTeam());
        Assertions.assertEquals(newBet.getAwayTeam(), createdBet.getAwayTeam());
        Assertions.assertEquals(newBet.getOutcome(), createdBet.getOutcome());
        Assertions.assertEquals(newBet.getOdd(), createdBet.getOdd());
    }

    @Then("the bet is persisted in the Database")
    public void thenBetIsPersistedInDatabase() {
        List<Bet> bets = mongoTemplate.findAll(Bet.class);
        Assertions.assertEquals(1, bets.size());

        Bet persistedBet = bets.get(0);
        Assertions.assertEquals(newBet.getHomeTeam(), persistedBet.getHomeTeam());
        Assertions.assertEquals(newBet.getAwayTeam(), persistedBet.getAwayTeam());
        Assertions.assertEquals(newBet.getOutcome(), persistedBet.getOutcome());
        Assertions.assertEquals(newBet.getOdd(), persistedBet.getOdd());
    }

}
