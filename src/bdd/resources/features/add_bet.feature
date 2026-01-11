Feature: Add bet

  Scenario: Create a new bet via REST endpoint
    Given a new bet with home team "Inter", away team "Milan", outcome "1", and odd 1.75
    When the bet is created via REST endpoint
    Then the bet is successfully created
    And the bet is persisted in the Database
