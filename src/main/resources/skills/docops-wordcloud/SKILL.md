---
name: docops-wordcloud
description: Create and edit DocOps wordcloud blocks in Asciidoc (.adoc) or Markdown (.md). Use for visualizing text data frequency or significance as a visual hierarchy of words.
---

# DocOps WordCloud

## Overview

Transform text data into visual hierarchies where font size indicates importance (weight).

## Workflow

1. Collect words and their relative weights.
2. Confirm output format: Asciidoc or Markdown.
3. Apply options: `title`, `minFontSize`, `maxFontSize`, `width`, `height`, `useDark`, `scale`.
4. Emit the block with a data table (Word | Weight | Color).

## References

- `references/reference.md`

## Output Rules

- Asciidoc: `[docops,wordcloud]` + `----` wrappers and `---` separator.
- Markdown: `[docops:wordcloud]` and `[/docops]` tags and `---` separator.
- Columns: `Word | Weight | Color`.
