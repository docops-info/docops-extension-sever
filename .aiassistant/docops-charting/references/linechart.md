# Line Charts (docops,line)

Use line charts for trends over time or multi-series comparisons.

## Asciidoc Format

### Basic

```asciidoc
[docops,line]
----
title=Monthly Revenue Trend
width=800
---
Revenue | Jan | 40
Revenue | Feb | 70
Revenue | Mar | 90
Revenue | Apr | 85
Revenue | May | 110
Revenue | Jun | 125
----
```

## Markdown Format

```md
[docops:line useDark=true]
title=Quarterly Performance
width=800
theme=tokyo
---
Q1 2023 | Jan | 120
Q1 2023 | Feb | 150
Q1 2023 | Mar | 180
Q2 2023 | Apr | 140
Q2 2023 | May | 170
Q2 2023 | Jun | 200
Q3 2023 | Jul | 160
Q3 2023 | Aug | 190
Q3 2023 | Sep | 220
[/docops]
```

## Options

- `smoothLines=true` for curved lines (default false)
- `showArea=true` to fill under lines (default true)
- `useDark=true`
- `width=800` (default 900)
- `backgroundColor=#F5F5F5`

## Multi-Series Example

```asciidoc
[docops,line]
----
title=Support KPIs
width=800
smoothLines=true
showArea=false
---
Tickets Opened | Jan | 120
Tickets Opened | Feb | 140
Tickets Opened | Mar | 135
Tickets Closed | Jan | 110
Tickets Closed | Feb | 150
Tickets Closed | Mar | 160
----
```

## Example: Smooth Lines With Area

```asciidoc
[docops,line]
----
title=User Growth Over Time
width=800
smoothLines=true
showArea=true
---
Active Users | Week 1 | 1200
Active Users | Week 2 | 1450
Active Users | Week 3 | 1680
Active Users | Week 4 | 1920
Active Users | Week 5 | 2100
----
```
