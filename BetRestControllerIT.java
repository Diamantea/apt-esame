package com.pippobet.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pippobet.model.Bet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Whole Spring context (controller, service, repository) with a real MongoDB
 * started by Testcontainers; HTTP calls are simulated by MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BetRestControllerIT {

    // started before the Spring context and stopped after the last test
    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:8.0.12");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Bet.class);
    }

    @Test
    void testGetAllBetsWithNoBetsInTheDB() throws Exception {
        mvc.perform(get("/api/bets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void testGetAllBetsWithBetsInTheDB() throws Exception {
        Bet betOne = mongoTemplate.save(new Bet("home team 1", "away team 1", "1", 1.0));
        Bet betTwo = mongoTemplate.save(new Bet("home team 2", "away team 2", "X", 2.0));

        mvc.perform(get("/api/bets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id", is(betOne.getIdAsString())))
            .andExpect(jsonPath("$[0].home_team", is("home team 1")))
            .andExpect(jsonPath("$[0].away_team", is("away team 1")))
            .andExpect(jsonPath("$[0].outcome", is("1")))
            .andExpect(jsonPath("$[0].odd", is(1.0)))
            .andExpect(jsonPath("$[1].id", is(betTwo.getIdAsString())))
            .andExpect(jsonPath("$[1].home_team", is("home team 2")))
            .andExpect(jsonPath("$[1].away_team", is("away team 2")))
            .andExpect(jsonPath("$[1].outcome", is("X")))
            .andExpect(jsonPath("$[1].odd", is(2.0)));
    }

    @Test
    void testPostBetShouldReturnAndPersistTheCreatedBet() throws Exception {
        String betJson =
            "{\"home_team\":\"home\",\"away_team\":\"away\",\"outcome\":\"1\",\"odd\":1.5}";

        mvc.perform(post("/api/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.home_team", is("home")))
            .andExpect(jsonPath("$.away_team", is("away")))
            .andExpect(jsonPath("$.outcome", is("1")))
            .andExpect(jsonPath("$.odd", is(1.5)));

        List<Bet> bets = mongoTemplate.findAll(Bet.class);
        assertThat(bets).containsExactly(new Bet("home", "away", "1", 1.5));
        assertThat(bets.get(0).getId()).isNotNull();
    }

    @Test
    void testDeleteBetShouldRemoveItFromTheDB() throws Exception {
        Bet toDelete = mongoTemplate.save(new Bet("home 1", "away 1", "1", 1.5));
        Bet toKeep = mongoTemplate.save(new Bet("home 2", "away 2", "2", 3.0));

        mvc.perform(delete("/api/bets/{id}", toDelete.getIdAsString()))
            .andExpect(status().isOk());

        assertThat(mongoTemplate.findAll(Bet.class))
            .extracting(Bet::getId)
            .containsExactly(toKeep.getId());
    }
}
