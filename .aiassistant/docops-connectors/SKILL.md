---
name: docops-connectors
description: Create and edit DocOps connector blocks in Asciidoc (.adoc) or Markdown (.md). Use for visualizing sequential processes, workflows, or step-by-step procedures with labeled boxes and arrows.
---

# DocOps Connectors

## Overview

Visualize sequential flows with auto-lettered boxes (A, B, C...) and arrow connections. Includes legend support for detailed step descriptions.

## Workflow

1. Define the process steps and their order.
2. Confirm output format: Asciidoc or Markdown.
3. Apply options: `useDark`.
4. Emit the block with a data table (Step | Color | Description) or JSON.

## References

- `references/reference.md`

## Output Rules

- Asciidoc: `[docops,connector]` + `----` wrappers.
- Markdown: `[docops:connector]` and `[/docops]` tags.
- Columns: `Step | Color | Description`.
