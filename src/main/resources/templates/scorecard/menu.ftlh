<h1>Scorecards</h1>
<div>
    <article class="flex max-w-xl flex-col items-start justify-between">
        <p class="mt-5 text-sm leading-6 text-gray-600">
            This view allows developers view and create scorecards or comparison charts to highlight before and after or pros and cons
            views within AsciiDoctor documents. This is a very powerful tool that can be used in many different ways
            depending on the needs of your project documentation.
        </p>
        <div data-hx-get="json/schema?name=scorecard-schema" data-hx-target="#scorecardTarget">
            Click here to view the JSON Schema for Scorecards
        </div>
    </article>
</div>
<ul class="flex flex-wrap text-sm font-medium text-center text-gray-500 border-b border-gray-200 dark:border-gray-700 dark:text-gray-400">
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" data-hx-get="scorecard/index.html?type=score2" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Light</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="scorecard/index.html?type=score-pink" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Pink</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="scorecard/index.html?type=score-purple" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Purple</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="scorecard/index.html?type=score-green" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Green</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="scorecard/index.html?type=score-red" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Red</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="scorecard/index.html?type=score-blue" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Blue</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="scorecard/index.html?type=score-yellow" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Yellow</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="scorecard/index.html?type=score1" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Dark</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="comp.html" data-hx-target="#scorecardTarget"><i class="fad fa-circle-half-stroke text-xs mr-2"></i> Comparison Chart</a>
    </li>
</ul>


<div id="scorecardTarget">
    <div>
        <h1>Create Scorecard from JSON</h1>
        <div id="contentBox" ></div>
        <div class="divider"></div>
        <form class="max-h-full mx-auto" data-hx-put="api/scorecard/form" data-hx-target="#contentBox" hx-trigger="submit, load">

            <div class="mb-5">
                <label for="payload" class="mb-2">JSON</label>
                <textarea id="payload" name="payload" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
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
                <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
            </div>
            <div class="flex flex-col">
                <input class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'scorecard.svg')" value="Download"><img class="htmx-indicator" src="images/bars.svg"/></input>
            </div>

        </form>
    </div>
</div>