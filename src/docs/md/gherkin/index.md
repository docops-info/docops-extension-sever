
# Gherkin BDD Visualization

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/GherkinIcon.svg" alt="Gherkin Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #0d9488; font-size: 32px;">DocOps Gherkin</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Visualize BDD scenarios with beautiful, status-aware test documentation</p>
    </div>
  </div>
</div>

[TOC]

## What is DocOps Gherkin?

DocOps Gherkin transforms Behavior-Driven Development (BDD) scenarios into visually stunning, status-aware documentation. Whether you write tests in standard Gherkin `.feature` files or JSON format, DocOps renders them as clear, color-coded visualizations showing feature descriptions, scenarios, and test execution status.

### Key Features

- **BDD-Native Syntax** - Supports standard Gherkin keywords (Given, When, Then, And, But)
- **Dual Input Formats** - Accept both `.feature` files and structured JSON
- **Status Visualization** - Color-coded status indicators (Passing, Failing, Pending, Skipped)
- **Scenario Outlines** - Full support for parameterized tests with example tables
- **Customizable Themes** - Complete control over colors, typography, and layout
- **Test Documentation** - Living documentation that reflects actual test status
- **Multiple Scenarios** - Display multiple test scenarios within a single feature

<div style="background: #ccfbf1; border-left: 4px solid #14b8a6; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #0f766e; font-weight: 600;">🧪 Living Documentation</p>
  <p style="margin: 8px 0 0 0; color: #0d9488;">Perfect for QA teams, developers, and product managers who want executable specifications that serve as both tests and documentation.</p>
</div>

---

## CI/CD Pipeline Testing

Visualize continuous integration scenarios with status indicators:

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
{ "type": "THEN",  "text": "artifacts are built and stored", "status": "PASSING" },
{ "type": "AND",   "text": "deployment to staging succeeds", "status": "PASSING" }
]
},
{
"title": "Unit tests failing",
"status": "FAILING",
"steps": [
{ "type": "GIVEN", "text": "a merge request is opened", "status": "PASSING" },
{ "type": "WHEN",  "text": "unit tests execute", "status": "FAILING" },
{ "type": "THEN",  "text": "the pipeline should fail and block merge", "status": "FAILING" },
{ "type": "BUT",   "text": "static analysis should still report results", "status": "PENDING" }
]
},
{
"title": "Lint warnings do not fail build",
"status": "PENDING",
"steps": [
{ "type": "GIVEN", "text": "linting is configured as non-blocking", "status": "PASSING" },
{ "type": "WHEN",  "text": "lint warnings are found", "status": "PENDING" },
{ "type": "THEN",  "text": "the pipeline should continue", "status": "PASSING" },
{ "type": "AND",   "text": "a report should be attached to the MR", "status": "SKIPPED" }
]
}
],
"theme": {
"colors": {
"feature": "#5e60ce",
"scenario": "#f0f4f8",
"given": "#3a86ff",
"when": "#fb5607",
"then": "#2ec4b6",
"and": "#6c757d",
"but": "#8338ec",
"passing": "#2eb85c",
"failing": "#e03131",
"pending": "#f59f00",
"skipped": "#868e96"
},
"layout": { "width": 720, "height": 460, "padding": 24, "scenarioSpacing": 34, "stepSpacing": 26 },
"typography": { "featureSize": 20, "scenarioSize": 16, "stepSize": 12, "descriptionSize": 10, "fontFamily": "Inter, Arial, sans-serif" }
}
}

[/docops]

---

## User Authentication Feature

Standard Gherkin format showing login scenarios:

[docops:gherkin]
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
[/docops]

---

## Scenario Outline with Examples

Parameterized testing with data-driven scenarios:

[docops:gherkin caption="User Login Validation"]
Feature: User Login Validation

    Scenario Outline: System authenticates users based on roles
        Given the login page is displayed
        When the user enters "<username>" and "<password>"
        Then the system should grant access to the "<dashboard>"

    Examples:
      | username | password  | dashboard |
      | admin    | secret123 | Admin     |
      | editor   | edit456   | Content   |
      | viewer   | view789   | Guest     |
[/docops]

---

## Navigation Testing

Verify UI navigation with example tables:

[docops:gherkin title="Tab Navigation Verification"]
Scenario Outline: Tab names are correct
Given a user is logged in to TE Reporting
When I navigate to the following url - <url>
Then the tab name is: <tab_name>

    Examples:
      | url                         | tab_name               |
      | /                           | Home                   |
      | /dashboards/summary         | All Dashboards         |
[/docops]

---

## Circuit Breaker Resilience

Document distributed system resilience patterns:

[docops:gherkin]
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
[/docops]

---

## Format Options

### Standard Gherkin Format

Use classic `.feature` file syntax:

```gherkin 

Feature: Feature Name
Scenario: Scenario Description
Given [precondition] 
When [action] 
Then [expected outcome]
And [additional outcome]

```

### JSON Format with Status

Add execution status and custom theming:


```json

```

### Scenario Outline Format

Parameterized tests with example tables:

```gherkin 
Feature: Feature Name

Scenario Outline: Parameterized scenario 
Given [precondition with ] 
When [action with ] 
Then [outcome with ]

Examples: 
| parameter | expected | 
| value1 | result1 | 
| value2 | result2 |
```


### Color Customization

**Feature Colors:**
- `feature` - Feature header background
- `scenario` - Scenario card background

