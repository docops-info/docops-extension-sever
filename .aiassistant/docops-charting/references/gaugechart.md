# Gauge Charts (docops,gauge)

Use gauges for single KPIs, thresholds, or compact multi-metric grids.

## Asciidoc Format

```asciidoc
[docops,gauge]
----
type=SEMI_CIRCLE
title=CPU Performance
showRanges=true
---
Label | Value | Min | Max | Unit | Color
CPU Load | 72 | 0 | 100 | % |
----
```

## Markdown Format

```md
[docops:gauge]
type=SEMI_CIRCLE
title=CPU Performance
useDark=false
scale=1.0
visualVersion=3
showRanges=true
---
Label | Value | Min | Max | Unit | Color
CPU Load | 72 | 0 | 100 | % |
[/docops]
```

## Gauge Types

- `SEMI_CIRCLE`
- `FULL_CIRCLE`
- `LINEAR`
- `SOLID_FILL`
- `MULTI_GAUGE`
- `DIGITAL`
- `DASHBOARD`

## Common Options

- `useDark=true`
- `showRanges=true` (default true)
- `showArc=true` (useful for `DIGITAL`)
- `animateArc=true` (default true)
- `columns=3` (for `MULTI_GAUGE` grids)

## Example: Digital Gauge

```asciidoc
[docops,gauge, useDark=true]
----
type=DIGITAL
title=Temperature Monitor
showArc=true
showStatus=true
---
Label | Value | Min | Max | Unit | Color | StatusText
Temperature | 43 | 0 | 100 | °C | #06B6D4 | optimal range
----
```

## Example: Linear Gauge With Target

```asciidoc
[docops,gauge]
----
type=LINEAR
showTarget=true
---
Label | Value | Min | Max | Unit | Color | Target
Budget Usage | 78 | 0 | 100 | % | | 80
----
```

## Example: Multi-Gauge Grid

```asciidoc
[docops,gauge]
----
type=MULTI_GAUGE
columns=3
---
Label | Value | Min | Max | Unit | Color
Server 1 | 45 | 0 | 100 | % |
Server 2 | 62 | 0 | 100 | % |
Server 3 | 88 | 0 | 100 | % |
----
```
