---
name: docops-adr
description: Create and edit DocOps Architecture Decision Record (ADR) blocks in Asciidoc (.adoc) or Markdown (.md). Use when documenting architectural choices, updating decision status, or listing participants and references in a structured ADR format.
---

# DocOps ADR

## Overview

Create DocOps ADR blocks to document architectural decisions with context, decisions, consequences, participants, and status tracking.

## Workflow

1. Identify the architectural decision to document.
2. Confirm output format: Asciidoc or Markdown.
3. Collect ADR details:
    - Title
    - Status (Proposed, Accepted, Superseded, Deprecated, Rejected)
    - Date (YYYY-MM-DD)
    - Context (bullet points)
    - Decision (bullet points)
    - Consequences (bullet points)
    - Participants (simple or structured format)
    - References (wiki-style links)
4. Apply options like `useDark=true` or `template:brutalist` (Markdown).
5. Emit a valid DocOps ADR block.

## Output Rules

### Asciidoc Format
- Block header: `[docops,adr]` (can include attributes like `role=center`, `useDark=true`).
- Content wrapper: `----` (or `....` if containing source examples).
- Content: YAML-like key-value pairs.
- Multi-line lists: Use `- ` prefix.

### Markdown Format
- Start tag: `[docops:adr]`.
- End tag: `[/docops]`.
- Content: YAML-like key-value pairs.
- Multi-line lists: Use `- ` prefix.

## References

- `references/reference.md`: Detailed syntax, status types, participant formats, and examples.
