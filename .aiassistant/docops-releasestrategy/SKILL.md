---
name: docops-releasestrategy
description: Create and edit DocOps release strategy blocks in Asciidoc (.adoc) or Markdown (.md). Use for visualizing product release plans with timeline roadmaps (TLS) or detailed roadmap (R) layouts.
---

# DocOps Release Strategy

## Overview

Create DocOps release strategy blocks in Asciidoc or Markdown. These visualizations help communicate release plans, milestones, and activities using a modern glass design.

## Workflow

1. Identify the release plan details (title, releases, milestones).
2. Choose the layout style: `TLS` (Timeline Summary) or `R` (Roadmap).
3. Confirm output format: Asciidoc or Markdown.
4. Collect release entries: type (M1, RC1, GA, etc.), date, goal, and activity lines.
5. Apply display options (scale, useDark, colors).
6. Emit a valid DocOps block.

## Style Selection

- `TLS` (Timeline Summary): Condensed timeline view, best for high-level overviews.
- `R` (Roadmap): Detailed roadmap layout, best for quarter-by-quarter plans or multi-year roadmaps.

## References

- `references/reference.md`: Detailed format examples for Asciidoc and Markdown.

## Output Rules

- Asciidoc blocks: `[docops,release]` + `----` wrappers. The content is a JSON object.
- Markdown blocks: `[docops:release]` start tag and `[/docops]` end tag. The content is a JSON object.
- Use semantic release types: `M1`, `M2` (Milestones), `RC1` (Release Candidate), `GA` (General Availability).
- Dates should be ISO (YYYY-MM-DD) or Quarters (YYYY-Q1).
- Keep activity lists focused (3-5 items).

## Quick Questions To Ask

- What is the title of the release strategy?
- Which style do you prefer: TLS (Timeline) or R (Roadmap)?
- Should the output be Asciidoc or Markdown?
- What are the releases (type, date, goal, activities)?
- Do you want to enable dark mode or adjust the scale?
