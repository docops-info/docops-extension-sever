---
name: docops-charting
description: Create and edit DocOps chart blocks in Asciidoc (.adoc) or Markdown (.md). Use when generating chart markup, selecting a DocOps chart type, mapping data into chart rows, or tuning chart options for bar, line, combination, gauge, pie/donut, multi-pie, or quadrant charts.
---

# DocOps Charting

## Overview

Create DocOps chart blocks in Asciidoc or Markdown with the correct chart type, options, and data rows.

## Workflow

1. Identify the chart goal and choose the chart type.
2. Confirm output format: Asciidoc or Markdown.
3. Collect labels, series, and data values.
4. Apply chart options (theme, dark mode, axes, legends, etc.).
5. Emit a valid DocOps block with inline data rows unless the user provides a file to `include::` (Asciidoc only).

## Chart Type Selection

Pick the most appropriate type before drafting markup:

- Bar comparisons and rankings: `bar` or `bargroup`.
- Trends over time: `line`.
- Mixed absolute + rate metrics: `combination`.
- Single KPI with thresholds: `gauge`.
- Proportions of a whole: `pieslice` (pie or donut).
- Compact KPI badges: `pie` (multi-pie row).
- Impact vs. effort maps: `quadrant`.

## References

Load the reference for the selected chart type and follow its format exactly:

- `references/barchart.md`
- `references/linechart.md`
- `references/combination.md`
- `references/gaugechart.md`
- `references/piechart.md`
- `references/quadrant.md`

## Output Rules

- Asciidoc blocks: `[docops,<type>]` + `----` wrappers and `---` data separator.
- Markdown blocks: `[docops:<type>]` start tag and `[/docops]` end tag.
- Put options above the `---` separator, then rows below it.
- Use inline data rows unless the user explicitly wants `include::` data files.
- Preserve user-provided values, units, and labels; do not invent data.

## Quick Questions To Ask

- Which chart type do you want?
- Should the output be Asciidoc or Markdown?
- What are the labels and numeric values (or series + categories)?
- Any options like `useDark`, `theme`, `sorted`, `dualYAxis`, or `donut`?
- Should I inline the data, or use `include::` (Asciidoc only)?
