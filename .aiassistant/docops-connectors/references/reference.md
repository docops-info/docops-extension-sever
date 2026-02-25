# DocOps Connectors Reference

## Asciidoc Format

### Table Format
```asciidoc
[docops,connector]
----
Step | Color | Description
First Step | #E14D2A | Initial setup and configuration
Second Step | #82CD47 | Process data and validate
Third Step | #687EFF | Generate output and notify
----
```

### JSON Format
```asciidoc
[docops,connector]
----
{
  "connectors": [
    {
      "text": "First Step",
      "baseColor": "#E14D2A",
      "description": "Description text"
    }
  ]
}
----
```

## Markdown Format

```md
[docops:connector]
Step | Color | Description
Step 1 | #E14D2A | Detail 1
Step 2 | #82CD47 | Detail 2
[/docops]
```

## Options

- `useDark=true`: Enable dark theme.
- Table Columns: `Step (name) | Color (hex) | Description`.
