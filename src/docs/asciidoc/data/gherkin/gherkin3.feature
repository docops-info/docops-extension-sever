Feature: Circuit Breaker Behavior
  Scenario: Open on Consecutive Failures
    Given the downstream payment service is failing
    When three consecutive calls fail
    Then the circuit breaker should open
    And subsequent calls should be short-circuited

  Scenario: Half-Open Probe
    Given the circuit breaker is open for 60s
    When the cooldown elapses
    Then a single probe request should be allowed
    And if it succeeds the circuit should close
    But if it fails the circuit should stay open