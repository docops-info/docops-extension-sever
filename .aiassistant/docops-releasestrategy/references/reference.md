# Release Strategy Reference

## Asciidoc Format

```asciidoc
[docops,release]
----
{
  "title": "Product Release Strategy",
  "style": "TLS",
  "scale": 1.0,
  "releases": [
    {
      "type": "M1",
      "date": "2024-03-01",
      "goal": "Initial Planning",
      "lines": [
        "Define release scope",
        "Identify key features"
      ]
    },
    {
      "type": "GA",
      "date": "2024-06-15",
      "goal": "Public Release",
      "lines": [
        "Production deployment",
        "Marketing launch"
      ]
    }
  ],
  "displayConfig": {
    "fontColor": "#e2e8f0",
    "colors": ["#06B6D4", "#6366F1", "#10B981"],
    "notesVisible": true
  }
}
----
```

## Markdown Format

```md
[docops:release]
{
  "title": "Cloud Platform Roadmap",
  "style": "R",
  "scale": 0.8,
  "useDark": true,
  "releases": [
    {
      "type": "M1",
      "date": "2024-Q1",
      "goal": "Foundation",
      "lines": ["Core setup", "CI/CD pipeline"]
    },
    {
      "type": "GA",
      "date": "2024-Q4",
      "goal": "Enterprise",
      "lines": ["Multi-region", "Compliance"]
    }
  ]
}
[/docops]
```

## Configuration Options

- `style`: `"TLS"` (Timeline) or `"R"` (Roadmap).
- `scale`: Numeric (e.g., `0.8`, `1.5`).
- `useDark`: `true` or `false`.
- `displayConfig.colors`: Array of 3 hex strings.
- `displayConfig.notesVisible`: `true` or `false`.
