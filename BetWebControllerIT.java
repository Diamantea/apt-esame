package com.pippobet.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.pippobet.model.Bet;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * The real web page served on a random port, with a real MongoDB
 * (Testcontainers), driven by HtmlUnit (a browser without a GUI;
 * JavaScript is disabled, so the delete confirmation is not shown).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class BetWebControllerIT {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:8.0.12");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @LocalServerPort
    private int port;

    private WebDriver driver;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        driver = new HtmlUnitDriver();
        mongoTemplate.dropCollection(Bet.class);
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void testViewBets() {
        mongoTemplate.save(new Bet("home team", "away team", "X", 2.5));

        driver.get(baseUrl + "/bets");

        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        assertThat(rows).hasSize(1);
        List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
        assertThat(cells.get(0).getText()).isEqualTo("home team");
        assertThat(cells.get(1).getText()).isEqualTo("away team");
        assertThat(cells.get(2).getText()).isEqualTo("X");
        assertThat(cells.get(3).getText()).isEqualTo("2.5");
    }

    @Test
    void testViewBetsEmpty() {
        driver.get(baseUrl + "/bets");

        assertThat(driver.findElement(By.className("no-bets")).getText())
            .contains("No bets available.");
        assertThat(driver.findElements(By.tagName("table"))).isEmpty();
    }

    @Test
    void testViewMultipleBets() {
        mongoTemplate.save(new Bet("Team A", "Team B", "1", 1.5));
        mongoTemplate.save(new Bet("Team C", "Team D", "2", 3.0));
        mongoTemplate.save(new Bet("Team E", "Team F", "X", 2.0));

        driver.get(baseUrl + "/bets");

        assertThat(driver.findElements(By.cssSelector("tbody tr"))).hasSize(3);
    }

    @Test
    void testAddBetSubmitsTheFormAndPersistsTheBet() {
        driver.get(baseUrl + "/bets");

        fillForm("Manchester United", "Liverpool", "2", "1.95");
        driver.findElement(By.cssSelector("button.btn-submit")).click();

        assertThat(driver.findElement(By.className("alert-success")).getText())
            .contains("Bet added successfully!");
        assertThat(mongoTemplate.findAll(Bet.class))
            .containsExactly(new Bet("Manchester United", "Liverpool", "2", 1.95));
    }

    @Test
    void testFormIsResetAfterSubmission() {
        driver.get(baseUrl + "/bets");

        fillForm("Team A", "Team B", "2", "2.50");
        driver.findElement(By.cssSelector("button.btn-submit")).click();

        // after the redirect the page is new: inputs empty, select on its first option
        assertThat(driver.findElement(By.id("homeTeam")).getAttribute("value")).isEmpty();
        assertThat(driver.findElement(By.id("awayTeam")).getAttribute("value")).isEmpty();
        assertThat(driver.findElement(By.id("odd")).getAttribute("value")).isEmpty();
        assertThat(new Select(driver.findElement(By.id("outcome")))
            .getFirstSelectedOption().getAttribute("value")).isEqualTo("1");
    }

    @Test
    void testDeleteBetRemovesItFromThePageAndTheDB() {
        Bet toDelete = mongoTemplate.save(new Bet("Team A", "Team B", "1", 1.5));
        Bet toKeep = mongoTemplate.save(new Bet("Team C", "Team D", "2", 3.0));
        driver.get(baseUrl + "/bets");

        // first row = first bet saved
        driver.findElement(By.cssSelector("button.btn-delete")).click();

        assertThat(driver.findElement(By.className("alert-success")).getText())
            .contains("Bet deleted successfully!");
        assertThat(driver.findElements(By.cssSelector("tbody tr"))).hasSize(1);
        assertThat(mongoTemplate.findAll(Bet.class))
            .extracting(Bet::getId)
            .containsExactly(toKeep.getId())
            .doesNotContain(toDelete.getId());
    }

    private void fillForm(String homeTeam, String awayTeam, String outcome, String odd) {
        driver.findElement(By.id("homeTeam")).sendKeys(homeTeam);
        driver.findElement(By.id("awayTeam")).sendKeys(awayTeam);
        new Select(driver.findElement(By.id("outcome"))).selectByValue(outcome);
        driver.findElement(By.id("odd")).sendKeys(odd);
    }
}
