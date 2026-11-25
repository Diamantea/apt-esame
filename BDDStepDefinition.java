package com.pippobet.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.pippobet.model.Bet;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Step definitions: the REST scenarios use REST Assured, the web scenarios
 * a headless Chrome driven by Selenium. The database is prepared and checked
 * directly through MongoTemplate, not through the application classes.
 */
public class BDDStepDefinition {
    private static final String BET_COLLECTION = "bet";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Autowired
    private MongoTemplate mongoTemplate;

    @LocalServerPort
    private int port;

    private Response response;
    private List<Bet> expectedBets;
    private WebDriver driver;
    private Bet newBet;
    private String betIdToDelete;

    @Before
    public void setUp() {
        RestAssured.port = port;
        response = null;
        driver = null;
        expectedBets = new ArrayList<>();
        mongoTemplate.dropCollection(BET_COLLECTION);
    }

    @After
    public void tearDown() {
        // always executed, also when a step fails: Chrome is never left open
        if (driver != null) {
            driver.quit();
        }
    }

    /** Chrome is started only by the scenarios that use the web interface. */
    private WebDriver browser() {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
        }
        return driver;
    }

    private String betsPage() {
        return "http://localhost:" + port + "/bets";
    }

    @Given("{int} bets in the Database")
    public void givenNBetsInDB(int betCount) {
        for (int i = 0; i < betCount; i++) {
            Bet bet = new Bet("home team " + i, "away team " + i, "1", 1.5 + i);
            expectedBets.add(mongoTemplate.save(bet));
        }
    }

    @Given("a new bet with home team {string}, away team {string}, outcome {string}, and odd {double}")
    public void givenANewBet(String homeTeam, String awayTeam, String outcome, double odd) {
        newBet = new Bet(homeTeam, awayTeam, outcome, odd);
    }

    @When("get all bets from REST endpoint")
    public void whenGetAllBetsFromRESTEndpoint() {
        response = RestAssured.get("/api/bets").andReturn();
    }

    @When("the bet is created via REST endpoint")
    public void whenBetIsCreatedViaRESTEndpoint() {
        String betJson = String.format(
            "{\"home_team\":\"%s\",\"away_team\":\"%s\",\"outcome\":\"%s\",\"odd\":%s}",
            newBet.getHomeTeam(), newBet.getAwayTeam(), newBet.getOutcome(), newBet.getOdd());

        response = RestAssured.given()
            .contentType("application/json")
            .body(betJson)
            .when()
            .post("/api/bets")
            .andReturn();
    }

    @When("the bet is deleted via REST endpoint")
    public void whenBetIsDeletedViaRESTEndpoint() {
        betIdToDelete = expectedBets.get(0).getIdAsString();
        response = RestAssured.delete("/api/bets/" + betIdToDelete).andReturn();
    }

    @When("get all bets from view endpoint")
    public void whenGetAllBetsFromViewEndpoint() {
        browser().get(betsPage());
    }

    @When("the user navigates to the bets page")
    public void whenUserNavigatesToTheBetsPage() {
        browser().get(betsPage());
    }

    @When("the user fills the form with home team {string}, away team {string}, outcome {string}, and odd {double}")
    public void whenUserFillsTheForm(String homeTeam, String awayTeam, String outcome, double odd) {
        browser().findElement(By.id("homeTeam")).sendKeys(homeTeam);
        browser().findElement(By.id("awayTeam")).sendKeys(awayTeam);
        new Select(browser().findElement(By.id("outcome"))).selectByValue(outcome);
        browser().findElement(By.id("odd")).sendKeys(String.valueOf(odd));
    }

    @When("the user submits the form")
    public void whenUserSubmitsTheForm() {
        browser().findElement(By.cssSelector("button.btn-submit")).click();
    }

    @When("the user clicks the delete button for the first bet")
    public void whenUserClicksDeleteButtonForFirstBet() {
        betIdToDelete = expectedBets.get(0).getIdAsString();
        browser().findElement(By.cssSelector("button.btn-delete")).click();
        // the page asks for a confirmation with a JavaScript dialog
        new WebDriverWait(browser(), TIMEOUT)
            .until(ExpectedConditions.alertIsPresent())
            .accept();
    }

    @Then("bets are correctly returned in the response")
    public void thenBetsAreCorrectlyReturnedInResponse() {
        response.then().statusCode(200);
        List<String> ids = response.jsonPath().getList("id", String.class);
        List<String> homeTeams = response.jsonPath().getList("home_team", String.class);
        assertThat(ids).containsExactlyElementsOf(
            expectedBets.stream().map(Bet::getIdAsString).toList());
        assertThat(homeTeams).containsExactlyElementsOf(
            expectedBets.stream().map(Bet::getHomeTeam).toList());
    }

    @Then("view endpoint returns success response with bets displayed")
    public void thenViewEndpointShowsTheBets() {
        List<WebElement> rows = browser().findElements(By.xpath("//table/tbody/tr"));
        assertThat(rows).hasSize(expectedBets.size());
        for (int i = 0; i < expectedBets.size(); i++) {
            Bet expected = expectedBets.get(i);
            List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));
            assertThat(cells.get(0).getText()).isEqualTo(expected.getHomeTeam());
            assertThat(cells.get(1).getText()).isEqualTo(expected.getAwayTeam());
            assertThat(cells.get(2).getText()).isEqualTo(expected.getOutcome());
            assertThat(cells.get(3).getText()).isEqualTo(String.valueOf(expected.getOdd()));
        }
    }

    @Then("the bet is successfully returned in the response")
    public void thenBetIsReturnedInTheResponse() {
        response.then().statusCode(200);
        assertThat(response.jsonPath().getString("id")).isNotNull();
        assertThat(response.jsonPath().getString("home_team")).isEqualTo(newBet.getHomeTeam());
        assertThat(response.jsonPath().getString("away_team")).isEqualTo(newBet.getAwayTeam());
        assertThat(response.jsonPath().getString("outcome")).isEqualTo(newBet.getOutcome());
        assertThat(response.jsonPath().getDouble("odd")).isEqualTo(newBet.getOdd());
    }

    @Then("the bet is persisted in the Database")
    public void thenBetIsPersistedInDatabase() {
        assertThat(mongoTemplate.findAll(Bet.class)).containsExactly(newBet);
    }

    @Then("the bet is persisted in the Database with home team {string}, away team {string}, outcome {string}, and odd {double}")
    public void thenBetIsPersistedWithDetails(String homeTeam, String awayTeam, String outcome, double odd) {
        assertThat(mongoTemplate.findAll(Bet.class))
            .containsExactly(new Bet(homeTeam, awayTeam, outcome, odd));
    }

    @Then("the success message is displayed")
    public void thenSuccessMessageIsDisplayed() {
        WebElement alert = new WebDriverWait(browser(), TIMEOUT)
            .until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        assertThat(alert.getText()).contains("successfully!");
    }

    @Then("the response status code is {int}")
    public void thenResponseStatusCodeIs(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("the bet is removed from the Database")
    public void thenBetIsRemovedFromDatabase() {
        assertThat(mongoTemplate.findAll(Bet.class))
            .extracting(Bet::getIdAsString)
            .doesNotContain(betIdToDelete)
            .hasSize(expectedBets.size() - 1);
    }
}
