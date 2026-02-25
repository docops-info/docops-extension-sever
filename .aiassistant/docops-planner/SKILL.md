---
name: docops-planner
description: Create and edit DocOps planner blocks in Asciidoc (.adoc) or Markdown (.md). Use for organizing tasks into NOW, NEXT, LATER, and DONE columns for roadmaps and sprint planning.
---

# DocOps Planner

## Overview

A Kanban-style visualization for roadmaps and project planning with 4 fixed columns: NOW, NEXT, LATER, and DONE.

## Workflow

1. Identify tasks and their current stage.
2. Confirm output format: Asciidoc or Markdown.
3. Apply options: `title`, `useDark`.
4. Emit the block using the `- stage Task Title` syntax followed by the description.

## References

- `references/reference.md`

## Output Rules

- Asciidoc: `[docops,planner]` + `----` wrappers.
- Markdown: `[docops:planner]` and `[/docops]` tags.
- Syntax: `- stage Task Title` (stages: `now`, `next`, `later`, `done`).
- Description follows the title line and ends at the next task marker.
