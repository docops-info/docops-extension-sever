# Data Visualization Charts

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/ChartIcon.svg" alt="Chart Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #dc2626; font-size: 32px;">DocOps Charts</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Transform raw data into compelling visual stories with professional charts</p>
    </div>
  </div>
</div>

[TOC]

## What are DocOps Charts?

DocOps Charts provide a powerful suite of data visualization tools that transform tabular data into professional, SVG-based charts. Whether you need to show trends, compare values, display distributions, or analyze correlations, DocOps Charts offer the flexibility and visual appeal to make your data meaningful.

### Available Chart Types

- **Bar Charts** - Compare values across categories with vertical or horizontal bars
- **Grouped Bar Charts** - Compare multiple data series side-by-side
- **Line Charts** - Show trends and changes over time
- **Pie Charts** - Display proportional relationships and distributions
- **Donut Charts** - Modern variation of pie charts with emphasis on segments
- **Combination Charts** - Mix bars and lines for multi-dimensional analysis
- **Magic Quadrant** - Analyze positioning across two dimensions

<div style="background: #fef2f2; border-left: 4px solid #dc2626; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #991b1b; font-weight: 600;">📊 Data-Driven Documentation</p>
  <p style="margin: 8px 0 0 0; color: #b91c1c;">Charts make complex data accessible and memorable. Use them to support arguments, show progress, and communicate insights effectively.</p>
</div>

---

## Bar Charts

### Simple Bar Chart

Perfect for comparing values across categories or time periods.

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

**Configuration:**

[docops:bar] 
title=Berry Picking by Month 2024 
yLabel=Number of Sales 
xLabel=Month 
vBar=true
Jan | 120.0 
Feb | 334.0
Mar | 455.0 
Apr | 244.0 
May | 256.0 
Jun | 223.0 
[/docops]

### Cylinder Bar Chart

Add visual interest with 3D cylinder-style bars.

[docops:bar]
title=Berry Picking by Month 2024
yLabel=Number of Sales
xLabel=Month
type=C
paletteType=OCEAN_BREEZE
---
Jan | 120.0
Feb | 334.0
Mar | 455.0
Apr | 244.0
May | 256.0
Jun | 223.0
[/docops]

---

## Grouped Bar Charts

### Standard Grouped Bars

Compare multiple data series across categories.

[docops:bargroup]
title=Annual Product Sales Report
yLabel=Sales (USD)
xLabel=Quarters
paletteType=OCEAN_BREEZE
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

### Condensed Layout

Vertical grouping for compact display.

[docops:bargroup]
title=Annual Product Sales Report
yLabel=Sales (USD)
xLabel=Quarters
vBar=true
paletteType=OCEAN_BREEZE
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

High-contrast styling for bold impact.

[docops:bargroup]
title=System Performance Analysis
yLabel=Latency (ms)
xLabel=Data Centers
theme=brutalist
paletteType=OCEAN_BREEZE
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

---

## Combination Charts

Mix different chart types for multi-dimensional analysis. Perfect for showing relationships between different metrics on the same timeline.

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

**Key Features:**
- **Dual Y-Axes** - Different scales for bars and lines
- **Mixed Chart Types** - Bars for absolute values, lines for percentages
- **Custom Colors** - Define colors per series
- **Grid Lines** - Optional background grid for readability

---

## Line Charts

Visualize trends and changes over time with smooth or angular lines.

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

**When to use Line Charts:**
- Tracking metrics over time
- Showing growth or decline trends
- Comparing multiple time series
- Displaying continuous data

---

## Pie Charts

### Donut Chart (Recommended)

Modern, clean donut style with better visual hierarchy.

[docops:pieslice]
legend=false
---
Product A | 30
Product B | 25
Product C | 20
Product D | 15
Product E | 10

[/docops]

### Standard Pie Chart

Classic pie chart with optional legend and percentages.

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

### Enhanced Donut Chart

[docops:pieslice]
title=Sales Distribution by Product
width=700
height=600
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

### Modern Visual Style

[docops:pieslice]
title=Sales Distribution by Product
width=700
height=600
legend=true
percentages=true
donut=true
visualVersion=2
---
Product A | 30
Product B | 25
Product C | 20
Product D | 15
Product E | 10

[/docops]

### Portfolio Split Example

