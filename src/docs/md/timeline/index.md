# DocOps Timeline Visualizer

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/TimelineIcon.svg" alt="Timeline Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #0f766e; font-size: 32px;">DocOps Timeline</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Map milestones and history into clean, easy-to-scan timelines</p>
    </div>
  </div>
</div>

[TOC]

## What is DocOps Timeline?

DocOps Timeline turns dated milestones into structured visuals. Use it for product history, release cycles, historical timelines, or multi-phase program updates.

### Key Features

- **Chronological clarity** - Show milestones in order with consistent formatting
- **Vertical and horizontal layouts** - Choose the format that fits your content
- **Rich descriptions** - Include multi-line details and links
- **Readable at a glance** - Ideal for roadmap or historical summaries

---

## Default Look

[docops:timeline]
date: Q1 2024
text: Release DocOps Extension Server v2.0
• Enhanced timeline visualization
• Improved performance
• New documentation templates

date: Q2 2024
text: Launch DocOps Cloud Service
• Browserbased editing
• Collaborative documentation
• Automatic versioning

date: Q3 2024
text: Mobile App Release
• View documentation onthego
• Offline access
• Push notifications for updates

date: Q4 2024
text: Enterprise Integration Suite
• LDAP/Active Directory support
• Advanced access controls
• Custom branding options
• Analytics dashboard

date: Q1 2025
text: AIPowered Documentation Assistant
• Automated content suggestions
• Quality and consistency checks
• Smart search capabilities

[/docops]

---

## Horizontal Layout

[docops:timeline]
type=H
 ---
colorIdx=3
width=600
---
date: 1660 - 1798
text: The Enlightenment/Neoclassical Period
Literature focused on reason, logic, and scientific thought. Major writers include [[https://en.wikipedia.org/wiki/Alexander_Pope Alexander Pope]] and [[https://en.wikipedia.org/wiki/Jonathan_Swift Jonathan Swift]].

date: 1798 - 1832
text: Romanticism
Emphasized emotion, individualism, and the glorification of nature. Key figures include [[https://en.wikipedia.org/wiki/William_Wordsworth William Wordsworth]] and [[https://en.wikipedia.org/wiki/Lord_Byron Lord Byron]].

date: 1837 - 1901
text: Victorian Era
Literature reflected the social, economic, and cultural changes of the Industrial Revolution. Notable authors include [[https://en.wikipedia.org/wiki/Charles_Dickens Charles Dickens]] and [[https://en.wikipedia.org/wiki/George_Eliot George Eliot]].

date: 1914 - 1945
text: Modernism
Characterized by a break with traditional forms and a focus on experimentation. Important writers include [[https://en.wikipedia.org/wiki/James_Joyce James Joyce]] and [[https://en.wikipedia.org/wiki/Virginia_Woolf Virginia Woolf]].

date: 1945 - present
text: Postmodernism
Challenges the distinction between high and low culture and emphasizes fragmentation and skepticism. Key authors include [[https://en.wikipedia.org/wiki/Thomas_Pynchon Thomas Pynchon]] and [[https://en.wikipedia.org/wiki/Toni_Morrison Toni Morrison]].
[/docops]

---

## Format Options

### Timeline Structure

Use repeated `date` and `text` blocks. Bullet lines can be added under each text block.

```text
[docops:timeline]
date: Q1 2024
text: Release v2.0
• Highlight 1
• Highlight 2
[/docops]
```

### Layout Options

- **type=H** - Horizontal layout
- **colorIdx** - Color palette index
- **width** - Canvas width in pixels

---

## Best Practices

- **Keep dates consistent** - Use quarters, months, or exact dates
- **Limit bullet depth** - Two to four lines per milestone
- **Balance detail** - Move long narratives into linked docs
- **Use horizontal sparingly** - Best for short, high-level timelines

<div style="background: #f0fdfa; border-left: 4px solid #14b8a6; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #0f766e; font-weight: 600;">🧭 Timeline Tip</p>
  <p style="margin: 8px 0 0 0; color: #475569;">Keep your timeline in one visual screen for easy scanning. If it grows too long, split by year or phase.</p>
</div>

---

## Common Use Cases

- **Product release history** - Track shipped milestones
- **Roadmap summaries** - Communicate upcoming phases
- **Historical overviews** - Document organizational evolution
- **Program reporting** - Show major deliverables over time
