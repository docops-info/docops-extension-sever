<h1>Load Builder from JSON</h1>
<div>
  <article class="flex max-w-xl flex-col items-start justify-between">
    <p class="mt-5 text-sm leading-6 text-gray-600">
      Release Strategies are a way to view the release strategy and the high level planned idea
      for rolling out some feature. That plan entails either a final GA or Milestones or Release candidates.
      This view is intended to demonstrate the use of the JSON dsl to create release strategies.
      <a href="#" data-hx-get="json/schema?name=release-strategy-schema" data-hx-target="#contentBox">JSON Schema</a>
    </p>
  </article>
</div>
<div id="contentBox"></div>
<form data-hx-put="api/release/prefill" data-hx-target="#contentBox" class="max-h-full mx-auto" hx-trigger="submit, load">
    <div class="md-5">
        <label for="payload" class="mb-2">Insert JSON here</label>
        <textarea id="payload" name="payload" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" rows="20" cols="90" placeholder="" data-hx-put="api/release/prefill" data-hx-target="#contentBox">
        {
  "title": "Release Strategy Builder",
  "scale": 0.6,
  "releases": [
    {
      "type": "M1",
      "lines": [
        "Team will deploy to application server current production code",
        "Setup IHS proxy",
        "Team will Mark out websphere servers and mark in liberty servers",
        "Team will validate Okta changes on Liberty",
        "Once validated team will mark out liberty servers",
        "and mark in websphere servers",
        "QA will validate Production is still working",
        "Team will record all observations",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well"
      ],
      "date": "July 30th, 2023",
      "selected": true,
        "completed": true,
      "goal": "Our goal is to Establish the Liberty infrastructure with Okta enabled As this will be an enviroment for shared security model with portal infrastructure."
    },
    {
      "type": "RC1",
      "lines": [
        "Team will deploy to application server current production code",
        "Setup IHS proxy",
        "Team will Mark out websphere servers and mark in liberty servers",
        "Team will validate Okta changes on Liberty",
        "Once validated team will mark out liberty servers",
        "and mark in websphere servers",
        "QA will validate Production is still working",
        "Team will record all observations",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well"
      ],
      "date": "TBD",
      "selected": true,
      "goal": "Our goal is ..."
    },
    {
      "type": "GA",
      "lines": [
        "Team will deploy to application server current production code",
        "Setup IHS proxy",
        "Team will Mark out websphere servers and mark in liberty servers",
        "Team will validate Okta changes on Liberty",
        "Once validated team will mark out liberty servers",
        "and mark in websphere servers",
        "QA will validate Production is still working",
        "Team will record all observations",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well",
        "Team will validate logs and record observations as well"
      ],
      "date": "TBD",
      "selected": true,
      "goal": "Our goal is ..."
    }
  ],
  "style": "TLS",
  "displayConfig": {
    "colors": ["#77CDFF", "#0D92F4", "#2563eb"],
    "fontColor": "#111111"
    }
}
        </textarea>
    </div>
<div class="mb-5">
    <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
</div>
</form>

<#include "stratcommon.ftlh">
<@stratMacro builder = "" fromJson= "btn btn-primary btn-active"/>