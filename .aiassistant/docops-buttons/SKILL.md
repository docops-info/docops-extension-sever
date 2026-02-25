---
name: docops-buttons
description: Create and edit DocOps button blocks in Asciidoc (.adoc) or Markdown (.md). Use when generating interactive navigation grids, visual menus, dashboards, or resource directories with various button shapes and styles.
---

# DocOps Buttons

## Overview

Create DocOps button blocks in Asciidoc or Markdown with various shapes (REGULAR, PILL, RECTANGLE, ROUND, CIRCLE, LARGE, SLIM, OVAL, HEX), colors, and layouts.

## Workflow

1. Identify the navigation goal and choose the button type/shape.
2. Confirm output format: Asciidoc or Markdown.
3. Collect button details: labels, links, descriptions, types, dates, authors, and optional images.
4. Define the theme: columns, scale, colors, and sorting.
5. Emit a valid DocOps block with JSON configuration.

## Button Type Selection

Pick the most appropriate shape:

- **REGULAR**: Standard navigation grids.
- **PILL**: Tag-style or compact groupings.
- **LARGE**: Hero tiles with metadata and icons.
- **RECTANGLE**: Structured menus with sub-links.
- **SLIM**: Compact sidebar-like navigation.
- **HEX**: Technical docs or interconnected dashboards.
- **CIRCLE**: Icon-focused launchers.
- **ROUND**: Modern widgets or feature showcases.
- **OVAL**: Playful or timeline-style layouts.

## References

Follow the format in:
- `references/reference.md`

## Output Rules

- **Asciidoc blocks**:
  ```asciidoc
  [docops,buttons]
  ----
  {
    "buttons": [...],
    "buttonType": "...",
    "theme": {...}
  }
  ----
  ```
- **Markdown blocks**:
  ```md
  [docops:buttons]
  {
    "buttons": [...],
    "buttonType": "...",
    "theme": {...}
  }
  [/docops]
  ```
- Use valid JSON for the configuration.
- Supports `include::` for data files in Asciidoc.

## Quick Questions To Ask

- What buttons do you want to include (label and link)?
- Which button shape/type should be used?
- Should the output be Asciidoc or Markdown?
- How many columns should the grid have?
- Any specific colors, icons, or sorting requirements?
