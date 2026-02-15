---
name: timeline
description: "Timeline is a skill that allows you to create and manage timelines in your documentation. It provides a visual representation of events, milestones, and progress over time."
trigger:
  - "create a timeline"
  - "add event to timeline"
  - "remove event from timeline"
  - "update event in timeline"
  - "view timeline"
---

## Key Features

* Horizontal layout - Events flow left-to-right along a center spine

* Alternating placement - Events alternate above/below for balanced design

* Multi-line support - Descriptions can span multiple lines with links

* Date flexibility - Use any date format (Q1 2024, Jan 15, etc.)

* Interactive elements - Clickable items with expandable details

 
## Usage

### Asciidoctor

```asciidoc
[docops,timeline,title="Project Milestones", useDark=false]
----
type=V
---
date: Q1 2024
text: Project Kickoff
Initial planning and team formation completed.

date: Q2 2024
text: Design Phase
UI/UX design and architecture planning.

date: Q3 2024
text: Development
Core functionality implementation.

date: Q4 2024
text: Testing & Launch
Quality assurance and public release.
----
```

### Markdown

```markdown
[docops,timeline]
type=H
---
date: Q1 2024
text: Project Kickoff
Initial planning and team formation completed.

date: Q2 2024
text: Design Phase
UI/UX design and architecture planning.

date: Q3 2024
text: Development
Core functionality implementation.

date: Q4 2024
text: Testing & Launch
Quality assurance and public release.
[/docops]
```

### URL format
the image url would be in the format of
https://roach.gy/extension/api/docops/svg?kind=<type>&payload=<base64_encoded_data>'

IMPORTANT: kind=timeline