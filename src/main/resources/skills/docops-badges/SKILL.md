---
name: badges
description: "Badges and shields add visual metadata to documentation—version numbers, build status, coverage metrics, or any label/value pair you want to highlight. They’re compact, scannable, and instantly recognizable."
trigger:
  - "create badge"
  - "create shield"
  - "create tag"
---

Key Features

* 7 visual styles - Classic, glass morphism, neon, brutalist, gradient, minimal, neumorphic

* Icon support - Embed logos from SimpleIcons library (1000+ icons)

* Flexible layouts - Horizontal, vertical, or grid arrangements

* Clickable badges - Link to builds, releases, or documentation

* Dark mode optimized - Auto-adapts to user preferences

## Asciidoctor Format

```asciidoc
[docops,badge,useDark=true]
----
type=classic
---
Made With|Kotlin||#06133b|#6fc441|<kotlin>|#fcfcfc
Framework|Spring Boot 3.2||#6db33f|#44cc11|<spring>|#ffffff
----
```

## Markdown Format

```markdown
[docops,badge]
type=classic
theme=auto
spacing=1
direction=horizontal
perRow=3
---
Made With|Kotlin||#06133b|#6fc441|<kotlin>|#fcfcfc
Framework|Spring Boot 3.2||#6db33f|#44cc11|<spring>|#ffffff
[/docops]
```

## URL format 
the image url would be in the format of
https://roach.gy/extension/api/docops/svg?kind=<type>&payload=<base64_encoded_data>'

IMPORTANT: kind=badge

## Common Options

* type - classic, glassmorphic, neon, brutalist, gradient, minimal, neumorphic

* theme - auto, light, dark, both

* arrangement - HORIZONTAL, VERTICAL, GRID

* spacing=8 - Space between badges (pixels)

## Best Practices

* Use icons for recognition - <kotlin>, <github>, <docker> from SimpleIcons

* Color-code by meaning - Green=passing, red=failing, yellow=warning

* Keep labels short - 1-2 words max for readability

* Link to details - Use URL field for builds, releases, coverage reports

* Group related badges - Use grid layout for tech stack or status dashboard

