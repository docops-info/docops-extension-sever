---
name: docops-metricscard
description: Create and edit DocOps metricscard blocks in Asciidoc (.adoc) or Markdown (.md). Use for highlighting key KPIs (Key Performance Indicators) with clean summary visuals. Ideal for executive summaries, sprint reviews, and reporting.
---

# DocOps MetricsCard

## Overview

Create DocOps MetricsCard blocks to transform KPI tables into visual summary snapshots. Supports table-based and JSON input.

## Workflow

1. Identify the KPIs (Metric, Value, Sublabel).
2. Confirm output format: Asciidoc or Markdown.
3. Select an optional theme (e.g., `aurora`, `tokyo`) or display options.
4. Emit a valid DocOps block with the metrics data.

## References

Follow the format and options in:
- `.aiassistant/docops-metricscard/references/reference.md`

## Output Rules

- Asciidoc blocks: `[docops,metricscard]` + `----` wrappers.
- Markdown blocks: `[docops:metricscard]` start tag and `[/docops]` end tag.
- Standard Table syntax: `Metric | Value | Sublabel` headers followed by rows.
- JSON support for programmatic generation.
- Common Options: `title`, `useDark`, `scale`, `controls`, `theme`.
