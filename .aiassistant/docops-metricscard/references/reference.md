# DocOps MetricsCard Syntax Reference

Transforms KPI tables into clean summary visuals for snapshots and reporting.

## Asciidoc Format

### Basic Table Format

```asciidoc
[docops,metricscard]
----
title= Q2 2024 Business Metrics
---
Metric | Value | Sublabel
Revenue | $4.2M | 18% YoY Growth
New Customers | 156 | 42 Enterprise
Customer Retention | 94% | 2% Improvement
NPS Score | 72 | Industry Leading
----
```

### JSON Format

```asciidoc
[docops,metricscard, useDark=true]
----
{
  "title": "Performance Improvements",
  "metrics": [
    {
      "value": "97%",
      "label": "Query Cost Reduction",
      "sublabel": "(12,000 → 405)"
    }
  ]
}
----
```

## Markdown Format

```md
[docops:metricscard]
title= Q2 2024 Business Metrics
theme=aurora
---
Metric | Value | Sublabel
Revenue | $4.2M | 18% YoY Growth
New Customers | 156 | 42 Enterprise
Customer Retention | 94% | 2% Improvement
NPS Score | 72 | Industry Leading
[/docops]
```

## Columns

- **Metric**: The KPI name or label.
- **Value**: The primary number to highlight.
- **Sublabel**: Supporting context or delta (e.g., trend, comparison).

## Options

- `title`: Main heading for the card.
- `theme`: Visual style (e.g., `aurora`, `tokyo`).
- `useDark=true`: Enable dark theme.
- `scale=1.5`: Increase size multiplier.
- `controls=true`: Add export/interactive controls.

## Best Practices

1. **Use consistent units**: Keep values comparable (%, $, counts).
2. **Limit to 4-6 metrics**: Prevent visual overload and maintain scanability.
3. **Show deltas**: Add trend or comparison in the sublabel for context.
4. **Treat as snapshots**: Use cards for summaries; link to deeper reports if detail is needed.
