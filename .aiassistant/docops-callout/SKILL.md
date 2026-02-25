---
name: docops-callout
description: Create and edit DocOps callout blocks in Asciidoc (.adoc) or Markdown (.md). Use for highlighting processes (systematic), key metrics (metrics), or timeline events (timeline) with modern visual cards.
---

# DocOps Callout

## Overview

Create DocOps callout blocks to spotlight key information. Supports three types: systematic process, key metrics, and timeline events.

## Workflow

1. Identify the goal: process steps, metrics display, or chronological events.
2. Select the `type`: `systematic`, `metrics`, or `timeline`.
3. Confirm output format: Asciidoc or Markdown.
4. Apply options: `title`, `width`, `height`, `useDark`.
5. Emit the block with a data table using `|` as separator.

## Callout Types

- **Systematic**: For step-by-step approaches (Phase | Action | Result | Improvement).
- **Metrics**: For key performance indicators (Metric | Value).
- **Timeline**: For sequential events (Phase | Action | Result).

## References

- `references/reference.md`

## Output Rules

- Asciidoc: `[docops,callout]` + `----` wrappers and `---` separator.
- Markdown: `[docops:callout]` and `[/docops]` tags and `---` separator.
- Put options (`title`, `type`, etc.) above `---`, data table below.
