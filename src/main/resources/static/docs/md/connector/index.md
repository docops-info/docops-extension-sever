
# Process Flow Connectors

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/ConnectorIcon.svg" alt="Connector Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #6366f1; font-size: 32px;">DocOps Connectors</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Visualize process flows and sequential workflows with connected nodes</p>
    </div>
  </div>
</div>

[TOC]

## What are DocOps Connectors?

DocOps Connectors create beautiful, sequential flow visualizations that show how different stages, processes, or components connect together. Perfect for documenting workflows, CI/CD pipelines, data flows, and process chains.

### Key Features

- **Sequential Visualization** - Clearly shows step-by-step progression through connected nodes
- **Color-Coded Stages** - Each step can have its own distinct color for visual clarity
- **Rich Descriptions** - Add context to each node with detailed descriptions
- **Flexible Layout** - Automatic spacing and arrangement of connected elements
- **Gradient Support** - Beautiful color gradients for modern, professional appearance
- **Responsive Design** - Scales elegantly from small embeds to full-width displays

<div style="background: #ede9fe; border-left: 4px solid #8b5cf6; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #6b21a8; font-weight: 600;">🎯 Perfect For</p>
  <p style="margin: 8px 0 0 0; color: #7c3aed;">CI/CD pipelines, development workflows, data processing chains, deployment stages, and any sequential process that needs clear visual communication.</p>
</div>

---

## Basic CI/CD Pipeline

A common use case—documenting your continuous integration and deployment pipeline:

[docops:connector]
{
"connectors": [
{
"text": "Code Commit",
"baseColor": "#2d00f7",
"description": "Developer pushes code to repository"
},
{
"text": "Automated Tests",
"baseColor": "#6a00f4",
"description": "Unit and integration tests run"
},
{
"text": "Code Quality",
"baseColor": "#8900f2",
"description": "Static analysis and code review"
},
{
"text": "Build",
"baseColor": "#a100f2",
"description": "Compiling and packaging application"
},
{
"text": "Artifact Storage",
"baseColor": "#b100e8",
"description": "Storing build artifacts"
},
{
"text": "Staging Deploy",
"baseColor": "#bc00dd",
"description": "Deploying to staging environment"
},
{
"text": "Acceptance Tests",
"baseColor": "#d100d1",
"description": "Automated acceptance testing"
},
{
"text": "Production Deploy",
"baseColor": "#db00b6",
"description": "Deploying to production environment"
},
{
"text": "Monitoring",
"baseColor": "#e500a4",
"description": "Continuous monitoring and alerting"
}
]
}
[/docops]

---

## Software Development Lifecycle

Illustrate traditional SDLC phases with distinct color coding:

[docops:connector]
{
"connectors": [
{
"text": "Requirements",
"baseColor": "#4361ee",
"description": "Gathering and analyzing requirements"
},
{
"text": "Design",
"baseColor": "#3a0ca3",
"description": "Creating software architecture and design"
},
{
"text": "Implementation",
"baseColor": "#7209b7",
"description": "Writing code based on design"
},
{
"text": "Testing",
"baseColor": "#f72585",
"description": "Verifying software quality"
},
{
"text": "Deployment",
"baseColor": "#4cc9f0",
"description": "Releasing software to production"
},
{
"text": "Maintenance",
"baseColor": "#4895ef",
"description": "Ongoing support and updates"
}
]
}
[/docops]

---

## Test Documentation Workflow

Show how automated tests generate documentation:

[docops:connector]
{
"connectors": [
{
"text": "Developer",
"baseColor": "#E14D2A",
"description": "Writes unit tests"
},
{
"text": "Unit Tests",
"baseColor": "#82CD47",
"description": "Unit tests produces excel"
},
{
"text": "Microsoft Excel",
"baseColor": "#687EFF",
"description": "Excel is stored in test engine"
},
{
"text": "Test Engine",
"baseColor": "#C02739",
"description": "Test Engine write documentation"
},
{
"text": "API Documentation Output",
"baseColor": "#FEC260",
"description": "Documentation is committed"
}
]
}
[/docops]

---

## Data Processing Pipeline

Visualize ETL (Extract, Transform, Load) processes:

[docops:connector]
{
"connectors": [
{
"text": "Data Source",
"baseColor": "#06b6d4",
"description": "Raw data from multiple sources"
},
{
"text": "Extract",
"baseColor": "#0891b2",
"description": "Pull data from source systems"
},
{
"text": "Validate",
"baseColor": "#0e7490",
"description": "Data quality checks"
},
{
"text": "Transform",
"baseColor": "#155e75",
"description": "Clean and structure data"
},
{
"text": "Enrich",
"baseColor": "#164e63",
"description": "Add calculated fields and metadata"
},
{
"text": "Load",
"baseColor": "#083344",
"description": "Write to data warehouse"
},
{
"text": "Analytics",
"baseColor": "#0c4a6e",
"description": "Business intelligence and reporting"
}
]
}
[/docops]

