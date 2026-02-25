# DocOps Placemat Reference

## Asciidoc Format

```asciidoc
[docops,placemat]
----
{
  "title": "System Architecture Overview",
  "placeMats": [
    {"name": "Frontend", "legend": "UI"},
    {"name": "Backend", "legend": "API"},
    {"name": "Database", "legend": "DATA"}
  ],
  "config": {
    "legend": [
      {"legend": "UI", "color": "#4361ee"},
      {"legend": "API", "color": "#3a0ca3"},
      {"legend": "DATA", "color": "#7209b7"}
    ]
  }
}
----
```

## Markdown Format

```md
[docops:placemat]
{
  "title": "Modern Tech Stack",
  "placeMats": [
    {"name": "React", "legend": "Frontend"},
    {"name": "Node.js", "legend": "Backend"}
  ],
  "config": {
    "legend": [
      {"legend": "Frontend", "color": "#4cc9f0"},
      {"legend": "Backend", "color": "#4361ee"}
    ]
  }
}
[/docops]
```

## Options

- `title`: Diagram heading.
- `placeMats`: Array of `{"name": "text", "legend": "category"}`.
- `config.legend`: Array of `{"legend": "category", "color": "#hex"}`.
- `fill`: `false` for outlined boxes (default: `true`).
- `useDark`: `true` for dark theme.
- `scale`: Size multiplier (default: 1.0).
