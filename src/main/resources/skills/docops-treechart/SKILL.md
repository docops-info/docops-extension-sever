---
name: docops-treechart
description: Create and edit DocOps treechart blocks in Asciidoc (.adoc) or Markdown (.md). Use for hierarchical diagrams like org charts, taxonomies, decision trees, or project breakdowns.
---

# DocOps Tree Chart

## Overview

Create hierarchical diagrams using indentation-based syntax. Supports vertical and horizontal layouts, collapsible nodes, and color coding.

## Workflow

1. Define the hierarchy using indentation (spaces or tabs).
2. Confirm output format: Asciidoc or Markdown.
3. Apply options: `title`, `width`, `height`, `orientation`, `useGlass`, `useDark`, `colors`.
4. Emit the block with metadata followed by the indented tree data.

## References

- `references/reference.md`

## Output Rules

- Asciidoc: `[docops,treechart]` + `----` wrappers and `---` separator.
- Markdown: `[docops:treechart]` and `[/docops]` tags and `---` separator.
- Root node must have zero indentation.
- Optional `| #hexcolor` suffix for custom node colors.
