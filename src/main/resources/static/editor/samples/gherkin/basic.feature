Feature: User Authentication
  Scenario: Successful Login
    Given the user is on the login page
    When they enter valid credentials
    Then they should be redirected to dashboard
    And the login attempt should be logged

  Scenario: Failed Login
    Given the user is on the login page
    When they enter invalid credentials
    Then they should see an error message
    And they should remain on the login page