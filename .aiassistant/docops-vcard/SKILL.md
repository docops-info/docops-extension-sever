---
name: docops-vcard
description: Create and edit DocOps vcard blocks in Asciidoc (.adoc) or Markdown (.md). Use for professional business cards, team directories, and speaker bios with QR codes.
---

# DocOps VCard

## Overview

Create DocOps vcard blocks in Asciidoc or Markdown. It transforms standard vCard 3.0 format into professional SVG business cards with embedded QR codes.

## Workflow

1. Identify the contact information (Name, Org, Title, Email, Tel, etc.).
2. Confirm output format: Asciidoc or Markdown.
3. Choose a design template (`modern_card`, `tech_pattern_background`, `neo_brutalist`).
4. Apply vcard options (theme, scale, dark mode).
5. Emit a valid DocOps block with vCard 3.0 content.

## References

- `references/reference.md`: Comprehensive guide for syntax and vCard fields.

## Output Rules

- Asciidoc blocks: `[docops,vcard]` + `----` wrappers and optional `---` separator for options.
- Markdown blocks: `[docops:vcard]` start tag and `[/docops]` end tag.
- Options can be placed above the `---` separator (or as block attributes in Asciidoc).
- The body must contain standard vCard 3.0 format (BEGIN:VCARD ... END:VCARD).
