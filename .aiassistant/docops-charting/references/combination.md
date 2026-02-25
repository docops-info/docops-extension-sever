# Combination Charts (docops,combination)

Combine bars and lines on one chart, optionally with dual Y-axes.

## Asciidoc Format

```asciidoc
[docops,combination]
----
title=Chart Title
xLabel=X-Axis Label
yLabel=Primary Y-Axis Label
yLabelSecondary=Secondary Y-Axis Label
dualYAxis=true
smoothLines=true
showGrid=true
---
Series Name | Type | X-Value | Y-Value | Color | Y-Axis
Revenue | BAR | Q1 | 50000 | #3498db | PRIMARY
Growth Rate | LINE | Q1 | 15.5 | #e74c3c | SECONDARY
----
```

## Markdown Format

```md
[docops:combination]

title=Financial Performance Q1-Q4 2024
xLabel=Quarter
yLabel=Amount ($000)
yLabelSecondary=Margin (%)
useDark=false
dualYAxis=true
showGrid=true
smoothLines=false
showPoints=true
baseColor=#2c3e50
---
Revenue | BAR | Q1 2024 | 450 | #3498db | PRIMARY
Revenue | BAR | Q2 2024 | 520 | #3498db | PRIMARY
Revenue | BAR | Q3 2024 | 580 | #3498db | PRIMARY
Revenue | BAR | Q4 2024 | 650 | #3498db | PRIMARY
Expenses | BAR | Q1 2024 | 320 | #e67e22 | PRIMARY
Expenses | BAR | Q2 2024 | 350 | #e67e22 | PRIMARY
Expenses | BAR | Q3 2024 | 380 | #e67e22 | PRIMARY
Expenses | BAR | Q4 2024 | 420 | #e67e22 | PRIMARY
Profit Margin | LINE | Q1 2024 | 28.9 | #27ae60 | SECONDARY
Profit Margin | LINE | Q2 2024 | 32.7 | #27ae60 | SECONDARY
Profit Margin | LINE | Q3 2024 | 34.5 | #27ae60 | SECONDARY
Profit Margin | LINE | Q4 2024 | 35.4 | #27ae60 | SECONDARY

[/docops]
```

## Options

- `dualYAxis=true` to enable a secondary axis
- `smoothLines=true` for curved lines
- `showGrid=true`
- `showLegend=true`
- `showPoints=true`
- `useDark=true`

## Data Columns

- `Series Name` (legend label)
- `Type` = `BAR` or `LINE`
- `X-Value` (category/time label)
- `Y-Value` (numeric)
- `Color` (hex)
- `Y-Axis` = `PRIMARY` or `SECONDARY`

## Example: Bars + Line

```asciidoc
[docops,combination]
----
title=Sales Volume vs Profit Margin
xLabel=Quarter
yLabel=Units Sold
yLabelSecondary=Margin (%)
dualYAxis=true
---
Units Sold | BAR | Q1 | 1200 | #3498db | PRIMARY
Units Sold | BAR | Q2 | 1450 | #3498db | PRIMARY
Units Sold | BAR | Q3 | 1680 | #3498db | PRIMARY
Units Sold | BAR | Q4 | 1920 | #3498db | PRIMARY
Profit Margin | LINE | Q1 | 22.5 | #2ecc71 | SECONDARY
Profit Margin | LINE | Q2 | 24.8 | #2ecc71 | SECONDARY
Profit Margin | LINE | Q3 | 26.2 | #2ecc71 | SECONDARY
Profit Margin | LINE | Q4 | 28.1 | #2ecc71 | SECONDARY
----
```
