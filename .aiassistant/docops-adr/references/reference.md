# DocOps ADR Reference

Architecture Decision Records (ADRs) capture architectural choices with their context and consequences.

## Syntax

### Asciidoc Structure
```asciidoc
[docops,adr,role=center,useDark=true]
----
title: Decision Title
status: Accepted
date: 2024-05-15
context:
- Point 1
- Point 2
decision:
- Action 1
- Action 2
consequences:
- Result 1
- Result 2
participants: Name (Role), Name (Role)
references:
[[https://example.com Link Text]]
----
```

### Markdown Structure
```md
[docops:adr]
template: brutalist
title: Decision Title
status: Accepted
date: 2024-05-15
context:
- Point 1
decision:
- Action 1
consequences:
- Result 1
participants: Name | Role | email@example.com | #color | emoji
[/docops]
```

## Fields

| Field | Description | Values / Format |
|-------|-------------|-----------------|
| `title` | The heading of the decision | String |
| `status` | Current state of the decision | Proposed, Accepted, Superseded, Deprecated, Rejected |
| `date` | When the decision was made | YYYY-MM-DD |
| `context` | Factors influencing the decision | List (use `- ` prefix) |
| `decision` | The actual choice made | List (use `- ` prefix) |
| `consequences` | Implications (positive/negative) | List (use `- ` prefix) |
| `participants` | People involved | Simple or Structured (see below) |
| `references` | Related documents | Wiki-style links: `[[url Label]]` |
| `template` | Visual style (Markdown only) | `brutalist` |
| `useDark` | Enable dark theme | `true` |

## Status Types

| Status | Meaning |
|--------|---------|
| **Proposed** | Decision being considered |
| **Accepted** | Decision approved and active |
| **Superseded** | Replaced by a newer decision |
| **Deprecated** | No longer relevant |
| **Rejected** | Decision not approved |

## Participant Formats

### Simple Format
Comma-separated names with optional roles in parentheses.
`participants: Jane Smith (Architect), John Doe (Developer)`

### Structured Format (Table style)
One person per line using pipe separators.
`Name | Title | Email | #ColorHex | Emoji`

Example:
```yaml
participants:
Jane Smith | Architect | jane@example.com | #6366F1 | 👩‍💻
John Doe | Developer | john@example.com | #10B981 | 👨‍💻
```

## Best Practices

- **Atomic Decisions**: One decision per ADR.
- **Traceability**: When a decision is superseded, update its status and link to the new ADR in `references`.
- **Honesty**: Include negative consequences and trade-offs.
- **Actionable**: Decisions should be stated clearly (e.g., "We will use..." rather than "We might use...").
