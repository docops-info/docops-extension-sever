Feature: Search Suggestions
  Scenario: Show Suggestions While Typing
    Given a user is on the search page
    When they type the first three characters
    Then the top 5 matching suggestions should appear
    And selecting a suggestion should populate the input