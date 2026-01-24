# Information Callouts

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/CalloutIcon.svg" alt="Callout Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #7c3aed; font-size: 32px;">DocOps Callouts</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Highlight important information with structured, visually distinct callout boxes</p>
    </div>
  </div>
</div>

[TOC]

## What are Callouts?

DocOps Callouts are specialized information containers that draw attention to important content within your documentation. They transform raw data into visually organized, scannable information blocks that help readers quickly identify and understand key points, processes, and metrics.

### Key Features

- **Multiple Styles** - Systematic process flows and metrics displays
- **Visual Hierarchy** - Colored accent bars and structured layouts
- **Data Organization** - Transform tables into visual information cards
- **Smart Formatting** - Automatic styling based on content type
- **Responsive Design** - Adapts to light and dark modes
- **Professional Aesthetics** - Modern, clean visual design

<div style="background: #f5f3ff; border-left: 4px solid #7c3aed; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #5b21b6; font-weight: 600;">💡 Use Cases</p>
  <p style="margin: 8px 0 0 0; color: #6d28d9;">Perfect for highlighting processes, metrics, timelines, best practices, warnings, and any content that needs special attention.</p>
</div>

---

## Callout Types

### Systematic Process Callouts

Visualize multi-step processes with clear phases, actions, results, and improvement notes. Ideal for documenting workflows, problem-solving approaches, and systematic methodologies.

[docops:callout]
title: Software Development Process
type=systematic
---
Phase | Action | Result | Improvement
Requirements | Gather user needs and system requirements | Detailed requirements document | Involve end-users earlier in the process
Design | Create system architecture and UI/UX designs | Technical specifications and wireframes | Use more design thinking workshops
Development | Implement features according to specifications | Working code with unit tests | Increase pair programming sessions
Testing | Perform QA and user acceptance testing | Bug reports and validation results | Automate more test cases
Deployment | Release to production environment | Live application | Implement more robust CI/CD pipeline
Maintenance | Monitor performance and fix issues | Stable system with ongoing improvements | Establish better feedback loops
[/docops]

**When to use Systematic Callouts:**
- Documenting development processes
- Explaining troubleshooting methodologies
- Showing before/after improvements
- Illustrating phase-based approaches
- Presenting action-result relationships

---

### Metrics Callouts

Display key performance indicators, business metrics, and quantitative data in an organized, scannable format. Perfect for dashboards, reports, and performance summaries.

[docops:callout]
title: Q2 2024 Business Performance
type=metrics
---
Metric | Value
Revenue | $2.4M
Growth | 18%
New Customers | 156
Customer Retention | 94%
NPS Score | 72
Average Deal Size | $15,400
Sales Cycle | 32 days
Marketing ROI | 3.2x

[/docops]

**When to use Metrics Callouts:**
- Presenting KPIs and performance data
- Summarizing quarterly/annual results
- Highlighting key statistics
- Creating executive summaries
- Building metrics dashboards

---

## Callout Anatomy

### Systematic Callout Structure

Each systematic callout consists of:

| Component | Description | Required |
|-----------|-------------|----------|
| **Title** | Descriptive header for the process | Yes |
| **Phase** | Step or phase name | Yes |
| **Action** | What was done in this phase | Yes |
| **Result** | Outcome or deliverable | Yes |
| **Improvement** | Lessons learned or optimization notes | Optional |

### Metrics Callout Structure

Each metrics callout consists of:

| Component | Description | Required |
|-----------|-------------|----------|
| **Title** | Descriptive header for the metrics | Yes |
| **Metric** | Name of the measured item | Yes |
| **Value** | The metric's value (with units) | Yes |

---

## Syntax and Configuration

### Basic Syntax

[docops:callout] 
title: Your Callout Title 
---
type=systematic|metrics
[Table Data] 
[/docops]

### Systematic Callout Example

[docops:callout] 
title: Database Optimization Journey 
type=systematic
---
Phase | Action | Result | Improvement Analysis | 
Identified slow queries | Found 3 bottlenecks | Use query profiler Indexing | 
Added composite indexes | 60% faster queries | Monitor index usage Caching | 
Implemented Redis cache | 80% load reduction | Cache invalidation strategy 
[/docops]

### Metrics Callout Example

[docops:callout]
title: Q2 2024 Business Performance
type=metrics
---
Metric | Value
Revenue | $2.4M
Growth | 18%
New Customers | 156
Customer Retention | 94%
NPS Score | 72
Average Deal Size | $15,400
Sales Cycle | 32 days
Marketing ROI | 3.2x

[/docops]

## Best Practices

### Content Guidelines

1. **Keep titles concise** - Use clear, descriptive titles that summarize the callout's purpose
2. **Be specific with data** - Provide concrete metrics and results, not vague descriptions
3. **Use consistent formatting** - Maintain similar structure across callouts in the same document
4. **Include units** - Always specify units for metrics (%, ms, $, etc.)
5. **Prioritize scanability** - Present information in order of importance

### Visual Design

1. **Color coding** - Use different callout types to categorize information
2. **White space** - Don't overcrowd callouts with too many items
3. **Logical flow** - Arrange systematic steps in chronological order
4. **Highlight changes** - Use the Improvement column to show evolution

<div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #92400e; font-weight: 600;">⚠️ Important</p>
  <p style="margin: 8px 0 0 0; color: #b45309;">Callouts are meant to highlight key information—overuse can reduce their effectiveness. Use them strategically for content that truly deserves special attention.</p>
</div>

---

## Common Use Cases

### Development Processes

**Problem-Solving Documentation**

[docops:callout] title: API Performance Improvement type=systematic

Phase | Action | Result | Improvement Investigation | Analyzed slow endpoints | Identified N+1 queries | Use APM tools earlier Optimization | Implemented eager loading | 75% reduction in DB calls | Add query monitoring Caching | Added Redis for hot data | 90% faster response | Cache warming strategy [/docops]


### Business Reporting

**Quarterly Performance Summary**
```markdown
[docops:callout]
title: Q4 2024 Key Metrics
type=metrics
---
Metric | Value
Total Revenue | $8.9M
YoY Growth | 42%
Customer Churn | 3.2%
Team Size | 85
Product Launches | 3
[/docops]
```

[docops:callout]
title: Cloud Migration Progress
type=systematic
---
Phase | Action | Result | Improvement
Planning | Assessed cloud options | Selected AWS | Consider multi-cloud
Migration | Moved 80% of services | Zero downtime | Better rollback plan
Optimization | Right-sized instances | 30% cost reduction | Auto-scaling policies
[/docops]


## Project Metrics
### Sprint Velocity Trackin

[docops:callout]
title: Sprint 23 Metrics
type=metrics
---
Metric | Value
Story Points Completed | 48
Velocity | 16 points/week
Bugs Fixed | 12
Code Coverage | 87%
Deployment Frequency | 3x/week
[/docops]