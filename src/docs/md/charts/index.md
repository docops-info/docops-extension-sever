# Bar Charts
[TOC]

## Default

[docops:bar]
title=Berry Picking by Month 2024
yLabel=Number of Sales
xLabel=Month
vBar=true
---
Jan | 120.0
Feb | 334.0
Mar | 455.0
Apr | 244.0
May | 256.0
Jun | 223.0
[/docops]

## Cylinder Type

[docops:bar]
title=Berry Picking by Month 2024
yLabel=Number of Sales
xLabel=Month
type=C
---
Jan | 120.0
Feb | 334.0
Mar | 455.0
Apr | 244.0
May | 256.0
Jun | 223.0
[/docops]

## Bar Chart Grouping

### Default

[docops:bargroup]
title=Annual Product Sales Report
yLabel=Sales (USD)
xLabel=Quarters
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

### Condensed

[docops:bargroup]
title=Annual Product Sales Report
yLabel=Sales (USD)
xLabel=Quarters
vBar=true
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

### Brutalist Theme

[docops:bargroup]
title=System Performance Analysis
yLabel=Latency (ms)
xLabel=Data Centers
theme=brutalist
---
Edge Node | Tokyo | 24.5
Edge Node | London | 45.2
Edge Node | New York | 38.8
Core Switch | Tokyo | 12.1
Core Switch | London | 18.4
Core Switch | New York | 15.6
Gateway | Tokyo | 88.3
Gateway | London | 95.0
Gateway | New York | 91.2
[/docops]

## Combination Charts

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

## Line Charts

[docops:line]
title=Quarterly Performance
width=800
colors=#6a0dad,#0da6a0,#daad0d
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

## Pie Charts

### Donut
[docops:pieslice]
legend=false
---
Product A | 30
Product B | 25
Product C | 20
Product D | 15
Product E | 10

[/docops]

### Regular

[docops:pieslice]
title=Sales Distribution by Product
width=700
height=600
legend=true
percentages=true
donut=false
---
Product A | 30
Product B | 25
Product C | 20
Product D | 15
Product E | 10

[/docops]

### Multi-Pie Charts
[docops:pie]
outlineColor=#FA4032
scale=1
---
Label | Percent
Toys | 14
Furniture | 43
Home Decoration | 15
Electronics | 28

[/docops]