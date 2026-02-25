# DocOps Tree Chart Reference

## Asciidoc Format

```asciidoc
[docops,treechart]
----
title=Organization Chart
width=800
height=600
orientation=vertical
---
CEO
    CTO
        Engineering Manager
            Developer
        QA Manager
    CFO
        Finance Manager
----
```

### Custom Colors
```asciidoc
[docops,treechart]
----
---
Application | #3498db
    Frontend | #2ecc71
        React
    Backend | #e74c3c
        Spring Boot
----
```

## Markdown Format

```md
[docops:treechart]
title=Decision Tree
orientation=horizontal
---
Start
    Option A
        Result A1
    Option B
        Result B1
[/docops]
```

## Options

- `title`: Chart heading.
- `width` / `height`: Canvas dimensions (default: 800x600).
- `orientation`: `vertical` (top-down) or `horizontal` (left-right).
- `collapsible`: `false` to disable interactivity (useful for PDF).
- `expanded`: `true` to show all nodes initially.
- `useGlass`: `true` for modern glass effect on nodes.
- `useDark`: `true` for dark theme.
- `colors`: Comma-separated hex colors for palette.

## Data Syntax
- Indentation defines hierarchy.
- Use spaces or tabs consistently.
- Root node must have zero indentation.
- Append `| #hex` for per-node colors.
