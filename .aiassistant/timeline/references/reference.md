# Timeline (DocOps) — Reference (Markdown)

Timelines turn a chronological list of events into a visual narrative in your documentation.

---

## 1) Basic syntax (Markdown)

```markdown 
[docops,timeline,title="Timeline Title",scale="1"]
date: Q1 2024 
text: Kickoff Description line 1. Description line 2 with a [[https://example.com link]].

date: Q2 2024 
text: Design Phase More details here. 
[/docops]
```

### Notes

- `scale` is supported.
- **Dark/light mode is controlled by your stylesheet name** (containing `dark` or `light`).  
  There is no `useDark` option in the Markdown timeline block.

---

## 2) Options

### Header options (Markdown)

| Option | Type | Default | Purpose |
|---|---:|---:|---|
| `title="..."` | string | (none) | Title shown for the timeline |
| `scale="1"` | number-like string | `"1"` | Scales the rendered timeline |

### Payload options

| Option | Values | Default | Purpose |
|---|---|---|---|
| `type=H` / `type=V` | `H`, `V` | `H` | Layout direction (Horizontal/Vertical) |

> If `type` is omitted, the layout is **horizontal (`H`)**.

---

## 3) Event format (grammar)

An event is defined by:

- `date: <label>`
- `text: <headline>`
- Optional description lines after `text:` until the next `date:` (or the end)

### Blank lines

Blank lines between events are **optional**. This is valid:

```markdown 
[docops,timeline,title="No Blank Lines Needed",scale="1"]
date: 2024-01 
text: Started First description line 
date: 2024-02 
text: Continued Second description line 
[/docops]
```

---

## 4) Links

Links are allowed in **description lines** using:

- `[[https://example.com Label]]`

Example:

```markdown
date: Q3 2024 
text: Beta See [[https://docs.example.com/beta-notes Beta notes]] for details.
```

---

## 5) Best practices

### Recommended size

- Sweet spot: **6–10 events**
- If you have more, split into multiple timelines (e.g., “2024 H1” and “2024 H2” or “Build” vs “Launch”).

### Add a timeline summary (recommended)

Add 1–2 lines *above* the timeline to help readers scan:

- **Scope:** time range and what’s included
- **Narrative:** what changes over time
- **Anchor:** what “done” means (beta/GA/migration complete)

Copy-ready patterns:


```text 
From Q1–Q4 2024, this timeline tracks kickoff through launch, highlighting the design handoff (Q2) and stabilization window (Q4).
```

---

## 6) Troubleshooting

- Nothing renders: verify you used the opening `[docops,timeline ...]` and closing `[/docops]`.
- Missing events: ensure each event has both `date:` and `text:`.
- Link not clickable: ensure the link uses `[[url label]]` and is placed in a description line (not in `text:`).