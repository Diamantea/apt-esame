package com.pippobet.controller;

import com.pippobet.model.Bet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BetRestControllerIT {
    @Autowired
    private MockMvc mvc;

    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.12"))
            .withExposedPorts(27017);

    @Autowired
    private MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    }

    @Before
    public void setUp()
    {
        for(String collection : mongoTemplate.getCollectionNames()) {
            mongoTemplate.dropCollection(collection);
        }
    }

    @Test
    public void testGetAllBetsWithNoBetsInTheDB() throws Exception {
        this.mvc.perform(get("/api/bets").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testGetAllBetsWithBetsInTheDB() throws Exception {
        Bet betOne = new Bet("home team 1","away team 1","outcome 1",1.);
        Bet betTwo = new Bet("home team 2","away team 2","outcome 2",2.);
        betOne = mongoTemplate.save(betOne);
        betTwo = mongoTemplate.save(betTwo);

        this.mvc.perform(get("/api/bets").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(betOne.getIdAsString())))
                .andExpect(jsonPath("$[0].home_team", is("home team 1")))
                .andExpect(jsonPath("$[0].away_team", is("away team 1")))
                .andExpect(jsonPath("$[0].outcome", is("outcome 1")))
                .andExpect(jsonPath("$[0].odd", is(1.)))
                .andExpect(jsonPath("$[1].id", is(betTwo.getIdAsString())))
                .andExpect(jsonPath("$[1].home_team", is("home team 2")))
                .andExpect(jsonPath("$[1].away_team", is("away team 2")))
                .andExpect(jsonPath("$[1].outcome", is("outcome 2")))
                .andExpect(jsonPath("$[1].odd", is(2.)));
    }

    @Test
    public void testPostBetShouldReturnCreatedBet() throws Exception {
        String betJson = "{\"home_team\":\"home\",\"away_team\":\"away\",\"outcome\":\"1\",\"odd\":1.5}";

        this.mvc.perform(post("/api/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.home_team", is("home")))
            .andExpect(jsonPath("$.away_team", is("away")))
            .andExpect(jsonPath("$.outcome", is("1")))
            .andExpect(jsonPath("$.odd", is(1.5)));

        var bets = mongoTemplate.findAll(Bet.class);
        org.junit.Assert.assertEquals(1, bets.size());
        org.junit.Assert.assertEquals("home", bets.get(0).getHomeTeam());
        org.junit.Assert.assertEquals("away", bets.get(0).getAwayTeam());
        org.junit.Assert.assertEquals("1", bets.get(0).getOutcome());
        org.junit.Assert.assertEquals(1.5, bets.get(0).getOdd(), 0.01);
    }
}
