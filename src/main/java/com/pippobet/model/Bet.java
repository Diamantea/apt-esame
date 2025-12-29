package com.pippobet.model;

import java.util.Objects;

public class Bet {
    private String homeTeam;
    private String awayTeam;
    private String outcome;
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