**Step Type Colors:**
- `given` - Given step color
- `when` - When step color
- `then` - Then step color
- `and` - And step color
- `but` - But step color

**Status Colors:**
- `passing` - Successful test indicator
- `failing` - Failed test indicator
- `pending` - Pending test indicator
- `skipped` - Skipped test indicator

### Layout Options

| Property | Description | Default |
|----------|-------------|---------|
| `width` | Canvas width in pixels | `720` |
| `height` | Canvas height in pixels | `460` |
| `padding` | Inner padding | `24` |
| `scenarioSpacing` | Space between scenarios | `34` |
| `stepSpacing` | Space between steps | `26` |

### Typography Options

| Property | Description | Default |
|----------|-------------|---------|
| `featureSize` | Feature title font size | `20` |
| `scenarioSize` | Scenario title font size | `16` |
| `stepSize` | Step text font size | `12` |
| `descriptionSize` | Description font size | `10` |
| `fontFamily` | Font stack | `Inter, Arial, sans-serif` |

---

## Best Practices

### Writing Effective Scenarios

- **Clear Language** - Use business-friendly terminology
- **One Scenario, One Behavior** - Keep scenarios focused
- **Declarative Over Imperative** - Describe what, not how
- **Reusable Steps** - Design steps for reuse across scenarios

### Status Management

- **Update Regularly** - Keep status current with test runs
- **CI/CD Integration** - Automate status updates from test pipelines
- **Granular Status** - Set status at both scenario and step level
- **Visual Scanning** - Use color coding for quick test health assessment

### Organization

- **Group Related Scenarios** - Keep related tests in the same feature
- **Meaningful Feature Names** - Clear, descriptive feature titles
- **Scenario Ordering** - Happy path first, then edge cases
- **Example Tables** - Use scenario outlines for data variations

### Documentation

- **Living Specs** - Treat Gherkin as executable specifications
- **Stakeholder Review** - Share with non-technical stakeholders
- **Version Control** - Track changes alongside code
- **Link to Implementation** - Reference step definitions

---

## Common Use Cases

### Acceptance Testing
Document user acceptance criteria as executable specifications that verify feature completeness.

### Regression Testing
Create comprehensive regression test suites that double as feature documentation.

### API Testing
Describe API behavior with Given-When-Then scenarios showing requests and expected responses.

### Integration Testing
Document integration points and data flows between systems.

### Performance Testing
Specify performance expectations and load scenarios.

### Security Testing
Describe security requirements and authorization scenarios.

---

## Integration Patterns

### CI/CD Pipeline Integration

```yaml
# Example: GitHub Actions
- name: Run BDD tests and export JSON
  run: npm run test:bdd -- --format json > test-results.json
- name: Generate DocOps Gherkin diagram
  run: docops-convert gherkin test-results.json > docs/test-report.svg
- name: Upload DocOps artifact
  uses: actions/upload-artifact@v4
  with:
    name: gherkin-report
    path: docs/test-report.svg
```

### Test Automation Frameworks

- **Cucumber** - Direct `.feature` file support
- **Behave** (Python) - Export to JSON format
- **SpecFlow** (.NET) - Convert to DocOps JSON
- **JBehave** (Java) - JSON report transformation

### Documentation Sites

Embed Gherkin visualizations in:
- **Confluence** - Living test documentation
- **GitHub Pages** - Test status dashboards
- **Internal Wikis** - QA knowledge bases
- **API Docs** - Endpoint behavior specifications

---

## Advanced Examples

### Multi-Scenario Feature with Mixed Status

```json 
{
  "feature": "Payment Processing",
  "scenarios": [
    {
      "title": "Happy path payment",
      "status": "PASSING",
      "steps": [
        { "type": "GIVEN", "text": "a customer has a valid card", "status": "PASSING" },
        { "type": "WHEN", "text": "they submit a $50 charge", "status": "PASSING" },
        { "type": "THEN", "text": "the payment is authorized", "status": "PASSING" },
        { "type": "AND", "text": "a receipt is generated", "status": "PASSING" }
      ]
    },
    {
      "title": "Expired card is rejected",
      "status": "FAILING",
      "steps": [
        { "type": "GIVEN", "text": "a customer has an expired card", "status": "PASSING" },
        { "type": "WHEN", "text": "they submit a $25 charge", "status": "FAILING" },
        { "type": "THEN", "text": "the payment is declined", "status": "FAILING" },
        { "type": "AND", "text": "a decline reason is returned", "status": "FAILING" }
      ]
    },
    {
      "title": "Gateway timeout is retried",
      "status": "PENDING",
      "steps": [
        { "type": "GIVEN", "text": "the payment gateway is slow", "status": "PASSING" },
        { "type": "WHEN", "text": "a charge request times out", "status": "PENDING" },
        { "type": "THEN", "text": "the system retries once", "status": "PENDING" },
        { "type": "BUT", "text": "the user is notified of delay", "status": "SKIPPED" }
      ]
    }
  ]
}
```

---

<div align="center" style="margin-top: 48px; padding: 24px; background: #fafbfc; border-radius: 8px;">
  <p style="color: #64748b; margin: 0;">Ready to visualize your BDD scenarios?</p>
  <p style="color: #0d9488; font-weight: 600; margin: 8px 0 0 0;">Create living test documentation with DocOps Gherkin</p>
</div>
