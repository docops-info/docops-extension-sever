# DocOps Scorecard Syntax Reference

Compares before-and-after states in a single visual, ideal for transformations and release summaries.

## Asciidoc Format

```asciidoc
[docops,scorecard]
----
title=Software Release v2.4.0
subtitle=Migration Summary
---
[before]
title=BEFORE v2.4.0
---
[before.items]
=== Feature Status
Dark Mode Theme | Missing
=== Known Issues
Login timeout | Frequent logouts
---
[after]
title=AFTER v2.4.0
---
[after.items]
=== New Features Added
Dark Mode Theme | Implemented
=== Bugs Resolved
Login timeout | Rewritten session management
----
```

## Markdown Format

```md
[docops:scorecard]
title=Software Release v2.4.0
subtitle=Highlights
---
[before]
title=BEFORE
---
[before.items]
=== Stability Risks
Service timeouts | Frequent during peak load
---
[after]
title=AFTER
---
[after.items]
=== Improvements Delivered
Service timeouts | Reduced by 68%
[/docops]
```

## Structure

- **title/subtitle**: Main headings.
- **[before] / [after]**: Defines the state block.
- **title** (within before/after): Column heading.
- **[before.items] / [after.items]**: Contains list of changes.
- **=== Section Name**: Grouping header for items.
- **Item | Description**: Pipe-separated item and status.

## Options

- `useDark=true`: Enable dark theme.
- `scale=1.5`: Increase size.
- `controls=true`: Show interactivity controls.

## Best Practices

1. **Mirror categories**: Use identical section names in before and after columns.
2. **Keep it balanced**: Align items to make improvements obvious.
3. **Use measurable outcomes**: Quantify gains (e.g., "-67%", "Reduced by 68%").
4. **Be concise**: Keep descriptions short for scannability.
5. **Limit grid size**: 8-12 items per column max.
