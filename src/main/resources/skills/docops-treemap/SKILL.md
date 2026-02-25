---
name: docops-treemap
description: Create and edit DocOps treemap blocks in Asciidoc (.adoc) or Markdown (.md). Use for visualizing hierarchical or proportional data like budgets, market share, and resource allocation.
---

# DocOps Treemap

## Overview

Create DocOps treemap visualizations in Asciidoc or Markdown. Treemaps use nested rectangles to represent data proportions.

## Workflow

1. Identify the hierarchical data and the proportions to visualize.
2. Confirm output format: Asciidoc or Markdown.
3. Collect labels, numeric values, and optional descriptions or metrics.
4. Apply treemap options (theme, palette, dimensions, dark mode, etc.).
5. Emit a valid DocOps block with pipe-separated data rows.

## References

- `references/reference.md`: Comprehensive guide for syntax and options.

## Output Rules

- Asciidoc blocks: `[docops,treemap]` + `----` wrappers and `---` header/data separator.
- Markdown blocks: `[docops:treemap]` start tag and `[/docops]` end tag with `---` separator.
- Put options above the `---` separator, then data rows below it.
- Data rows use pipe-separated format: `Label | Value | Description | Color | Metric`.
- Use inline data rows for new treemaps.
