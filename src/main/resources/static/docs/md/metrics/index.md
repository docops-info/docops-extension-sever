# DocOps Metrics Cards

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/MetricsIcon.svg" alt="Metrics Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #1e40af; font-size: 32px;">DocOps Metrics Cards</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Highlight key KPIs with clean, shareable metric snapshots</p>
    </div>
  </div>
</div>

[TOC]

## What are DocOps Metrics Cards?

Metrics cards turn small KPI tables into clean, consistent summary visuals you can drop into docs, status pages, and release notes. They are ideal for executive summaries, sprint reviews, and quarterly reporting where you want fast scanning over dense detail.

### Key Features

- **Compact KPI summaries** - Highlight the numbers that matter
- **Consistent formatting** - Keep teams aligned on how metrics are presented
- **Lightweight input** - Simple table format with labels, values, and sublabels
- **Great for snapshots** - Works well for quarterly or milestone reporting

---

## Default Look

[docops:metricscard]
title= Q2 2024 Business Metrics
---
Metric | Value | Sublabel
Revenue | $4.2M | 18% YoY Growth
New Customers | 156 | 42 Enterprise
Customer Retention | 94% | 2% Improvement
NPS Score | 72 | Industry Leading
[/docops]

---

## Sustainability Metrics Example

[docops:metricscard]
title= Sustainability Achievements
---
Metric | Value | Sublabel
Carbon Reduction | 28% | vs. 2020 Baseline
Renewable Energy | 72% | Of Total Consumption
Water Conservation | 350K | Gallons Saved
Waste Diverted | 94% | From Landfill
Paper Reduction | 65% | Digital Transformation
[/docops]

---

## Modern Snapshot Example

[docops:metricscard]
title= Q3 2024 Product Health
---
Metric | Value | Sublabel
Active Users | 248K | +12% MoM
Time to First Value | 6 mins | -1.5 mins
Crash-Free Sessions | 99.4% | +0.3%
Support Tickets | 182 | -18%
[/docops]

---

## Common Use Cases

- **Executive summaries** - Keep leadership focused on outcomes
- **Sprint reviews** - Show deltas and delivery impact
- **Release notes** - Highlight adoption and stability improvements
- **Status pages** - Publish operational KPIs
- **Sustainability reports** - Track targets against baselines

---

## Format Options

### Metrics Card Structure

Use a title and a simple table with `Metric`, `Value`, and `Sublabel` columns:

```text
[docops:metricscard]
title= Q2 2024 Business Metrics
---
Metric | Value | Sublabel
Revenue | $4.2M | 18% YoY Growth
New Customers | 156 | 42 Enterprise
Customer Retention | 94% | 2% Improvement
NPS Score | 72 | Industry Leading
[/docops]
```

### Columns

- **Metric** - The KPI name or label
- **Value** - The primary number to highlight
- **Sublabel** - Supporting context or delta

---

## Best Practices

- **Use consistent units** - Keep values comparable (%, $, counts)
- **Limit to 4-6 metrics** - Prevent visual overload
- **Show deltas** - Add trend or comparison in the sublabel
- **Refresh regularly** - Out-of-date metrics erode trust

<div style="background: #eff6ff; border-left: 4px solid #3b82f6; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #1e3a8a; font-weight: 600;">📌 Quick Tip</p>
  <p style="margin: 8px 0 0 0; color: #475569;">Treat metrics cards as snapshots. If a KPI needs explanation, link to a deeper report instead of overloading the card.</p>
</div>
