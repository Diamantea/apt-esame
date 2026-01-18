Feature: Delete bet

  Scenario: Delete a bet via REST API
    Given 2 bets in the Database
    When the bet is deleted via REST endpoint
    Then the response status code is 200
    And the bet is removed from the Database