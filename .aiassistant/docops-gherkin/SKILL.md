---
name: docops-gherkin
description: Create and edit DocOps Gherkin BDD visualization blocks in Asciidoc (.adoc) or Markdown (.md). Use when visualizing BDD scenarios with Given-When-Then formatting, showing test status (PASSING, FAILING, etc.), or documenting feature requirements.
---

# DocOps Gherkin

## Overview

Create DocOps Gherkin blocks in Asciidoc or Markdown to visualize BDD features and scenarios. Supports standard Gherkin text and structured JSON with status indicators.

## Workflow

1. Identify the Gherkin content (Feature, Scenarios, Steps).
2. Determine if execution status indicators are needed (requires JSON format).
3. Confirm output format: Asciidoc or Markdown.
4. Apply display options (theme, dark mode, scale, controls).
5. Emit a valid DocOps block with the Gherkin content or JSON.

## References

Follow the format and options in:
- `.aiassistant/docops-gherkin/references/reference.md`

## Output Rules

- Asciidoc blocks: `[docops,gherkin]` + `----` wrappers.
- Markdown blocks: `[docops:gherkin]` start tag and `[/docops]` end tag.
- Use plain Gherkin text for simple documentation.
- Use JSON format when status indicators or custom theming for steps are required.
- Standard keywords: `Given`, `When`, `Then`, `And`, `But`.
- Status types: `PASSING`, `FAILING`, `PENDING`, `SKIPPED`.
