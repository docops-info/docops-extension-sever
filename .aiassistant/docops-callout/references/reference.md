# DocOps Callout Reference

## Asciidoc Format

### Systematic Process
```asciidoc
[docops,callout]
----
title: Software Development Process
type=systematic
---
Phase | Action | Result | Improvement
Requirements | Gather user needs | Requirements doc | Involve users earlier
Design | Create architecture | Tech specs | Design workshops
Development | Implement features | Working code | Pair programming
----
```

### Key Metrics
```asciidoc
[docops,callout]
----
title: Performance Metrics
type=metrics
---
Metric | Value
Query Performance | 97% reduction
Database CPU | Reduced from 88% to 60%
Methodology | Data-driven approach
----
```

### Timeline Events
```asciidoc
[docops,callout]
----
title: Project Timeline
type=timeline
---
Phase | Action | Result
Q1 2024 | Kickoff | Team formed
Q2 2024 | Design | Architecture complete
Q3 2024 | Build | Core features shipped
----
```

## Markdown Format

```md
[docops:callout]
title: Process Title
type=systematic
---
Phase | Action | Result | Improvement
Step 1 | Action 1 | Result 1 | Note 1
[/docops]
```

## Options

- `type`: `systematic`, `metrics`, `timeline` (required)
- `title`: Callout heading
- `width`: Width in pixels (default: 800)
- `height`: Base height (default: 600 for systematic, 400 for metrics, 500 for timeline)
- `useDark`: `true` for dark theme
