---
name: docops-scorecard
description: Create and edit DocOps scorecard blocks in Asciidoc (.adoc) or Markdown (.md). Use for comparing before-and-after states in a single visual. Ideal for release summaries, migrations, incident outcomes, and program improvements.
---

# DocOps Scorecard

## Overview

Create DocOps Scorecard blocks to visualize transformations with a before/after comparison layout. Organizes changes by categories using grouped sections.

## Workflow

1. Identify the 'before' and 'after' states.
2. Group items into categories (e.g., Feature Status, Known Issues, Operational Debt).
3. Confirm output format: Asciidoc or Markdown.
4. Define the `title` and optional `subtitle`.
5. Create `[before]` and `[after]` blocks with `title` and `items`.
6. Emit a valid DocOps block.

## References

Follow the format and options in:
- `.aiassistant/docops-scorecard/references/reference.md`

## Output Rules

- Asciidoc blocks: `[docops,scorecard]` + `----` wrappers and `---` section separators.
- Markdown blocks: `[docops:scorecard]` start tag and `[/docops]` end tag.
- Block labels: `[before]`, `[before.items]`, `[after]`, `[after.items]`.
- Section headers: Use `=== Section Name` within items.
- Items: `Label | Description` syntax.
- Options: `title`, `subtitle`, `useDark`, `scale`, `controls`.
