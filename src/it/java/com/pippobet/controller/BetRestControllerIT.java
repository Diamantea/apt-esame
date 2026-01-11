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
        mongoTemplate.save(betOne);
        mongoTemplate.save(betTwo);

        this.mvc.perform(get("/api/bets").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].home_team", is("home team 1")))
                .andExpect(jsonPath("$[0].away_team", is("away team 1")))
                .andExpect(jsonPath("$[0].outcome", is("outcome 1")))
                .andExpect(jsonPath("$[0].odd", is(1.)))
                .andExpect(jsonPath("$[1].home_team", is("home team 2")))
                .andExpect(jsonPath("$[1].away_team", is("away team 2")))
                .andExpect(jsonPath("$[1].outcome", is("outcome 2")))
                .andExpect(jsonPath("$[1].odd", is(2.)));
    }

}
