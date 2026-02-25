---
name: docops-domain
description: Create and edit DocOps domain visualization blocks in Asciidoc (.adoc) or Markdown (.md). Use for mapping business or technical architectures, system landscapes, or capability maps using a compact CSV-like format.
---

# DocOps Domain Visualization

## Overview

Create DocOps domain visualization blocks in Asciidoc or Markdown. These hierarchical diagrams are ideal for showing system landscapes with shared foundations (COMMON) and specialized verticals.

## Workflow

1. Identify the domain structure (main node, shared layers, vertical groups).
2. Confirm output format: Asciidoc or Markdown.
3. Collect nodes and their hierarchy (COMMON vs. VERTICAL).
4. Apply domain options (useDark, useNeural, emojis, wiki links).
5. Emit a valid DocOps block.

## Key Concepts

- **Main Node**: The central entity (declared as `main,NAME`).
- **COMMON rows**: Shared capabilities/infrastructure appearing at the top.
- **Verticals**: Specialized groups with their own rows.
- **Wiki Links**: Clickable nodes using `[[url Label]]` syntax.

## References

- `references/reference.md`: Detailed format examples for Asciidoc and Markdown.

## Output Rules

- Asciidoc blocks: `[docops,domain]` + `----` wrappers.
- Markdown blocks: `[docops:domain]` start tag and `[/docops]` end tag.
- Always start with the main node: `main,DOMAIN_NAME`.
- Define the header: `type,emoji,rowIndex,nodes`.
- Use `COMMON` for shared layers.
- Use specific names for vertical groups.

## Quick Questions To Ask

- What is the main name of the domain?
- Should the output be Asciidoc or Markdown?
- What are the common infrastructure/shared nodes?
- What are the specialized verticals and their features?
- Do you want to include emojis or wiki-style links for any nodes?
- Should I enable dark mode?
