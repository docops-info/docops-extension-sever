# DocOps Scorecard

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #15803d 0%, #22c55e 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/ScorecardIcon.svg" alt="Scorecard Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #15803d; font-size: 32px;">DocOps Scorecard</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Summarize before-and-after changes with structured scorecards</p>
    </div>
  </div>
</div>

[TOC]

## What is DocOps Scorecard?

DocOps Scorecard compares before-and-after states in a single visual. It is ideal for release summaries, migrations, incident outcomes, and program improvements.

### Key Features

- **Before/after structure** - Show transformation clearly
- **Grouped sections** - Organize changes by category
- **Readable deltas** - Highlight gains and removals quickly
- **Narrative-friendly** - Combine qualitative notes with outcomes

---

## Default Look

[docops:scorecard]
title=Software Release v2.4.0 - Feature & Bug Summary
subtitle=Migration from Legacy System to Modern Architecture
---

[before]
title=BEFORE v2.4.0
---
[before.items]
=== Feature Status
Dark Mode Theme | Missing feature affecting user experience
Multi-language Support | Not available, limiting global reach
Advanced Search Filters | Basic search only, slow performance
Export to PDF | Feature not implemented
Two-Factor Authentication | Security vulnerability exists
API Rate Limiting | No protection against abuse
=== Known Issues
Login timeout issues | Users frequently logged out
Memory leaks in dashboard | System becomes slow over time
File upload corruption | Files sometimes corrupted during upload
Mobile UI overlapping | Interface broken on mobile devices
Database connection drops | Intermittent connection failures
Email notifications failing | Users not receiving important updates
Report generation errors | Reports fail to generate properly
---

[after]
title=AFTER v2.4.0
---
[after.items]
=== New Features Added
Dark Mode Theme | Implemented with user preference saving
Multi-language Support | Added 12 languages with automatic detection
Advanced Search Filters | Fast indexing with multiple filter options
Export to PDF | High-quality PDF export with custom templates
Two-Factor Authentication | TOTP and SMS-based 2FA implemented
API Rate Limiting | Intelligent rate limiting with user tiers
=== Bugs Resolved
Login timeout issues | Session management completely rewritten
Memory leaks in dashboard | React components optimized, memory usage -67%
File upload corruption | New chunked upload system with integrity checks
Mobile UI overlapping | Responsive design overhaul completed
Database connection drops | Connection pooling and retry logic implemented
Email notifications failing | New email service with 99.9% delivery rate
Report generation errors | Async report generation with progress tracking
[/docops]

---

## Program Improvement Example

[docops:scorecard]
title=Q3 Platform Reliability Improvements
subtitle=Stability work across infrastructure and tooling
---

[before]
title=BEFORE Q3
---
[before.items]
=== Stability Risks
Service timeouts | Frequent during peak load
On-call alerts | High noise and false positives
Deployment rollback | Manual and slow
=== Operational Debt
Runbooks | Outdated or missing
Observability | Limited tracing coverage
---

[after]
title=AFTER Q3
---
[after.items]
=== Improvements Delivered
Service timeouts | Reduced by 68%
On-call alerts | 42% fewer pages
Deployment rollback | Automated in 2 minutes
=== Operational Upgrades
Runbooks | Updated and centralized
Observability | Tracing coverage at 85%
[/docops]

---

## Format Options

### Scorecard Structure

Define `title`, `subtitle`, then `before` and `after` blocks with grouped items:

``````terminaloutput
[docops:scorecard]
title=Release Summary
subtitle=Highlights
---

[before]
title=BEFORE
---
[before.items]
=== Category
Item | Description
---

[after]
title=AFTER
---
[after.items]
=== Category
Item | Description
[/docops]
``````

### Sections

- **[before]** - State prior to change
- **[after]** - State after change
- **=== Section** - Group related items

---

## Best Practices

- **Keep it balanced** - Mirror categories in before/after
- **Use measurable outcomes** - Quantify improvements where possible
- **Limit sections** - Two to four sections per column
- **Stay concise** - One line per item keeps it readable

<div style="background: #f0fdf4; border-left: 4px solid #22c55e; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #166534; font-weight: 600;">✅ Scorecard Tip</p>
  <p style="margin: 8px 0 0 0; color: #475569;">Mirror the language between before and after to make improvements obvious.</p>
</div>

---

## Common Use Cases

- **Release summaries** - Show what improved or changed
- **Migration outcomes** - Compare legacy vs. modern systems
- **Incident reviews** - Capture state before and after remediation
- **Program reporting** - Demonstrate impact for stakeholders
