# Pie Charts (docops,pieslice) and Multi-Pie (docops,pie)

Use `pieslice` for classic pie/donut charts. Use `pie` for compact KPI pie badges in a row.

## Asciidoc Format

### Pie/Donut

```asciidoc
[docops,pieslice]
----
title=Chart Title
legend=true
donut=true
percentages=true
---
Category 1 | 30
Category 2 | 25
Category 3 | 20
Category 4 | 15
Category 5 | 10
----
```

### Multi-Pie

```asciidoc
[docops,pie]
----
baseColor=#e5e7eb
outlineColor=#10b981
scale=1.0
---
Toys | 14
Furniture | 43
Home Decoration | 15
Electronics | 28
----
```

## Markdown Format

### Pie/Donut

```md
[docops:pieslice]
legend=true
percentages=true
donut=true
---
Product A | 30
Product B | 25
Product C | 20
Product D | 15
Product E | 10

[/docops]
```

### Multi-Pie

```md
[docops:pie]
outlineColor=#FA4032
scale=1
---
Toys | 14
Furniture | 43
Home Decoration | 15
Electronics | 28

[/docops]
```

## Options (pieslice)

- `donut=true` (modern donut style)
- `legend=true` (interactive legend)
- `percentages=true` (show % labels)
- `hover=true`
- `useDark=true`
- `width=600` / `height=600`

## Custom Colors

Add a third column for hex colors:

```asciidoc
Category 1 | 30 | #3498db
Category 2 | 25 | #e74c3c
```

## Example: Donut With Legend

```asciidoc
[docops,pieslice]
----
title=Market Share by Region
visualVersion=1
legend=true
donut=true
percentages=true
theme=reo
---
North America | 35
Europe | 28
Asia Pacific | 22
Latin America | 10
Middle East & Africa | 5
----
```
