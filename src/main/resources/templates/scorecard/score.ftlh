<div>
    <h1>Create Scorecard from JSON</h1>

    <form class="pure-form pure-form-stacked" data-hx-put="api/scorecard/form" data-hx-target="#contentBox">

        <fieldset id="response-div" >
            <legend>JSON</legend>
            <div class="pure-1-2">
                <label for="payload">Insert content here</label>
            <textarea id="payload" name="payload" class="textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
            {
  "title": "Release Strategies",
  "initiativeTitle": "Initiative",
  "outcomeTitle": "Outcomes",
  "initiativeItems": [
    {
      "displayText": "API Versioning",
      "description": "TravHost API versioning"
    },
    {
      "displayText": "Network Switch",
      "description": "Migrate IBM Websphere to Liberty Server with RHEL 8 and JSESSION ID fix with Application cache busting and versioning"
    },
    {
      "displayText": "Geico MFA",
      "description": "Enabling single signon from Travelers sign-in page"
    },
    {
      "displayText": "Siteminder Elimination - PI Portal"
    }
  ],
  "outcomeItems": [
    {
      "displayText": "API Versioning changes with backwards compatibility"
    },
    {
      "displayText": "Network Mark-In/Mark-Out Servers"
    },
    {
      "displayText": "Phased Rollout, Milestones and Release Candidate"
    },

    {
      "displayText": "Software toggle to Enable/Disable OKTA"
    }
  ],
  "scale": 1.0,
  "scoreCardTheme": {
    "titleColor": "#E7D6B7",
    "initiativeTitleColor": "#E7D6B7",
    "outcomeTitleColor":"#E7D6B7",
    "backgroundColor": "#2c445a",
    "initiativeBackgroundColor": "#111111",
    "outcomeBackgroundColor": "#111111",
    "initiativeDisplayTextColor": "#fcfcfc",
    "outcomeDisplayTextColor":"#fcfcfc",
    "arrowColor": "#E7D6B7"
  }
}
            </textarea>
            </div>
        </fieldset>
        <fieldset>
            <button class="btn btn-primary">Submit</button>
            <button class="btn btn-secondary" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'scorecard.svg')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
        </fieldset>
    </form>
</div>
<div class="carousel rounded-box">
<div id="contentBox" class="contentBox carousel-item"></div>
</div>