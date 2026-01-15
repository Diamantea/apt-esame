Feature: Add bet

  Scenario: Create a new bet via REST endpoint
    Given a new bet with home team "Inter", away team "Milan", outcome "1", and odd 1.75
    When the bet is created via REST endpoint
    Then the bet is successfully returned in the response
    And the bet is persisted in the Database

  Scenario: Create a new bet via web form
    When the user navigates to the bets page
    And the user fills the form with home team "Juventus", away team "Roma", outcome "X", and odd 2.50
    And the user submits the form
    Then the success message is displayed
    And the bet is persisted in the Database with home team "Juventus", away team "Roma", outcome "X", and odd 2.50

