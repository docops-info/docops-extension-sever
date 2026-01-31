# Mermaid Diagrams

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
<div style="background: linear-gradient(135deg, #ff3670 0%, #ff6b9d 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/MermaidIcon.svg" alt="Mermaid Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #ff3670; font-size: 32px;">Mermaid Diagrams</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Create diagrams and flowcharts from text using Mermaid.js syntax</p>
    </div>
  </div>
</div>

[TOC]

## Overview

DocOps Converter now supports **Mermaid.js** diagrams directly within your Markdown documentation. Mermaid is a JavaScript-based diagramming tool that renders Markdown-inspired text definitions to create and modify diagrams dynamically.

Simply write your diagram definition inside a fenced code block with the `mermaid` language identifier, and DocOps will render it as an interactive SVG diagram.

<div style="background: #fef2f2; border-left: 4px solid #ff3670; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #be123c; font-weight: 600;">📊 Diagrams as Code</p>
  <p style="margin: 8px 0 0 0; color: #9f1239;">Mermaid lets you create complex diagrams using simple text syntax. Version control your diagrams alongside your code!</p>
</div>

---

## Supported Diagram Types

DocOps supports all major Mermaid diagram types:

| Diagram Type | Description |
|--------------|-------------|
| **Flowchart** | Process flows and decision trees |
| **Sequence** | Interaction between actors/systems |
| **Class** | Object-oriented class structures |
| **State** | State machine diagrams |
| **Entity Relationship** | Database schemas |
| **Gantt** | Project timelines |
| **Kanban** | Task boards |
| **Pie** | Proportional data |
| **Git Graph** | Branch visualization |
| **Mindmap** | Hierarchical ideas |

---

## Flowchart

Flowcharts are the most common Mermaid diagram type. Use them to visualize processes, algorithms, and decision flows.

```mermaid 
graph TD 
    A[Start] --> B{Decision} 
    B -->|Yes| C[Option A] 
    B -->|No| D[Option B] 
    C --> E[Result A] 
    D --> F[Result B] 
    E --> G[End] 
    F --> G
```

### Direction Options

- `TD` or `TB` — Top to bottom
- `BT` — Bottom to top
- `LR` — Left to right
- `RL` — Right to left

### Node Shapes

```mermaid
graph LR 
    A[Rectangle] --> B(Rounded) 
    B --> C([Stadium]) 
    C --> D[[Subroutine]] 
    D --> E[(Database)] 
    E --> F((Circle)) 
    F --> G{Diamond} 
    G --> H{{Hexagon}}
```

---

## Sequence Diagrams

Visualize interactions between participants over time.

```mermaid 
sequenceDiagram 
    participant Alice 
    participant Bob 
    participant John

    Alice->>John: Hello John, how are you?
    loop HealthCheck
        John->>John: Fight against hypochondria
    end
    Note right of John: Rational thoughts <br/>prevail!
    John-->>Alice: Great!
    John->>Bob: How about you?
    Bob-->>John: Jolly good!
```

### Message Types

| Syntax | Description |
|--------|-------------|
| `->` | Solid line without arrow |
| `-->` | Dotted line without arrow |
| `->>` | Solid line with arrow |
| `-->>` | Dotted line with arrow |
| `-x` | Solid line with cross |
| `--x` | Dotted line with cross |

---

## Class Diagrams

Document object-oriented designs and relationships.



```mermaid 
classDiagram 
    Class01 <|-- AveryLongClass : Cool 
    Class03 _-- Class04 
    Class05 o-- Class06 
    Class07 .. Class08 
    Class09 --> C2 : Where am I? 
    Class09 --_ C3 
    Class09 --|> Class07 
    Class07 : equals() 
    Class07 : Object[] elementData 
    Class01 : size() 
    Class01 : int chimp 
    Class01 : int gorilla 
    Class08 <--> C2: Cool label
```

### Relationship Types

