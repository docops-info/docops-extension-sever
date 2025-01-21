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
  "scale": 1.0,
  "releases": [
    {
      "type": "M1",
      "lines": [
        "Team will deploy application and build out infrastructure with Terraform scripts.",
        "Team will Apply API gateway pattern to establish API version infrastructure.",
        "Tream will validate access to the application",
        "Team will shutdown infrastructure as security is not in place."
      ],
      "date": "July 30th, 2023",
      "selected": true,
      "goal": "Our Goal is to provision new infrastructure on our cloud EKS platform without enabling production traffic",
        "completed": true
    },
    {
      "type": "RC1",
      "lines": [
        "Team will leverage CICD pipeline to deploy latest code",
        "Team will enable OAuth security on the API Gateway",
        "Team will make the application communication private and local to the API Gateway",
        "Team will enable API throttling at the Gateway layer",
        "Team will have QA do initial testing."
      ],
      "date": "September 20th, 2023",
       "completed": true
      "goal": "Our goal is to deploy the lastest code along with security patches applied at the API Layer"
    },
    {
      "type": "GA",
      "lines": [
        "Team will deploy latest code.",
        "QA will test and sign off"
      ],
      "date": "September 30th",
      "selected": true,
      "goal": "Our goal is to release version 1.0 of API making it generally available to all consumers."
    }
  ],
  "style": "TLS",
  "displayConfig": {
    "colors": [ "#E73879", "#074799", "#118B50"],
    "fontColor": "#fcfcfc",
    "notesVisible": true
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