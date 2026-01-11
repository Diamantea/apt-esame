package com.pippobet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BetCreateDTO {
    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    private String outcome;
    private double odd;

    public BetCreateDTO() {
    }

    public BetCreateDTO(String homeTeam, String awayTeam, String outcome, double odd) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.outcome = outcome;
        this.odd = odd;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public double getOdd() {
        return odd;
    }

    public void setOdd(double odd) {
        this.odd = odd;
    }
}