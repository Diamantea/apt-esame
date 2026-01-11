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
}