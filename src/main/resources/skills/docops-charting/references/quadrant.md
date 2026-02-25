# Quadrant Charts (docops,quadrant)

Use quadrant charts for 2D prioritization maps like impact vs. effort.

## Asciidoc Format

```asciidoc
[docops,quadrant]
----
#title: Strategic Priority Matrix
#xAxis: EFFORT REQUIRED
#yAxis: IMPACT LEVEL
---
Feature A | 75 | 85 | Core
Feature B | 30 | 70 | Enhancement
Feature C | 60 | 40 | Nice-to-have
Feature D | 20 | 30 | Optional
----
```

## Markdown Format

```md
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
```

## Custom Quadrant Labels (Asciidoc)

```asciidoc
[docops,quadrant]
----
#title: Product Feature Prioritization
#xAxis: COMPLEXITY
#yAxis: BUSINESS VALUE
#leaders: QUICK WINS
#challengers: BIG BETS
#visionaries: MONEY PITS
#niche: INCREMENTAL
---
Feature A | 25 | 85 | Core
Feature B | 70 | 80 | Enhancement
Feature C | 80 | 30 | Nice-to-have
Feature D | 30 | 20 | Optional
Feature E | 50 | 50 | Maintenance
----
```

## Options

- `#title` chart title
- `#xAxis` X-axis label (default: EFFORT REQUIRED)
- `#yAxis` Y-axis label (default: IMPACT LEVEL)
- `#leaders` top-right quadrant label
- `#challengers` top-left quadrant label
- `#visionaries` bottom-left quadrant label
- `#niche` bottom-right quadrant label
- `visualVersion=2` for enhanced styling
- `useDark=true` for dark theme

## Example: Visual Version 2

```asciidoc
[docops,quadrant,useDark=true]
----
visualVersion=2
#title: Modern Design
---
Item | 50 | 50 | Category
----
```
