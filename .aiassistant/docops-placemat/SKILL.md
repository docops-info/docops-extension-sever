---
name: docops-placemat
description: Create and edit DocOps placemat blocks in Asciidoc (.adoc) or Markdown (.md). Use for organizing concepts, components, or technology stacks into color-coded label grids.
---

# DocOps Placemat

## Overview

Organize items into a grid of color-coded labels. Each item belongs to a category defined in a legend.

## Workflow

1. List the items and their categories.
2. Define colors for each category in the legend.
3. Confirm output format: Asciidoc or Markdown.
4. Apply options: `title`, `fill`, `useDark`, `scale`.
5. Emit the block using JSON format for the configuration and items.

## References

- `references/reference.md`

## Output Rules

- Asciidoc: `[docops,placemat]` + `----` wrappers.
- Markdown: `[docops:placemat]` and `[/docops]` tags.
- Format: JSON structure containing `title`, `placeMats` array, and `config.legend` array.
