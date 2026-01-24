# DocOps Word Cloud Visualizer

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #be123c 0%, #f97316 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/WordcloudIcon.svg" alt="Word Cloud Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #be123c; font-size: 32px;">DocOps Word Cloud</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Turn keyword frequency into engaging visual summaries</p>
    </div>
  </div>
</div>

[TOC]

## What is DocOps Word Cloud?

DocOps Word Cloud transforms weighted keyword lists into an eye-catching visualization. Use it to summarize themes, survey responses, or product feedback.

### Key Features

- **Weighted words** - Size reflects importance or frequency
- **Color control** - Customize colors per term
- **Flexible sizing** - Adjust canvas and font ranges
- **Great for summaries** - Quick visual read for large datasets

---

## Default Look

[docops:wordcloud]
title= Software Development Concepts
width= 800
height= 600
minFontSize= 14
maxFontSize= 50
---
Agile | 90 | #ff7979
CI/CD | 85 | #7ed6df
Testing | 80 | #f6e58d
Microservices | 75 | #badc58
Containers | 70 | #ffbe76
Serverless | 65 | #ff9ff3
API | 60 | #f9ca24
Refactoring | 55 | #686de0
Clean Code | 50 | #be2edd
Design Patterns | 45 | #eb4d4b
[/docops]

---

## Survey Themes Example

[docops:wordcloud]
title= Customer Feedback Themes
width= 760
height= 520
minFontSize= 16
maxFontSize= 48
---
Ease of Use | 92 | #f97316
Performance | 84 | #fb7185
Support | 76 | #fdba74
Reliability | 70 | #fecaca
Integrations | 64 | #fda4af
Onboarding | 58 | #fed7aa
Pricing | 54 | #fb923c
Documentation | 48 | #f97316
[/docops]

---

## Format Options

### Word Cloud Structure

Provide `title`, sizing options, then `word | weight | color` rows:

```text
[docops:wordcloud]
title= Theme Summary
width= 800
height= 600
minFontSize= 14
maxFontSize= 50
---
Keyword | 90 | #ff7979
[/docops]
```

### Options

- **width** - Canvas width in pixels
- **height** - Canvas height in pixels
- **minFontSize** - Smallest word size
- **maxFontSize** - Largest word size

---

## Best Practices

- **Normalize weights** - Keep weights in a consistent range
- **Limit word count** - 15-30 words reads best
- **Use contrast** - Ensure colors are readable
- **Group themes** - Cluster related terms with similar colors

<div style="background: #fff1f2; border-left: 4px solid #f97316; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #be123c; font-weight: 600;">💬 Word Cloud Tip</p>
  <p style="margin: 8px 0 0 0; color: #475569;">If a word cloud feels noisy, reduce low-weight terms and increase spacing with fewer words.</p>
</div>

---

## Common Use Cases

- **Survey summaries** - Highlight common responses
- **Product feedback** - Surface top themes
- **Meeting notes** - Summarize discussion topics
- **Market research** - Visualize keywords and trends
