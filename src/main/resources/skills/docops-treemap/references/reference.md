# DocOps Treemap Reference

Treemaps visualize proportional data through nested rectangles where area represents value.

## Asciidoc Format

```asciidoc
[docops,treemap,controls=true]
----
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
----
```

## Markdown Format

```md
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
[/docops]
```

## Data Format

Pipe-separated values:
`Label | Value | Description | Color | Metric`

- **Label** (required): Category name.
- **Value** (required): Numeric value for sizing.
- **Description** (optional): Tooltip context.
- **Color** (optional): Custom hex color.
- **Metric** (optional): Formatted display value (e.g., "$2.4M").

## Configuration Options

| Option | Values | Purpose |
|--------|--------|---------|
| `title` | string | Main chart title |
| `subtitle` | string | Secondary title (all caps recommended) |
| `width` | integer | Canvas width (default: 1200) |
| `height` | integer | Canvas height (default: 800) |
| `theme` | `modern`, `brutalist`, `glassmorphic` | Visual style |
| `paletteType` | see below | Color scheme |
| `showValues` | `true`/`false` | Display numeric values |
| `showPercentages` | `true`/`false` | Display percentages |
| `useDark` | `true`/`false` | Dark mode |

## Color Palettes

- `OCEAN_BREEZE`: Blues and teals
- `SUNSET`: Warm oranges and reds
- `FOREST`: Greens and earth tones
- `VIBRANT`: High-saturation colors
- `PROFESSIONAL`: Muted business colors
- `NEON`: Electric bright colors
- `URBAN_NIGHT`: Dark mode optimized
- `TABLEAU`: Data visualization standard
- `PASTEL`: Soft, muted tones

## Best Practices

- **Limit categories**: Use 4-10 categories for best readability.
- **Format Metrics**: Use the Metric column for human-readable values ($1.2M) while keeping the Value column numeric for sizing.
- **Semantic Palettes**: Use `OCEAN_BREEZE` for tech/cloud, `SUNSET` for finance.
- **Descriptions**: Add context in the description field to keep the visual clean.
