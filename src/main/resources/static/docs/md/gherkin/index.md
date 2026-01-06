# DocOps Gherkin


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

