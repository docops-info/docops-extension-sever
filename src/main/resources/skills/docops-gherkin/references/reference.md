# DocOps Gherkin Syntax Reference

Visualizes Behavior-Driven Development (BDD) scenarios with Given-When-Then formatting and status indicators.

## Asciidoc Format

### Plain Gherkin Text

```asciidoc
[docops,gherkin,controls=true]
----
Feature: User Authentication
  Scenario: Successful Login
    Given the user is on the login page
    When they enter valid credentials
    Then they should be redirected to dashboard
    And the login attempt should be logged
----
```

### JSON with Status Indicators

```asciidoc
[docops,gherkin]
----
{
  "feature": "User Authentication",
  "scenarios": [
    {
      "title": "Failed Login",
      "status": "FAILING",
      "steps": [
        { "type": "GIVEN", "text": "the user is on the login page", "status": "PASSING" },
        { "type": "WHEN", "text": "they enter invalid credentials", "status": "FAILING" },
        { "type": "THEN", "text": "they should see an error message", "status": "PENDING" }
      ]
    }
  ]
}
----
```

## Markdown Format

### Plain Gherkin Text

```md
[docops:gherkin]
Feature: User Authentication
  Scenario: Successful Login
    Given the user is on the login page
    When they enter valid credentials
    Then they should be redirected to dashboard
[/docops]
```

### JSON with Status Indicators

```md
[docops:gherkin]
{
  "feature": "Continuous Integration Pipeline",
  "scenarios": [
    {
      "title": "Successful build and deploy",
      "status": "PASSING",
      "steps": [
        { "type": "GIVEN", "text": "code is pushed to main", "status": "PASSING" },
        { "type": "WHEN",  "text": "the build pipeline runs", "status": "PASSING" },
        { "type": "THEN",  "text": "artifacts are built and stored", "status": "PASSING" }
      ]
    }
  ]
}
[/docops]
```

## Keywords & Syntax

### Step Keywords
- **Given**: Initial context or preconditions (Blue).
- **When**: Action or event (Orange).
- **Then**: Expected outcome or assertion (Green).
- **And**: Additional step in same category (Gray).
- **But**: Exception or negative condition (Gray).

### Status Types (JSON only)
- `PASSING`: Green (#28a745 / #2eb85c)
- `FAILING`: Red (#dc3545 / #e03131)
- `PENDING`: Yellow (#ffc107 / #f59f00)
- `SKIPPED`: Gray (#6c757d / #868e96)

## Options

- `useDark=true`: Enable dark theme.
- `scale=1.5`: Adjust size (default: 1.0).
- `controls=true`: Show export/interactivity controls.
- `theme`: Custom JSON theme configuration for colors, layout, and typography.

## Best Practices

1. **Keep steps atomic**: One action or assertion per step.
2. **Use AND for lists**: Chain related preconditions without repeating Given/When/Then.
3. **Include edge cases**: Show both happy path and error scenarios.
4. **Reserve JSON for status**: Use plain text for initial specs, JSON for CI/CD test reports.
5. **Declarative Over Imperative**: Describe *what*, not *how*.