[docops:pieslice]
title=Recommended Portfolio Split
width=700
height=600
legend=true
percentages=true
donut=true
visualVersion=2
---
Equity | 30
Real-estate | 27
Debt | 23
Bullion | 18
Insurance | 2

[/docops]

### Multi-Pie Display

Show multiple related pie charts in a single visualization.

[docops:pie]
outlineColor=#FA4032
scale=1
---
Toys | 14
Furniture | 43
Home Decoration | 15
Electronics | 28

[/docops]

---

## Gauge Charts

Gauge charts are used to display a single metric or value within a range, often used for performance indicators or progress tracking.


### Semi Circle

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

### Full Circle

[docops:gauge]
type=FULL_CIRCLE
title=System Uptime
useDark=false
scale=1.2
showLabel=true
---
Label | Value | Min | Max | Unit | Color
Uptime | 85 | 0 | 100 | days | #10B981
[/docops]

### Linear

[docops:gauge]
type=LINEAR
title=Response Time Analysis
useDark=false
showTarget=true
showRanges=true
---
Label | Value | Min | Max | Unit | Color | Target
Response Time | 68 | 0 | 100 | ms | #F59E0B | 80
[/docops]

### Solid Fill

[docops:gauge]
type=SOLID_FILL
title=Disk Usage Monitor
useDark=false
scale=1.0
innerRadius=60
---
Label | Value | Min | Max | Unit | Color
Disk Usage | 92 | 0 | 100 | % | #EF4444
[/docops]   

### Multi Gauge

[docops:gauge]
type=MULTI_GAUGE
title=Server Fleet Status
useDark=false
columns=3
scale=0.8
showLegend=false
---
Label | Value | Min | Max | Unit | Color
server-01 | 75 | 0 | 100 | % | #10B981
server-02 | 85 | 0 | 100 | % | #F59E0B
server-03 | 65 | 0 | 100 | % | #10B981
server-04 | 90 | 0 | 100 | % | #EF4444
server-05 | 70 | 0 | 100 | % | #10B981
server-06 | 80 | 0 | 100 | % | #F59E0B
[/docops]

### Digital

[docops:gauge]
type=DIGITAL
title=Temperature Monitor
useDark=false
showArc=true
showStatus=true
---
Label | Value | Min | Max | Unit | Color | StatusText
Temperature | 43 | 0 | 100 | °C | #06B6D4 | optimal range
[/docops]

### Dashboard Gauge

[docops:gauge]
type=DASHBOARD
title=System Health Dashboard
useDark=false
layout=2x3
scale=1.0
---
Type | Label | Value | Min | Max | Unit | Color | Extra
SEMI_CIRCLE | CPU | 72 | 0 | 100 | % | #F59E0B |
FULL_CIRCLE | Uptime | 85 | 0 | 100 | days | #10B981 |
LINEAR | Response | 68 | 0 | 100 | ms | #F59E0B | target=80
SOLID_FILL | Disk | 92 | 0 | 100 | % | #EF4444 |
DIGITAL | Temp | 43 | 0 | 100 | °C | #06B6D4 | status=optimal
MULTI_GAUGE | Memory | 75 | 0 | 100 | % | #10B981 |
[/docops]

## Magic Quadrant Chart

Analyze and visualize positioning across two dimensions. Perfect for risk assessment, competitive analysis, and strategic planning.

[docops:quadrant]
#title: Project Risk Assessment

#xAxis: PROBABILITY
#yAxis: IMPACT
#leaders: HIGH PRIORITY
#challengers: CRITICAL
#visionaries: LOW PRIORITY
#niche: MEDIUM PRIORITY

---
Security Breach | 30 | 95 | Security
Budget Overrun | 70 | 75 | Financial
Schedule Delay | 80 | 60 | Timeline
Scope Creep | 85 | 50 | Management
Resource Shortage | 60 | 70 | Staffing
Technical Failure | 40 | 85 | Technical
[/docops]

**Use Cases:**
- Risk assessment matrices
- Technology/vendor evaluation
- Strategic positioning analysis
- Priority mapping

---

## Chart Configuration Guide

### Common Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `title` | Chart title | `title=Q4 Sales Report` |
| `xLabel` | X-axis label | `xLabel=Month` |
| `yLabel` | Y-axis label | `yLabel=Revenue ($)` |
| `width` | Chart width in pixels | `width=800` |
| `height` | Chart height in pixels | `height=600` |
| `colors` | Custom color palette | `colors=#3498db,#e74c3c` |
| `theme` | Visual theme | `theme=brutalist` |
| `useDark` | Dark mode | `useDark=true` |

