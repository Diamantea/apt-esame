package com.pippobet.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

/**
 * The Spring context shared by all the scenarios: the whole application
 * on a random port, connected to a real MongoDB.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    // Cucumber does not run on JUnit Jupiter, so @Testcontainers/@Container
    // would be ignored: the container is started explicitly, once for the
    // whole BDD run; Testcontainers removes it when the JVM terminates.
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:8.0.12");

    static {
        mongo.start();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }
}
