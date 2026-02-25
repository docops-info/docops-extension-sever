# DocOps Buttons Reference

Use DocOps buttons to create organized navigation grids. The configuration is JSON-based.

## Asciidoc Format

```asciidoc
[docops,buttons]
----
{
  "buttons": [
    {
      "label": "Google",
      "link": "https://www.google.com",
      "type": "search"
    },
    {
      "label": "Amazon",
      "link": "https://www.amazon.com",
      "description": "Storefront",
      "type": "shopping"
    }
  ],
  "buttonType": "REGULAR",
  "theme": {
    "columns": 2,
    "scale": 1.0,
    "colors": ["#003b6b", "#FF6F36"]
  }
}
----
```

## Markdown Format

```md
[docops:buttons]
{
  "buttons": [
    {
      "label": "GitHub",
      "link": "https://github.com",
      "type": "dev"
    }
  ],
  "buttonType": "PILL",
  "theme": {
    "columns": 3,
    "scale": 0.8
  }
}
[/docops]
```

## Button Object Schema

| Field | Description | Type |
|-------|-------------|------|
| `label` | Main text on the button | String (Required) |
| `link` | Destination URL | String (Required) |
| `description` | Subtext below the label | String |
| `type` | Category for color mapping/sorting | String |
| `date` | Associated date (MM/DD/YYYY) | String |
| `author` | Array of creator names | Array of Strings |
| `embeddedImage.ref` | Path to icon or logo | String |
| `links` | Array of sub-links `{"label": "...", "href": "..."}` | Array of Objects |
| `activeName` | Matches `docname` to highlight current button | String |

## Theme Schema

| Field | Description | Type |
|-------|-------------|------|
| `columns` | Number of columns (1-4) | Integer |
| `scale` | Size multiplier (default 1.0) | Float |
| `colors` | Array of hex colors for buttons | Array of Strings |
| `colorTypeMap` | Map of `type` to hex color | Object |
| `sortBy` | Sorting config: `{"sort": "LABEL/TYPE/DATE/ORDER", "direction": "ASCENDING/DESCENDING"}` | Object |
| `useDark` | Enable dark mode styling | Boolean |
| `newWin` | Open links in new window | Boolean |
| `buttonStyle` | Custom CSS for text: `labelStyle`, `descriptionStyle`, etc. | Object |

## Button Types (`buttonType`)

- `REGULAR`
- `PILL`
- `LARGE`
- `RECTANGLE`
- `ROUND`
- `CIRCLE`
- `SLIM`
- `OVAL`
- `HEX`

## Best Practices

- Use **2-3 columns** for best readability.
- Keep **labels short** (1-3 words).
- Limit grid to **6-12 buttons** to avoid fatigue.
- Reserve **strong colors** for primary actions.
