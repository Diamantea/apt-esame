package com.pippobet.controller;

import com.pippobet.model.Bet;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MongoDBContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BetWebControllerIT {

    @ClassRule
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private MongoTemplate mongoTemplate;

    @LocalServerPort
    private int port;

    private WebDriver driver;

    private String baseUrl;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Before
    public void setup() {
        baseUrl = "http://localhost:" + port;
        driver = new HtmlUnitDriver();
        mongoTemplate.dropCollection(Bet.class);
    }

    @After
    public void teardown() {
        driver.quit();
    }

    @Test
    public void testViewBets() {
        Bet testBet = new Bet("home team", "away team", "1X2", 2.5);
        mongoTemplate.save(testBet);

        driver.get(baseUrl + "/bets");

        List<WebElement> betRows = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));

        assertThat(betRows.size()).isEqualTo(1);
        List<WebElement> cells = betRows.get(0).findElements(By.tagName("td"));
        assertThat(cells.get(0).getText()).isEqualTo("home team");
        assertThat(cells.get(1).getText()).isEqualTo("away team");
        assertThat(cells.get(2).getText()).isEqualTo("1X2");
        assertThat(cells.get(3).getText()).isEqualTo("2.5");
    }

    @Test
    public void testViewBetsEmpty() {
        driver.get(baseUrl + "/bets");

        WebElement messageElement = driver.findElement(By.className("no-bets"));
        assertThat(messageElement.getText()).contains("No bets available.");
    }

    @Test
    public void testViewMultipleBets() {
        mongoTemplate.save(new Bet("Team A", "Team B", "1", 1.5));
        mongoTemplate.save(new Bet("Team C", "Team D", "2", 3.0));
        mongoTemplate.save(new Bet("Team E", "Team F", "X", 2.0));

        driver.get(baseUrl + "/bets");

        List<WebElement> betRows = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));

        assertThat(betRows.size()).isEqualTo(3);
    }

    @Test
    public void testAddBet_SuccessfullySubmitsForm() {
        driver.get(baseUrl + "/bets");

        driver.findElement(By.id("homeTeam")).sendKeys("Manchester United");
        driver.findElement(By.id("awayTeam")).sendKeys("Liverpool");
        driver.findElement(By.id("outcome")).sendKeys("1");
        driver.findElement(By.id("odd")).sendKeys("1.95");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement successAlert = driver.findElement(By.className("alert-success"));
        assertThat(successAlert.getText()).contains("Bet added successfully!");

        List<Bet> savedBets = mongoTemplate.findAll(Bet.class);
        assertThat(savedBets).hasSize(1);
        assertThat(savedBets.get(0).getHomeTeam()).isEqualTo("Manchester United");
        assertThat(savedBets.get(0).getAwayTeam()).isEqualTo("Liverpool");
        assertThat(savedBets.get(0).getOutcome()).isEqualTo("1");
        assertThat(savedBets.get(0).getOdd()).isEqualTo(1.95);
    }

    @Test
    public void testAddBet_FormClearsAfterSubmission() {
        driver.get(baseUrl + "/bets");

        driver.findElement(By.id("homeTeam")).sendKeys("Team A");
        driver.findElement(By.id("awayTeam")).sendKeys("Team B");
        driver.findElement(By.id("outcome")).sendKeys("2");
        driver.findElement(By.id("odd")).sendKeys("2.50");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // After submission and redirect, verify form is cleared
        WebElement homeTeamInput = driver.findElement(By.id("homeTeam"));
        assertThat(homeTeamInput.getAttribute("value")).isEmpty();
        WebElement awayTeamInput = driver.findElement(By.id("awayTeam"));
        assertThat(awayTeamInput.getAttribute("value")).isEmpty();
        WebElement outcomeInput = driver.findElement(By.id("outcome"));
        assertThat(outcomeInput.getAttribute("value")).isEmpty();
        WebElement oddInput = driver.findElement(By.id("odd"));
        assertThat(oddInput.getAttribute("value")).isEmpty();
    }
}