# DocOps Treemap Visualizer

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/TreemapIcon.svg" alt="Treemap Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #7c3aed; font-size: 32px;">DocOps Treemap</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Visualize proportional data through nested rectangles for budget, resource, and market analysis</p>
    </div>
  </div>
</div>

[TOC]

## What is DocOps Treemap?

DocOps Treemap transforms hierarchical data into nested rectangle visualizations where area represents proportion. Use it for budget allocations, disk space usage, portfolio breakdowns, or any data where relative size matters.

### Key Features

- **Proportional visualization** - Rectangle size reflects value magnitude
- **Multiple themes** - Modern, brutalist, and glassmorphic styles
- **Color palettes** - Ocean Breeze, Sunset, Vibrant, and more
- **Flexible metrics** - Display values, percentages, or custom labels

---

## Default Look

[docops:treemap]
title=Project Portfolio 2024
subtitle=BUDGET ALLOCATION BY DEPARTMENT
width=1200
height=800
theme=modern
paletteType=PASTEL
showValues=true
showPercentages=true
---
Engineering | 2400000 | Development & Infrastructure | | $2.4M
Product | 980000 | Design & Research | | $980K
Marketing | 1200000 | Growth & Brand | | $1.2M
Sales | 720000 | Revenue Operations | | $720K
Operations | 420000 | Support | | $420K
[/docops]

---

## Cloud Infrastructure Costs

Track cloud spending across different services:

[docops:treemap]
title=Cloud Infrastructure Costs
subtitle=MONTHLY AWS SPENDING BREAKDOWN
width=1200
height=800
theme=glassmorphic
paletteType=PASTEL
showValues=true
showPercentages=false
---
EC2 Instances | 45000 | Compute resources | | $45K
RDS Databases | 28000 | Database hosting | | $28K
S3 Storage | 12000 | Object storage | | $12K
CloudFront CDN | 8500 | Content delivery | | $8.5K
Lambda Functions | 6200 | Serverless compute | | $6.2K
EBS Volumes | 5800 | Block storage | | $5.8K
ElastiCache | 4500 | In-memory caching | | $4.5K
Data Transfer | 3200 | Network egress | | $3.2K
Route 53 | 1800 | DNS services | | $1.8K
[/docops]

---

## Market Share Analysis

Visualize market distribution among competitors:

[docops:treemap]
title=Smartphone Market Share 2024
subtitle=GLOBAL MARKET DISTRIBUTION
width=1200
height=800
theme=modern
paletteType=PASTEL
showValues=true
showPercentages=true
---
Samsung | 285 | South Korean electronics | | 28.5%
Apple | 243 | Premium smartphones | | 24.3%
Xiaomi | 156 | Value-focused brand | | 15.6%
Oppo | 98 | Chinese manufacturer | | 9.8%
Vivo | 87 | BBK Electronics brand | | 8.7%
Others | 131 | Various manufacturers | | 13.1%
[/docops]

---

## Brutalist Theme

High-contrast styling for bold presentations:

[docops:treemap]
title=Department Budget 2024
subtitle=ANNUAL SPENDING ALLOCATION
width=1200
height=800
theme=brutalist
paletteType=TABLEAU
showValues=true
showPercentages=true
---
Engineering | 2400000 | Development & Infrastructure | | $2.4M
Product | 980000 | Design & Research | | $980K
Marketing | 1200000 | Growth & Brand | | $1.2M
Sales | 720000 | Revenue Operations | | $720K
Operations | 420000 | Support | | $420K
[/docops]

---

## Format Options

### Treemap Structure

Use pipe-separated values for each data row:

```text 
[docops:treemap] 
title=Your Title 
subtitle=Optional Subtitle 
width=1200 
height=800 
theme=modern 
paletteType=OCEAN_BREEZE 
showValues=true 
showPercentages=true
Label | Value | Description | Color | Metric 
[/docops]
```

### Data Fields

- **Label** (required) - Category name
- **Value** (required) - Numeric value for sizing
- **Description** (optional) - Additional context
- **Color** (optional) - Custom hex color
- **Metric** (optional) - Formatted display value

### Layout Options

| Parameter | Description | Default |
|-----------|-------------|---------|
| `title` | Main chart title | "Budget Allocation" |
| `subtitle` | Secondary title | "" |
| `width` | Chart width in pixels | 1200 |
| `height` | Chart height in pixels | 800 |
| `theme` | Visual theme | "modern" |
| `paletteType` | Color palette | "" |
| `showValues` | Display numeric values | true |
| `showPercentages` | Display percentages | true |

### Available Themes

- **modern** - Clean, contemporary design with gradients
- **brutalist** - High-contrast, bold design
- **glassmorphic** - Frosted glass effect

### Available Color Palettes

- `OCEAN_BREEZE` - Blues and teals
- `SUNSET` - Warm oranges and reds
- `FOREST` - Greens and earth tones
- `VIBRANT` - High-saturation colors
- `PROFESSIONAL` - Muted business colors
- `NEON` - Electric bright colors
- `URBAN_NIGHT` - Dark mode optimized
- `TABLEAU` - Data visualization standard
- `PASTEL` - Soft, muted tones

---

## Best Practices

- **Keep categories between 4-10** - Too few loses impact, too many clutters
- **Use meaningful labels** - Concise but descriptive names
- **Match theme to context** - Modern for reports, brutalist for presentations
- **Add descriptions** - Context without cluttering the visual

<div style="background: #f5f3ff; border-left: 4px solid #a78bfa; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #7c3aed; font-weight: 600;">📊 Treemap Tip</p>
  <p style="margin: 8px 0 0 0; color: #475569;">Treemaps work best when values have meaningful differences. If all categories are similar in size, consider a bar chart instead.</p>
</div>

---

## Common Use Cases

- **Budget allocations** - Departmental spending breakdowns
- **Resource distribution** - Team allocation across projects
- **Market share analysis** - Competitive landscape visualization
- **Storage usage** - Disk space by category
- **Portfolio composition** - Investment allocation displays
- **Traffic sources** - Website visitor breakdown