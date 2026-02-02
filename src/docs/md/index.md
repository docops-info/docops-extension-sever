# DocOps Markdown Support

DocOps Extensions can now be embedded in Markdown Files. Using the DocOps converter, you can seamlessly integrate visualizations, diagrams, and interactive components directly into your Markdown documents. This allows for a more engaging and informative way to present information, making it easier for readers to understand complex concepts and processes.

## What's New*

[Realtime Editor](../../editor/index.html)

## Navigation 

### Charts & Metrics

Visualize data with charts, gauges, and metric displays.


[docops:buttons]
{
  "buttons": [
    {
      "label": "BAR CHARTS",
      "link": "charts/index.html#bar-charts",
      "description": "Category comparisons",
      "activeName": "barchart",
      "type": "chart"
    },
    {
      "label": "PIE CHARTS",
      "link": "charts/index.html#pie-charts",
      "description": "Proportional data",
      "activeName": "piecharts",
      "type": "chart"
    },
    {
      "label": "LINE CHARTS",
      "link": "charts/index.html#line-charts",
      "description": "Trends over time",
      "activeName": "linecharts",
      "type": "chart"
    },
    {
      "label": "COMBINATION",
      "link": "charts/index.html#combination-charts",
      "description": "Mixed chart types",
      "activeName": "combination",
      "type": "chart"
    },
    {
      "label": "GAUGE",
      "link": "charts/index.html#gauge-charts",
      "description": "Single metrics",
      "activeName": "gauge",
      "type": "chart"
    },
    {
      "label": "METRICS CARD",
      "link": "metrics/index.html",
      "description": "KPI snapshots",
      "activeName": "metricscard",
      "type": "chart"
    }
  ],
  "buttonType": "ROUND",
  "theme": {
    "colors": ["#06B6D4"],
    "activeColor": "#00f5ff",
    "useActiveColor": true,
    "columns": 6,
    "newWin": true,
    "scale": 1.0,
    "buttonStyle": {
      "labelStyle": "font-family: 'Outfit', sans-serif; font-size: 11px; fill: #fcfcfc; font-weight: bold;",
      "descriptionStyle": "font-family: 'Outfit', sans-serif; font-size: 9px; fill: #94a3b8;"
    }
  }
}
[/docops]



### Navigation & Organization

Structure your documentation with scorecards, timelines, and navigation buttons.


[docops:buttons]
{
  "buttons": [
    {
      "label": "SCORECARDS",
      "link": "scorecard/index.html",
      "description": "Before/after comparisons",
      "activeName": "scorecard",
      "type": "navigation"
    },
    {
      "label": "TIMELINE",
      "link": "timeline/index.html",
      "description": "Chronological events",
      "activeName": "timeline",
      "type": "navigation"
    },
    {
      "label": "BUTTONS",
      "link": "buttons/buttons.html",
      "description": "Navigation grids",
      "activeName": "buttons",
      "type": "navigation"
    }
  ],
  "buttonType": "ROUND",
  "theme": {
    "colors": ["#8B5CF6"],
    "activeColor": "#00f5ff",
    "useActiveColor": true,
    "newWin": true,
    "columns": 3,
    "buttonStyle": {
      "labelStyle": "font-family: 'Outfit', sans-serif; font-size: 11px; fill: #fcfcfc; font-weight: bold;",
      "descriptionStyle": "font-family: 'Outfit', sans-serif; font-size: 9px; fill: #94a3b8;"
    }
  }
}
[/docops]

### Process & Planning

Plan releases, map priorities, and visualize workflows.


[docops:buttons]
{
  "buttons": [
    {
      "label": "CALLOUTS",
      "link": "callout/index.html",
      "description": "Highlighted processes",
      "activeName": "callout",
      "type": "process"
    },
    {
      "label": "PLANNER",
      "link": "planner/index.html",
      "description": "Kanban roadmaps",
      "activeName": "planner",
      "type": "process"
    },
    {
      "label": "CONNECTORS",
      "link": "connector/index.html",
      "description": "Workflow arrows",
      "activeName": "connectors",
      "type": "process"
    },
    {
      "label": "QUADRANT",
      "link": "charts/index.html#magic-quadrant-chart",
      "description": "2D priority mapping",
      "activeName": "quadrant",
      "type": "process"
    }
  ],
  "buttonType": "CIRCLE",
  "theme": {
    "colors": ["#10B981"],
    "activeColor": "#00f5ff",
    "useActiveColor": true,
    "newWin": true,
    "columns": 5,
    "buttonStyle": {
      "labelStyle": "font-family: 'Outfit', sans-serif; font-size: 11px; fill: #fcfcfc; font-weight: bold;",
      "descriptionStyle": "font-family: 'Outfit', sans-serif; font-size: 9px; fill: #94a3b8;"
    }
  }
}
[/docops]

### Technical & Architecture

Document architecture, tests, and hierarchies.

[docops:buttons]
{
  "buttons": [
    {
      "label": "WORD CLOUDS",
      "link": "wordcloud/index.html",
      "description": "Text hierarchies",
      "activeName": "wordcloud",
      "type": "technical"
    },
    {
      "label": "DOMAIN",
      "link": "domain/index.html",
      "description": "Architecture maps",
      "activeName": "domain",
      "type": "technical"
    },
    {
      "label": "GHERKIN",
      "link": "gherkin/index.html",
      "description": "BDD scenarios",
      "activeName": "gherkin",
      "type": "technical"
    },
    {
      "label": "TREE CHART",
      "link": "treechart/index.html",
      "description": "Hierarchies",
      "activeName": "treechart",
      "type": "technical"
    },
    {
      "label": "TREEMAP",
      "link": "treemap/index.html",
      "description": "Proportional boxes",
      "activeName": "treemap",
      "type": "technical"
    }
  ],
  "buttonType": "CIRCLE",
  "theme": {
    "colors": ["#F59E0B"],
    "activeColor": "#00f5ff",
    "useActiveColor": true,
    "newWin": true,
    "columns": 5,
    "buttonStyle": {
      "labelStyle": "font-family: 'Outfit', sans-serif; font-size: 11px; fill: #fcfcfc; font-weight: bold;",
      "descriptionStyle": "font-family: 'Outfit', sans-serif; font-size: 9px; fill: #94a3b8;"
    }
  }
}
[/docops]

### Metadata & Status

Track decisions, tasks, and team information.

[docops:buttons]
{
  "buttons": [
    {
      "label": "BADGES",
      "link": "badge/index.html",
      "description": "Status indicators",
      "activeName": "shield",
      "type": "metadata"
    },
    {
      "label": "ADR",
      "link": "adr/adr.html",
      "description": "Architecture decisions",
      "activeName": "adr",
      "type": "metadata"
    }
  ],
  "buttonType": "CIRCLE",
  "theme": {
    "colors": ["#EF4444"],
    "activeColor": "#00f5ff",
    "useActiveColor": true,
    "newWin": true,
    "columns": 6,
    "buttonStyle": {
      "labelStyle": "font-family: 'Outfit', sans-serif; font-size: 11px; fill: #fcfcfc; font-weight: bold;",
      "descriptionStyle": "font-family: 'Outfit', sans-serif; font-size: 9px; fill: #94a3b8;"
    }
  }
}
[/docops]