# DocOps Markdown Support
[TOC]

DocOps Extensions can now be embedded in Markdown Files. Using the DocOps converter, you can seamlessly integrate visualizations, diagrams, and interactive components directly into your Markdown documents. This allows for a more engaging and informative way to present information, making it easier for readers to understand complex concepts and processes.


[docops:buttons]
{
  "buttons": [
    {
      "label": "ADR",
      "link": "adr/adr.html",
      "description": "Architecture Decision Records",
        "embeddedImage": {
        "ref": "images/ADRIcon.svg"
        },
      "type": "adr"
    },
    {
      "label": "Buttons",
      "link": "buttons/buttons.html",
      "description": "Buttons",
        "embeddedImage": {
        "ref": "images/ButtonIcon.svg"
        },
      "type": "buttons"
    },
    {
      "label": "Callouts",
      "link": "callout/index.html",
      "description": "Callout",
      "embeddedImage": {
      "ref": "images/CalloutIcon.svg"
      },
      "type": "callout"
    },
    {
      "label": "Charts",
      "link": "charts/index.html",
      "description": "Generate reports",
      "embeddedImage": {
      "ref": "images/ChartIcon.svg"
      },
            "type": "charts"
    },
    {
      "label": "Connectors",
      "link": "connector/index.html",
      "description": "Connectors",
      "type": "connectors"
    },
    {
      "label": "Domain Visualization",
      "link": "domain/index.html",
      "description": "Domain Visualization",
      "type": "domain"
    },{
        "label": "Gherkin",
        "link": "gherkin/index.html",
        "description": "Gherkin Visualized",
        "type": "gherkin"
    },{
        "label": "Metrics Card",
        "link": "metrics/index.html",
        "description": "Metrics Cards",
        "type": "metrics"
    },{
        "label": "Planner",
        "link": "planner/index.html",
        "description": "Planner Cards",
        "type": "Plan"
    },{
        "label": "Scorecard",
        "link": "scorecard/index.html",
        "description": "Scorecards",
        "type": "scorecard"
    },{
        "label": "Badges And Shield",
        "link": "badge/index.html",
        "description": "Badges and Shield",
        "embeddedImage": {
            "ref": "images/BadgeShieldIcon.svg"
        },
        "type": "badge"
    },{
        "label": "Timeline",
        "link": "timeline/index.html",
        "description": "Timeline Visual",
        "type": "timeline"
    },{
        "label": "Wordcloud",
        "link": "wordcloud/index.html",
        "description": "Wordclouds",
        "type": "wordcloud"
    },{
        "label": "Tree Chart",
        "link": "treechart/index.html",
        "description": "Tree Chart - Experimental",
        "type": "Tree Chart"
    }
  ],
  "buttonType": "LARGE",
  "theme": {
    "colors": [
        "#1e3a8a"
    ],
    "hexLinesEnabled": false,
    "strokeColor": "#3498db",
    "newWin": true,
    "columns": 4,
    "scale": 1.5,
    "buttonStyle": {
      "labelStyle": "font-family: 'Poppins', sans-serif; font-size: 44px; fill: #fcfcfc; font-weight: bold;",
      "descriptionStyle": "font-family: 'Poppins', sans-serif; font-size: 12px; fill: #fcfcfc;"
    }
  }
}
[/docops]