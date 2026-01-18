Feature: Delete bet

  Scenario: Delete a bet via REST API
    Given 2 bets in the Database
    When the bet is deleted via REST endpoint
    Then the response status code is 200
    And the bet is removed from the Database

  Scenario: Delete a bet via web interface
    Given 2 bets in the Database
    When the user navigates to the bets page
    And the user clicks the delete button for the first bet
    Then the success message is displayed
    And the bet is removed from the Database