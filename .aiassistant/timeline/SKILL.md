---
name: timeline
description: "Timeline is a skill that helps you author DocOps timelines for Asciidoctor or Markdown. It generates a clean, chronological event list that renders as a timeline visualization."
trigger:
  - "create a timeline"
  - "make a timeline"
  - "add event to timeline"
  - "remove event from timeline"
  - "update event in timeline"
  - "convert timeline to asciidoctor"
  - "convert timeline to markdown"
---

## What this skill does

Given a set of events (date + headline + optional description), this skill produces a DocOps timeline block you can paste into:

- Asciidoctor documentation
- Markdown documentation

## Output rules (important)

- Default layout is `type=H` (horizontal). Only output `type=V` when the user asks for vertical.
- Blank lines between events are optional. Events are detected by `date:` boundaries.
- Links are allowed in *description lines only* (not in the `text:` headline line), using `[[url label]]`.
- `scale` is supported in both Asciidoctor and Markdown.
- Dark/light in Markdown is *not* controlled by `useDark`. It is driven by the stylesheet name containing `dark` or `light`.
    - Therefore: **never output `useDark` in Markdown**.

## Quick start

### Asciidoctor (supports `useDark`)

```asciidoc
[docops,timeline,title="Project Milestones",scale="1",useDark=false,role="center"]
----
date: Q1 2024 
text: Kickoff Initial planning and team formation completed.
date: Q2 2024 
text: Design Phase UI/UX design and architecture planning.
----
```

### Markdown (`scale` supported; theme via stylesheet name)

```markdown
[docops,timeline,title="Project Milestones",scale="1"]
date: Q1 2024 
text: Kickoff Initial planning and team formation completed.
date: Q2 2024 
text: Design Phase UI/UX design and architecture planning. 
[/docops]
```
## How to respond to users (behavior)

When asked to create a timeline, do this:

1. Ask at most 2 clarifying questions **only if needed**:
    - Target format: Asciidoctor or Markdown?
    - Any preference for date style (quarters vs months vs exact dates)?

2. Produce a single paste-ready timeline block.
3. Include a 1–2 line *timeline summary* above the block (unless the user asked for “just the block”):
    - Scope (time range)
    - Narrative (what changes over time)
    - Anchor (what “done” looks like)

## URL / API embedding (optional)

If the user needs an image URL, use:

`https://<host>/extension/api/docops/svg?kind=timeline&payload=<base64_encoded_payload>`
