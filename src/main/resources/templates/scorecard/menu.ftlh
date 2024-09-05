<h1>Scorecards</h1>


<div role="tablist" class="tabs tabs-boxed">
    <a role="tab" class="tab btnMenu score2" hx-get="scorecard/index.html?type=score2" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Light Theme</a>
    <a role="tab" class="tab btnMenu score-pink" hx-get="scorecard/index.html?type=score-pink" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Pink Theme</a>
    <a role="tab" class="tab btnMenu score-purple" hx-get="scorecard/index.html?type=score-purple" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Purple Theme</a>
    <a role="tab" class="tab btnMenu score-green" hx-get="scorecard/index.html?type=score-green" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Green Theme</a>
    <a role="tab" class="tab btnMenu score-red" hx-get="scorecard/index.html?type=score-red" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Red Theme</a>
    <a role="tab" class="tab btnMenu score-blue" hx-get="scorecard/index.html?type=score-blue" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Blue Theme</a>
    <a role="tab" class="tab btnMenu score-yellow" hx-get="scorecard/index.html?type=score-yellow" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Yellow Theme</a>
    <a role="tab" class="tab btnMenu score1" hx-get="scorecard/index.html?type=score1" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Dark Theme</a>
    <a role="tab" class="tab btnMenu scorecard-schema" hx-get="json/schema?name=scorecard-schema" data-hx-target="#scorecardTarget"><i class="fad fa-curling text-xs mr-2"></i> JSON Schema</a>

</div>


<div id="scorecardTarget">
    <div>
        <h1>Create Scorecard from JSON</h1>
        <div id="contentBox" ></div>
        <div class="divider"></div>
        <form class="grid grid-cols-2 gap-4" data-hx-put="api/scorecard/form" data-hx-target="#contentBox" hx-trigger="submit, load">

            <div class="flex flex-col col-span-2">
                <label for="payload" class="mb-2">JSON</label>
                <textarea id="payload" name="payload" class="textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
{
  "title": "Initiative",
  "initiativeTitle": "Journey Starts",
  "outcomeTitle": "Outcomes Since",
  "initiativeItems": [
    {
      "displayText": "API Versioning",
      "description": "Host API versioning"
    },
    {
      "displayText": "Network Switch",
      "description": "Migrate IBM Websphere to Liberty Server with RHEL 8 and JSESSION ID fix with Application cache busting and versioning"
    },
    {
      "displayText": "MFA",
      "description": "Enabling single signon from server sign-in page"
    },
    {
      "displayText": "Product Elimination - from system"
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
      "displayText": "Software toggle to Enable/Disable OAuth Server"
    }
  ],
  "slideShow": false,
  "scale": 1.0,
  "scoreCardTheme": {
    "titleColor": "#fcfcfc",
    "initiativeTitleColor": "#fcfcfc",
    "outcomeTitleColor":"#fcfcfc",
    "arrowColor": "#132043"
  }
}
            </textarea>
            </div>

            <div class="flex flex-col">
                <button type="submit" class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
            </div>
            <div class="flex flex-col">
                <input class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'scorecard.svg')" value="Download"><img class="htmx-indicator" src="images/bars.svg"/></input>
            </div>

        </form>
    </div>
</div>