# Bar Charts (docops,bar / docops,bargroup)

Use bar charts for ranked comparisons or category totals. Use grouped bars for multiple series per category.

## Asciidoc Format

### Simple Bar

```asciidoc
[docops,bar]
----
title=Monthly Sales Performance
yLabel=Revenue ($)
xLabel=Month
type=R
---
January | 120.0
February | 334.0
March | 455.0
April | 244.0
May | 256.0
June | 223.0
----
```

### Grouped Bar

```asciidoc
[docops,bargroup]
----
title=Chart Title
yLabel=Y-Axis Label
xLabel=X-Axis Label
paletteType=OCEAN_BREEZE
vBar=false
---
Group 1 | Category 1 | 5000.0
Group 1 | Category 2 | 7000.0
Group 2 | Category 1 | 6000.0
Group 2 | Category 2 | 8000.0
----
```

## Markdown Format

### Simple Bar

```md
[docops:bar]
title=Berry Picking by Month 2024
yLabel=Number of Sales
xLabel=Month
vBar=true
theme=sakura
---
Jan | 120.0
Feb | 334.0
Mar | 455.0
Apr | 244.0
May | 256.0
Jun | 223.0
[/docops]
```

### Grouped Bar

```md
[docops:bargroup]
title=Annual Product Sales Report
yLabel=Sales (USD)
xLabel=Quarters
paletteType=OCEAN_BREEZE
theme=brand
---
Product A | Q1 | 5000.0
Product A | Q2 | 7000.0
Product A | Q3 | 8000.0
Product A | Q4 | 6000.0
Product B | Q1 | 6000.0
Product B | Q2 | 8000.0
Product B | Q3 | 7000.0
Product B | Q4 | 9000.0
[/docops]
```

## Options

- `type` = `R` (regular) or `C` (cylinder)
- `vBar=true` for vertical bars (default is horizontal)
- `sorted=true` to rank bars by value
- `useDark=true` for dark theme
- `scale=1.5` to resize
- `paletteType` for grouped bars: `OCEAN_BREEZE`, `CORPORATE`, `SUNSET`

## Custom Colors

Add a third column for hex colors:

```asciidoc
Category 1 | 120.0 | #3498db
Category 2 | 334.0 | #e74c3c
```

## Example: Cylinder Style

```asciidoc
[docops,bar]
----
title=Quarterly Revenue
yLabel=Revenue ($)
type=C
theme=everest
---
Q1 | 50000
Q2 | 65000
Q3 | 70000
Q4 | 80000
----
```