| Symbol | Description |
|--------|-------------|
| `<\|--` | Inheritance |
| `*--` | Composition |
| `o--` | Aggregation |
| `-->` | Association |
| `..>` | Dependency |
| `..\|>` | Realization |

---

## Gantt Charts

Visualize project timelines and task dependencies.

```mermaid 
gantt 
    title Project Development Timeline 
    dateFormat YYYY-MM-DD
    section Planning
        Requirements     :a1, 2024-01-01, 14d
        Design           :a2, after a1, 10d

    section Development
        Backend API      :b1, after a2, 21d
        Frontend UI      :b2, after a2, 18d
        Integration      :b3, after b1, 7d

    section Testing
        Unit Tests       :c1, after b2, 7d
        QA Testing       :c2, after b3, 10d

    section Deployment
        Staging          :d1, after c2, 3d
        Production       :d2, after d1, 2d
```

---

## Kanban Boards

Visualize workflow and task status.

```mermaid
---
config:
  kanban:
    ticketBaseUrl: 'https://mermaidchart.atlassian.net/browse/#TICKET#'
---
kanban
Todo
[Create Documentation]
docs[Create Blog about the new diagram]
[In progress]
id6[Create renderer so that it works in all cases. We also add some extra text here for testing purposes. And some more just for the extra flare.]
id9[Ready for deploy]
id8[Design grammar]@{ assigned: 'knsv' }
id10[Ready for test]
id4[Create parsing tests]@{ ticket: MC-2038, assigned: 'K.Sveidqvist', priority: 'High' }
id66[last item]@{ priority: 'Very Low', assigned: 'knsv' }
id11[Done]
id5[define getData]
id2[Title of diagram is more than 100 chars when user duplicates diagram with 100 char]@{ ticket: MC-2036, priority: 'Very High'}
id3[Update DB function]@{ ticket: MC-2037, assigned: knsv, priority: 'High' }

id12[Can't reproduce]
id3[Weird flickering in Firefox]

```

---

## State Diagrams

Model state machines and transitions.

```mermaid
stateDiagram-v2
    [*] --> Active

    state Active {
        [*] --> NumLockOff
        NumLockOff --> NumLockOn : EvNumLockPressed
        NumLockOn --> NumLockOff : EvNumLockPressed
        --
        [*] --> CapsLockOff
        CapsLockOff --> CapsLockOn : EvCapsLockPressed
        CapsLockOn --> CapsLockOff : EvCapsLockPressed
        --
        [*] --> ScrollLockOff
        ScrollLockOff --> ScrollLockOn : EvScrollLockPressed
        ScrollLockOn --> ScrollLockOff : EvScrollLockPressed
    }

```

### Interactive Features

    DocOps renders Mermaid diagrams with built-in interactive controls:

- **VIEW** — Open diagram in a modal for closer inspection
- **SVG** — Copy the SVG source code to clipboard
- **PNG** — Export as PNG image

<div style="background: #ecfdf5; border-left: 4px solid #10b981; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
<p style="margin: 0; color: #047857; font-weight: 600;">💡 Pro Tip</p>
<p style="margin: 8px 0 0 0; color: #065f46;">Mermaid diagrams are rendered client-side, so they adapt to your page's theme and can be interacted with directly in the browser.</p>
</div>

---

## Best Practices

### Keep It Simple

- Break complex diagrams into smaller, focused diagrams
- Use clear, descriptive labels
- Limit the number of nodes for readability

### Consistent Styling

- Use consistent node shapes for similar concepts
- Apply styling with classDef for uniform appearance
- Choose appropriate direction for your content flow

### Documentation Integration

- Use diagrams to supplement text explanations
- Place diagrams near the content they illustrate
- Include alt text descriptions for accessibility

---

## Resources

- [Mermaid Official Documentation](https://mermaid.js.org/intro/)
- [Live Editor](https://mermaid.live/)
- [Mermaid GitHub Repository](https://github.com/mermaid-js/mermaid)