---

## Component Structure

Each connector node consists of:

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| **text** | String | Yes | The label displayed in the node |
| **baseColor** | String | No | Hex color code (default: `#E14D2A`) |
| **description** | String | No | Contextual information about this step |

### JSON Structure

```json 
{
  "connectors": [
    {
      "text": "Step Name",
      "baseColor": "#6366f1",
      "description": "What happens in this step"
    },
    {
      "text": "Next Step",
      "baseColor": "#8b5cf6",
      "description": "Following action in the process"
    },
    {
      "text": "Final Step",
      "baseColor": "#a78bfa",
      "description": "Completion of the workflow"
    }
  ]
}
```



<div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #92400e; font-weight: 600;">💡 Color Strategy</p>
  <p style="margin: 8px 0 0 0; color: #b45309;">Use gradient color progressions (like varying shades of blue or purple) for cohesive flows, or contrasting colors to highlight different types of steps (e.g., development vs. testing vs. deployment).</p>
</div>

---

## Design Best Practices

### Visual Coherence
- **Use color gradients** - Create smooth visual progression through related stages
- **Limit text length** - Keep node labels concise (1-3 words)
- **Group related steps** - Use similar colors for related phases

### Clarity & Communication
- **Meaningful descriptions** - Provide context that explains the "why" and "how"
- **Logical flow** - Order connectors to match the actual process sequence
- **Appropriate granularity** - Not too many steps (5-12 is ideal)

### Color Accessibility
- **Sufficient contrast** - Ensure text is readable on colored backgrounds
- **Distinct colors** - Make sure adjacent nodes are visually distinguishable
- **Consider color blindness** - Use color combinations that work for all users

---

## Common Use Cases

### DevOps & CI/CD
Document build pipelines, deployment workflows, and automated testing sequences.

### Business Processes
Map customer journeys, approval workflows, and operational procedures.

### Data Flows
Illustrate how data moves through ETL pipelines, microservices, or integration layers.

### Development Workflows
Show how code progresses from development through testing to production.

### Onboarding & Training
Create visual guides for new team members to understand processes.

---

## Advanced Examples

### Multi-Environment Deployment

[docops:connector]
{
"connectors": [
{
"text": "Build",
"baseColor": "#10b981",
"description": "Compile and package application"
},
{
"text": "Dev Environment",
"baseColor": "#059669",
"description": "Deploy to development for testing"
},
{
"text": "QA Environment",
"baseColor": "#047857",
"description": "Quality assurance testing"
},
{
"text": "Staging",
"baseColor": "#065f46",
"description": "Production-like environment for final checks"
},
{
"text": "Production",
"baseColor": "#064e3b",
"description": "Live production deployment"
},
{
"text": "Monitoring",
"baseColor": "#f59e0b",
"description": "Health checks and performance monitoring"
}
]
}
[/docops]

### Microservices Architecture Flow

[docops:connector]
{
"connectors": [
{
"text": "API Gateway",
"baseColor": "#8b5cf6",
"description": "Entry point for all requests"
},
{
"text": "Authentication",
"baseColor": "#7c3aed",
"description": "Verify user credentials"
},
{
"text": "Authorization",
"baseColor": "#6d28d9",
"description": "Check permissions"
},
{
"text": "Service Router",
"baseColor": "#5b21b6",
"description": "Route to appropriate microservice"
},
{
"text": "Business Logic",
"baseColor": "#4c1d95",
"description": "Process request"
},
{
"text": "Data Layer",
"baseColor": "#3b0764",
"description": "Persist or retrieve data"
},
{
"text": "Response",
"baseColor": "#2e1065",
"description": "Return formatted response"
}
]
}
[/docops]

---

## Integration Tips

### In Documentation
Embed connector visualizations inline with your documentation to provide immediate visual context for complex processes.

### In Presentations
Export as SVG for crisp, scalable graphics in slides and presentations.

### In Wikis & Knowledge Bases
Use connectors to create living process documentation that's easy to update as workflows evolve.

### With Other DocOps Components
- Combine with **Buttons** for interactive process navigation
- Use with **ADRs** to show the implementation path for architectural decisions
- Pair with **Timelines** to show process evolution over time

---

<div align="center" style="margin-top: 48px; padding: 24px; background: #fafbfc; border-radius: 8px;">
  <p style="color: #64748b; margin: 0;">Ready to visualize your workflows?</p>
  <p style="color: #6366f1; font-weight: 600; margin: 8px 0 0 0;">Create clear, beautiful process flows with DocOps Connectors</p>
</div>