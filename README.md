# PippoBet

Applicativo per esame Test-Driven Development, Build Automation, Continuous Integration

[![Coverage Status](https://coveralls.io/repos/github/Diamantea/apt-esame/badge.svg?branch=main)](https://coveralls.io/github/Diamantea/apt-esame?branch=main)
[![Java CI with Maven in Ubuntu](https://github.com/Diamantea/apt-esame/actions/workflows/maven.yml/badge.svg)](https://github.com/Diamantea/apt-esame/actions/workflows/maven.yml)

A Spring Boot web application to list, create and delete football bets,
through a Thymeleaf page (`/bets`) and a REST API (`/api/bets`).

## Requirements

- JDK 21 and Maven 3.9
- Docker (Testcontainers starts MongoDB for the integration and BDD tests)
- Google Chrome (headless) for the browser BDD scenarios

## Build and test

| Command | What it runs |
| --- | --- |
| `mvn test` | unit tests only (no Docker) |
| `mvn verify` | unit, integration (`src/it/java`) and BDD (`src/bdd`) tests |
| `mvn verify -Pjacoco` | the above plus the JaCoCo report in `target/site/jacoco` |
| `mvn verify -Pmutation-testing` | the above plus PIT, report in `target/pit-reports` |
| `mvn verify -Pcoveralls` | JaCoCo report sent to Coveralls (needs `COVERALLS_REPO_TOKEN`) |

## Run the application

```
docker compose up -d
mvn spring-boot:run
```

or `mvn package` and `java -jar target/PippoBet-1.0-SNAPSHOT.jar`.
The page is at http://localhost:8080/bets.
