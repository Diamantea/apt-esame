Feature: Show all available bets

  Scenario: Show all available bets in REST endpoint
    Given 2 bets in the Database
    When get all bets from REST endpoint
    Then bets are correctly returned in the response

  Scenario: Show all available bets in view endpoint
    Given 3 bets in the Database
    When get all bets from view endpoint
    Then view endpoint returns success response with bets displayed
