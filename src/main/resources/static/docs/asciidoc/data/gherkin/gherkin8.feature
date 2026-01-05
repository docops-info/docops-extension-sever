Feature: User Login, As a user, I want to be able to log in to the system.

  Scenario: Successful Login
    Given I am on the login page
    When I enter a valid username and password
    Then I should be redirected to the homepage
    And I should see a welcome message

  Scenario: Invalid Login
    Given I am on the login page
    When I enter an invalid username and password
    Then I should see an error message

  Scenario Outline: Login with different users
    Given I am on the login page
    When I enter username  and password
    Then I should see the message
    Examples:
      | username | password | message |
      | user1 | pass1 | Welcome |
      | user2 | wrong_password | Invalid credentials |