### Bar Chart Specific

| Parameter | Description | Default |
|-----------|-------------|---------|
| `vBar` | Vertical bars | `false` |
| `type` | Bar style (C=Cylinder) | `standard` |

### Pie Chart Specific

| Parameter | Description | Default |
|-----------|-------------|---------|
| `donut` | Donut style | `false` |
| `legend` | Show legend | `true` |
| `percentages` | Show percentages | `false` |
| `visualVersion` | Visual style version | `1` |

### Combination Chart Specific

| Parameter | Description | Default |
|-----------|-------------|---------|
| `dualYAxis` | Two Y-axes | `false` |
| `showGrid` | Display grid | `true` |
| `smoothLines` | Smooth line curves | `true` |
| `showPoints` | Show data points | `true` |

---

## Best Practices

### Chart Selection

1. **Bar Charts** - Comparing discrete categories or time periods
2. **Line Charts** - Showing trends and continuous data
3. **Pie Charts** - Displaying parts of a whole (limit to 5-7 segments)
4. **Combination** - Multi-metric analysis with different scales
5. **Quadrant** - Two-dimensional positioning analysis

### Design Guidelines

<div style="background: #fffbeb; border-left: 4px solid #f59e0b; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #92400e; font-weight: 600;">💡 Pro Tips</p>
  <ul style="margin: 8px 0 0 0; color: #b45309; padding-left: 20px;">
    <li>Use consistent color schemes across related charts</li>
    <li>Label axes clearly with units of measurement</li>
    <li>Keep titles concise but descriptive</li>
    <li>Limit pie chart segments to 7 or fewer</li>
    <li>Use donut charts for better visual hierarchy</li>
    <li>Add grid lines for easier value reading</li>
  </ul>
</div>

### Color Strategy

- **Sequential** - Single hue with varying intensity for ordered data
- **Categorical** - Distinct colors for unrelated categories
- **Diverging** - Two hues for data with a critical midpoint
- **Highlight** - Muted colors with one accent for emphasis

### Accessibility

- Use high-contrast colors
- Don't rely solely on color to convey information
- Include clear labels and legends
- Provide alternative text descriptions
- Consider colorblind-friendly palettes

---

## Common Use Cases

### Business Reporting

**Quarterly Revenue Tracking**
- Line charts for trend analysis
- Bar charts for period comparison
- Combination charts for revenue vs. margin

### Technical Documentation

**System Performance Metrics**
- Grouped bars for multi-server comparison
- Line charts for uptime tracking
- Quadrant charts for resource allocation

### Project Management

**Sprint Velocity**
- Bar charts for story points completed
- Line charts for velocity trends
- Combination for planned vs. actual

### Financial Analysis

**Budget vs. Actual**
- Grouped bars for department spending
- Line charts for cash flow
- Pie charts for expense categories

---

## Advanced Techniques

### Data Normalization

For combination charts with different scales:
```markdown
Revenue | BAR | Q1 | 450000 | #3498db | PRIMARY
Growth % | LINE | Q1 | 15 | #27ae60 | SECONDARY
```

### Color Coordination
Define a consistent palette:
- **Primary** - #3498db (blue)
- **Secondary** - #27ae60 (green)
- **Accent** - #e74c3c (red)
- **Neutral** - #95a5a6 (gray)

Use these colors consistently across charts for a cohesive look.

### Responsive Sizing
Adjust chart dimensions for different contexts:
- Full width: width=1200, height=600
- Half width: width=600, height=400
- Compact: width=400, height=300

## Ready to visualize your data?
Transform numbers into insights with DocOps Charts

## Key Features of This Update:

1. **Hero Section** - Red gradient (#dc2626 → #ef4444) matching the ChartIcon.svg
2. **Comprehensive Coverage** - All chart types with working examples
3. **Configuration Tables** - Clear parameter references for each chart type
4. **Best Practices** - Practical guidance on chart selection and design
5. **Use Case Examples** - Real-world scenarios for each chart type
6. **Visual Callouts** - Red info boxes and yellow tips throughout
7. **Advanced Techniques** - Tips for power users
8. **Accessibility** - Guidelines for inclusive visualizations

The documentation provides a complete guide to the charts feature, showcasing the variety of available visualizations while maintaining consistent branding with the red theme that represents data and analytics.
