# DocOps Planner Reference

## Asciidoc Format

```asciidoc
[docops,planner,title="Q2 Roadmap"]
----
- now User Authentication
Complete OAuth2 integration and JWT token handling

- next API Documentation
Generate OpenAPI specs and interactive docs

- later Mobile App
Native iOS and Android applications

- done Database Migration
PostgreSQL upgrade completed successfully
----
```

## Markdown Format

```md
[docops:planner]
title="Development Roadmap"
---
- now Feature A
Description of feature A

- next Feature B
Description of feature B
[/docops]
```

## Options

- `title`: Planner heading.
- `useDark`: `true` for dark theme.

## Task Stages
- `- now`: Current focus.
- `- next`: Immediate queue.
- `- later`: Backlog.
- `- done`: Completed tasks.
