# Badges and Shields

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/BadgeShieldIcon.svg" alt="Badge and Shield Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #4f46e5; font-size: 32px;">DocOps Badges & Shields</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Visually represent status, metrics, and achievements with customizable badge styles</p>
    </div>
  </div>
</div>

[TOC]

## What are Badges and Shields?

DocOps badges and shields are visual indicators that provide quick insights into the status, metrics, and achievements of your projects. They serve multiple purposes:

- **Status Indicators** - Show build status, test coverage, version numbers
- **Metrics Display** - Visualize downloads, stars, contributors, and other KPIs
- **Achievement Markers** - Highlight certifications, awards, and milestones
- **Visual Communication** - Convey information at a glance with color-coded badges

<div style="background: #f0fdf4; border-left: 4px solid #10b981; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #065f46; font-weight: 600;">✨ Multiple Styles Available</p>
  <p style="margin: 8px 0 0 0; color: #047857;">Choose from Default, Minimal, Brutalist, Glassmorphic, and Neon styles to match your design aesthetic.</p>
</div>

## Default Style

The classic badge style perfect for project metrics and status indicators.

[docops:badge]
Downloads|1.2M||#e91e63|#6610f2
Stars|12.5k||#ffc107|#ffc107
Forks|2.3k||#607d8b|#0366d6
Contributors|45||#795548|#6610f2
[/docops]

## Minimal Style

Clean, lightweight badges with subtle styling.

[docops:badge]
type=MINIMAL
theme=auto
spacing=10
---
Made With|Kotlin||#06133b|#6fc441|<kotlin>|#fcfcfc
[/docops]

## Brutalist Style

Bold, high-contrast badges with sharp edges for maximum impact.

[docops:badge]
type=BRUTALIST
theme=auto
spacing=10
---
Made With|Kotlin||#06133b|#6fc441|<kotlin>|#fcfcfc
[/docops]

## Glassmorphic Style

Modern, frosted-glass effect with translucent backgrounds.

[docops:badge]
type=glassmorphic
theme=auto
spacing=12
direction=horizontal
perRow=3
---
Made With|Kotlin||#06133b|#6fc441|<kotlin>|#fcfcfc
[/docops]

## Neon Style

Eye-catching badges with glowing neon effects.

[docops:badge]
type=neon
theme=auto
spacing=10
---
Made With|Kotlin||#06133b|#6fc441|<kotlin>|#fcfcfc
[/docops]

---

## Badge Anatomy

Each badge consists of:

1. **Label** - The descriptor (e.g., "Downloads", "Version", "Status")
2. **Message** - The value or status (e.g., "1.2M", "v2.0", "Passing")
3. **Colors** - Label color and message color for visual distinction
4. **Optional Icon** - Logo or icon for brand recognition
5. **Link** - Optional URL for clickable badges

## Common Use Cases

### Project Metrics
Display key performance indicators for your repository:
- Download counts
- GitHub stars and forks
- Contributor numbers
- Code coverage percentage

### Build Status
Show the health of your CI/CD pipeline:
- Build passing/failing status
- Test results
- Deployment status
- Quality gate results

### Version Information
Communicate current versions:
- Application version
- API version
- Dependency versions
- Release channels (stable, beta, alpha)

### Certifications & Compliance
Highlight important credentials:
- Security certifications
- Compliance standards (SOC 2, GDPR, HIPAA)
- Quality badges
- Award recognition

<div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #92400e; font-weight: 600;">💡 Pro Tip</p>
  <p style="margin: 8px 0 0 0; color: #b45309;">Use consistent color schemes across badges to create visual harmony. Group related badges together for better scanability.</p>
</div>

## Customization Options

### Colors
- **Label Color** - Background color for the left side
- **Message Color** - Background color for the right side
- **Font Color** - Text color (defaults to white for readability)

### Layout
- **Direction** - Horizontal or vertical arrangement
- **Spacing** - Gap between multiple badges
- **Per Row** - Number of badges per row in grid layouts

### Theming
- **Auto Theme** - Adapts to light/dark mode
- **Custom Themes** - Define your own color palettes
- **Style Variants** - Choose from multiple visual treatments

---

<div align="center" style="margin-top: 48px; padding: 24px; background: #fafbfc; border-radius: 8px;">
  <p style="color: #64748b; margin: 0;">Ready to add badges to your documentation?</p>
  <p style="color: #4f46e5; font-weight: 600; margin: 8px 0 0 0;">Start creating custom badges with the DocOps badge extension</p>
</div>