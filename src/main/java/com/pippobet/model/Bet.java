package com.pippobet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

@Document(collection = "bet")
public class Bet {
    @Id
    private ObjectId id;

    @JsonProperty("home_team")
    @Field(name = "home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    @Field(name = "away_team")
    private String awayTeam;

    @JsonProperty("outcome")
    @Field(name = "outcome")
    private String outcome;

    @JsonProperty("odd")
    @Field(name = "odd")
    private double odd;


    public Bet(String homeTeam, String awayTeam, String outcome, double odd)
    {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.outcome = outcome;
        this.odd = odd;
    }


    public String getHomeTeam()
    {
        return homeTeam;
    }


    public String getAwayTeam()
    {
        return awayTeam;
    }


    public String getOutcome()
    {
        return outcome;
    }


    public double getOdd()
    {
        return odd;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Bet bet = (Bet) o;
        return bet.odd == odd && Objects.equals(homeTeam, bet.homeTeam) &&
            Objects.equals(awayTeam, bet.awayTeam) && Objects.equals(outcome, bet.outcome);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(homeTeam, awayTeam, outcome, odd);
    }


    @Override
    public String toString()
    {
        return "Bet{" +
            "homeTeam='" + homeTeam + '\'' +
            ", awayTeam='" + awayTeam + '\'' +
            ", outcome='" + outcome + '\'' +
            ", odd=" + odd +
            '}';
    }
}
