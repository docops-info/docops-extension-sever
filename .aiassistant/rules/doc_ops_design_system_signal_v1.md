---
apply: always
---

# DocOps Design System — Signal (v1.0)

**Brand attributes:** Professional · Trust · Corporate America  
**Design intent (Apple HIG aligned):** Clarity, hierarchy, restraint, and accessibility.  
**Positioning:** “Decision-ready documentation and metrics—calm, confident, audit-friendly.”

---

## Table of Contents

1. [Foundations](#foundations)  
   1.1 [Color](#11-color)  
   1.2 [Typography](#12-typography)  
   1.3 [Layout Grid](#13-layout-grid)  
   1.4 [Spacing](#14-spacing)  
2. [Components](#components)  
   2.1 [Navigation](#21-navigation)  
   2.2 [Inputs](#22-inputs)  
   2.3 [Feedback](#23-feedback)  
   2.4 [Data Display](#24-data-display)  
   2.5 [Media](#25-media)  
3. [Patterns](#patterns)  
   3.1 [Page Templates](#31-page-templates)  
   3.2 [User Flows](#32-user-flows)  
   3.3 [Feedback Patterns](#33-feedback-patterns)  
4. [Tokens](#tokens)  
5. [Documentation](#documentation)  
   5.1 [Principles](#51-principles)  
   5.2 [Dos and Donts](#52-dos-and-donts)  
   5.3 [Developer Implementation Guide](#53-developer-implementation-guide)

---

## Foundations

### 1.1 Color

#### Primary palette (6)

| Token | Name | Hex | RGB | HSL | Primary usage |
|---|---|---:|---|---|---|
| `brand.ink` | Ink Navy | `#0B1220` | 11,18,32 | 220°, 49%, 8% | Navigation, headings, authority surfaces |
| `brand.cloud` | Cloud | `#F6F8FB` | 246,248,251 | 216°, 33%, 97% | App background (light) |
| `brand.slate` | Slate | `#2A3447` | 42,52,71 | 219°, 26%, 22% | Body text on light, secondary surfaces |
| `brand.steel` | Steel | `#8A96AD` | 138,150,173 | 219°, 18%, 61% | Secondary text, captions, dividers |
| `brand.cyan` | Signal Cyan (Accent) | `#00B7D6` | 0,183,214 | 189°, 100%, 42% | Primary action, focus, selection |
| `brand.gold` | Audit Gold | `#C9A227` | 201,162,39 | 46°, 68%, 47% | Review/audit attention (not error) |

#### Semantic colors

| Semantic | Token | Hex | Use it for |
|---|---|---:|---|
| Success | `semantic.success` | `#16A34A` | Completed, confirmed, “passed checks” |
| Warning | `semantic.warning` | `#D97706` | Risk, caution, non-blocking issues |
| Error | `semantic.error` | `#DC2626` | Blocking failure, destructive actions |
| Info | `semantic.info` | `#2563EB` | Neutral system notice, “learn more” |

#### Dark mode equivalents (roles)

Dark mode keeps the same hierarchy while inverting canvas/surfaces:

- `canvas.dark`: `#070B14`
- `surface1.dark`: `#0B1220`
- `surface2.dark`: `#111A2D`
- `textPrimary.dark`: `#F1F5FF`
- `textSecondary.dark`: `#B7C2D9`
- `textTertiary.dark`: `#8EA0C4`
- `accent.dark`: `#28D9F5`

**Contrast targets**
- Body text: aim for **≥ 7:1** where possible, never below **4.5:1**
- UI icons/controls: **≥ 3:1** against their background
- Focus ring: must be visible against both `surface1` and `surface2`

#### Color usage rules (meaning → behavior)
1. **Ink is authority:** use `brand.ink` for navigation and “system truth.”
2. **Cyan is action:** reserve `brand.cyan` for *primary actions*, selection, and focus.
3. **Gold is governance:** use `brand.gold` for “Review / Audit / Needs attention.”
4. **Semantic colors are literal:** do not decorate with error red or success green.
5. **Data-viz:** use accent for “current/selected,” neutrals for context.
6. **Secondary text limits:** `brand.steel` is not for critical info or small type on light.

---

### 1.2 Typography

#### Font families
- **Primary sans:** IBM Plex Sans
- **Monospace:** IBM Plex Mono (values, IDs, code, tables)

#### Role mapping (DocOps roles → suggested weight)
- Display: 600
- Headline: 600
- Title: 500
- Body: 400
- Callout: 500
- Subheadline: 400
- Footnote: 400
- Caption: 400
- Utility emphasis: 700 (sparingly)

#### Type scale (exact sizes)

**Desktop (≥ 1024px)**

| Role | Size | Line-height | Letter-spacing |
|---|---:|---:|---:|
| Display | 40px | 48px | -0.6px |
| Headline | 28px | 34px | -0.3px |
| Title | 22px | 28px | -0.2px |
| Body | 16px | 24px | 0px |
| Callout | 15px | 22px | 0px |
| Subheadline | 14px | 20px | 0.1px |
| Footnote | 12px | 18px | 0.2px |
| Caption | 11px | 16px | 0.2px |

**Tablet (768–1023px)**

| Role | Size | Line-height | Letter-spacing |
|---|---:|---:|---:|
| Display | 36px | 44px | -0.5px |
| Headline | 26px | 32px | -0.2px |
| Title | 20px | 26px | -0.1px |
| Body | 16px | 24px | 0px |
| Callout | 15px | 22px | 0px |
| Subheadline | 14px | 20px | 0.1px |
| Footnote | 12px | 18px | 0.2px |
| Caption | 11px | 16px | 0.2px |

**Mobile (≤ 767px)**

| Role | Size | Line-height | Letter-spacing |
|---|---:|---:|---:|
| Display | 32px | 40px | -0.4px |
| Headline | 24px | 30px | -0.2px |
| Title | 19px | 24px | -0.1px |
| Body | 16px | 24px | 0px |
| Callout | 15px | 22px | 0px |
| Subheadline | 14px | 20px | 0.1px |
| Footnote | 12px | 18px | 0.2px |
| Caption | 11px | 16px | 0.2px |

#### Pairing strategy
- Plex Sans for interface + prose (trustworthy, neutral, readable)
- Plex Mono for numbers and “operational truth” (scanability)

#### Accessibility minimums
- Body text: **16px** minimum on mobile for primary content
- Caption: **11px** only for non-critical metadata with sufficient contrast
- Touch target minimum: **44×44** (CSS px equivalent per platform density rules)

---

### 1.3 Layout Grid

#### 12-column responsive grid

**Desktop**
- Artboard: 1440px
- Container max width: 1200px
- Outer margins: 120px (responsive down to 24px)
- Gutter: 24px

**Tablet**
- Artboard: 768px
- Container: 720px
- Outer margins: 24px
- Gutter: 20px

**Mobile**
- Artboard: 375px
- Container: 343px
- Outer margins: 16px
- Gutter: 16px  
  *(Use 4–6 columns for most mobile layouts; keep 12 for internal alignment.)*

#### Breakpoints
- `sm`: 0–767
- `md`: 768–1023
- `lg`: 1024–1439
- `xl`: 1440+

#### Safe areas (notched devices)
- Place sticky/tab bars **inside** safe areas
- Keep critical controls at least **16px** away from screen edges on mobile

---

### 1.4 Spacing

Base unit: **8px**  
Scale: **4, 8, 12, 16, 24, 32, 48, 64, 96, 128**

Usage:
- 4: compact separators, tight icon gaps
- 8: control internals, chips, small stacks
- 12: dense card/table padding (compact)
- 16: default card padding, form rhythm
- 24: section spacing, modal padding
- 32: major section breaks
- 48: empty states, hero blocks
- 64/96/128: landing hero and “statement” layouts only

---

## Components

**Global component standards**
- **Corner radius:** 8 / 12 / 16 (small/medium/large), pills = 999
- **Default border:** 1px; **focus/selected:** 2px
- **Shadows:** subtle, used to indicate elevation not decoration
- **Focus ring:** 2px accent + soft outer glow; visible in light/dark
- **Keyboard:** all interactive components must be reachable and operable

> Component templates below specify anatomy, states, usage, accessibility, and code-ready specs.

### 2.1 Navigation

#### Header / Top Bar
- **Anatomy:** container, brand, nav items, global search, actions, user menu
- **States:** default, scrolled (shadow), item hover/active, disabled
- **Use:** persistent global navigation + search
- **Avoid:** stacking multiple sticky bars (merge actions)
- **A11y:** `role="banner"`, primary nav `role="navigation"`, skip link
- **Specs:** height 64; padding 16–24; divider 1px; shadow on scroll

#### Sidebar (Collapsible)
- **Anatomy:** rail, section headers, items, badges, collapse control
- **States:** expanded/collapsed, hover, selected, disabled, focus
- **Use:** admin/enterprise IA with depth
- **Avoid:** tiny apps with few links (use tabs)
- **A11y:** `aria-current="page"`, roving tabindex pattern
- **Specs:** width 280 expanded / 72 collapsed; item height 40

#### Tab Bar (Top tabs)
- **Anatomy:** tablist, tabs, active indicator, badges
- **States:** hover, selected, disabled, focus
- **Use:** switch views within a page
- **Avoid:** step flows (use Stepper pattern)
- **A11y:** `role="tablist"`, `role="tab"`, `aria-selected`
- **Specs:** indicator height 2; min tab height 40

#### Breadcrumbs
- **Anatomy:** list, separators, current
- **States:** hover, overflow menu
- **Use:** deep hierarchy navigation
- **A11y:** `aria-label="Breadcrumb"`, `aria-current="page"`
- **Specs:** Subheadline; separators low opacity

---

### 2.2 Inputs

#### Buttons (6 variants)
Variants: **Primary, Secondary, Tertiary (ghost), Destructive, Icon, Link**
- **Anatomy:** label, optional icon, spinner, focus ring
- **States:** default, hover, active, disabled, loading, focus-visible
- **Usage:** one Primary per region; Destructive only for irreversible actions
- **A11y:** loading: `aria-busy="true"` and disabled; icon-only needs `aria-label`
- **Specs:** height 40/32/48; paddingX 16; radius 12; icon gap 8

#### Text Field
- **Anatomy:** label, input, helper, error, optional leading/trailing icons
- **States:** default, hover, focus, filled, disabled, error
- **Use:** short text entry
- **A11y:** label association; error via `aria-describedby`
- **Specs:** height 44; padding 12; border 1px (2px on focus)

#### Text Area
- **Anatomy:** label, textarea, helper/error, optional char count
- **States:** same as Text Field
- **Use:** multi-line content
- **A11y:** preserve resize; char count politely announced if dynamic
- **Specs:** min-height 96; padding 12

#### Search Field (with suggestions)
- **Anatomy:** input, search icon, clear, suggestions panel
- **States:** typing, loading, empty, no-results
- **A11y:** combobox pattern: `aria-expanded`, `aria-controls`
- **Specs:** suggestions row height 40; max-height 320

#### Dropdown / Select
- **Anatomy:** trigger, value, chevron, menu, options, groups
- **States:** open/close, hover, selected, disabled, error
- **A11y:** listbox pattern + keyboard navigation
- **Specs:** radius 12; shadow medium; option height 40

#### Multi-select (chips)
- **Anatomy:** chips, input, menu, remove buttons
- **States:** add/remove, overflow, disabled
- **A11y:** chip remove: “Remove {tag}”
- **Specs:** chip height 28; gap 8

#### Checkbox
- **Anatomy:** box, mark, label
- **States:** unchecked, checked, indeterminate, disabled, focus
- **A11y:** native input; indeterminate announced
- **Specs:** 18×18; radius 6

#### Radio Button
- **Anatomy:** circle, dot, label
- **States:** selected, disabled, focus
- **A11y:** radiogroup semantics
- **Specs:** 18×18; dot 8

#### Toggle (Switch)
- **Anatomy:** track, thumb, label
- **States:** on/off, disabled, focus, loading
- **A11y:** `role="switch"`, `aria-checked`
- **Specs:** 44×28; thumb 22; pill radius

#### Slider
- **Anatomy:** track, fill, thumb, value label (opt)
- **States:** hover, drag active, disabled, focus
- **A11y:** `aria-valuemin/max/now`
- **Specs:** thumb 18; control height 32

#### Stepper
- **Anatomy:** value, minus, plus
- **States:** min/max disabled, hold-to-repeat
- **A11y:** buttons labeled; value announced
- **Specs:** buttons 40×40; radius 12

#### Date Picker (field + popover)
- **Anatomy:** field, calendar popover, month nav, day grid
- **States:** selected, range, disabled days
- **A11y:** keyboard day navigation; month announced
- **Specs:** popover width 320; day cell 40

#### File Upload (dropzone)
- **Anatomy:** drop area, instructions, browse, file list, progress
- **States:** drag-over, uploading, success, error
- **A11y:** browse button works without drag-drop
- **Specs:** min-height 120; dashed border; progress row 32

---

### 2.3 Feedback

#### Alert (Inline)
Variants: info/success/warning/error
- **Anatomy:** icon, title, message, action, close
- **States:** visible/dismissed
- **Use:** persistent feedback on page
- **A11y:** errors use `role="alert"`, others `role="status"`
- **Specs:** padding 12×16; radius 12; border 1px semantic color

#### Toast
- **Anatomy:** message, optional action, close
- **States:** entering, visible, exiting
- **Use:** ephemeral confirmations (“Saved”)
- **A11y:** `role="status"`; do not steal focus
- **Specs:** max width 360; mobile bottom-center

#### Modal / Dialog
- **Anatomy:** overlay, panel, header, body, footer actions
- **States:** open, closing, loading, destructive confirm
- **A11y:** `role="dialog"`, `aria-modal="true"`, focus trap, Esc closes
- **Specs:** width 560 default; padding 24; radius 16

#### Popover
- **Anatomy:** anchor, surface, optional arrow, content
- **A11y:** Esc closes; focus managed
- **Specs:** radius 12; padding 12–16; shadow medium

#### Tooltip
- **Anatomy:** label + arrow
- **A11y:** appears on focus; `aria-describedby`
- **Specs:** max width 240; padding 8×10; radius 10

#### Progress (Linear)
- **States:** determinate/indeterminate
- **A11y:** `role="progressbar"`
- **Specs:** height 6; pill radius

#### Progress (Circular)
- **States:** spinning, optional success tick
- **A11y:** announce loading state once
- **Specs:** 20/28/40 sizes; stroke 3

#### Skeleton Screen
- **Anatomy:** blocks/lines matching final layout
- **A11y:** reduced motion support; single loading status
- **Specs:** shimmer duration ~1.4s; subtle contrast

---

### 2.4 Data Display

#### Card
Variants: standard/glass/elevated/outline/interactive
- **Anatomy:** header, title, actions, body, footer
- **States:** hover (interactive), selected, loading
- **A11y:** if clickable, a single interactive surface (not nested links)
- **Specs:** padding 16/24; radius 16; border 1px; hover lift -2px

#### Metric Tile (KPI)
- **Anatomy:** label, value (mono), sublabel, delta badge
- **States:** neutral/up/down, selected
- **A11y:** reading order label → value → sublabel
- **Specs:** spacing 8; delta pill height 22

#### Table
Variants: standard/dense/zebra/sticky header
- **Anatomy:** header, rows, cells, sort, selection
- **States:** hover row, selected, empty, loading
- **A11y:** semantic `<table>`; sorting via `aria-sort`
- **Specs:** row height 44 (std) / 36 (dense); padding 12×16

#### Data List (two-line)
- **Anatomy:** leading icon/avatar, title, subtitle, trailing meta
- **A11y:** correct link/button semantics for rows
- **Specs:** min height 56; dividers 1px

#### Badge / Pill
Variants: neutral/accent/success/warning/error/gold-review
- **A11y:** include text/icon; don’t rely on color alone
- **Specs:** height 24; padding 0×10; pill radius

#### Stat Bar (inline)
- **Anatomy:** label, bar, numeric value
- **A11y:** bar is decorative; numeric is authoritative
- **Specs:** bar height 8; pill radius

#### Charts (line/bar)
- **Anatomy:** plot, axes, ticks, series, tooltip
- **A11y:** provide textual summary and optional data table export
- **Specs:** axis text 11–12; subtle gridlines

#### Pagination
- **A11y:** label controls, announce current page
- **Specs:** height 32/40; gap 8

#### Empty State Panel
- **Anatomy:** icon/illustration, title, guidance, primary action
- **A11y:** action-oriented copy; no blame language
- **Specs:** padding 48; max width 520

---

### 2.5 Media

#### Avatar
Variants: image/initials/icon
- **A11y:** meaningful images get alt; decorative use empty alt
- **Specs:** 24/32/40; circle

#### Image Container
- **States:** loading, error fallback
- **A11y:** alt text; accessible zoom controls if present
- **Specs:** radius 16; aspect-ratio presets

#### Video Player (embedded)
- **A11y:** captions; keyboard controls
- **Specs:** radius 16; 44px control targets

---

## Patterns

### 3.1 Page Templates

#### Landing
- Hero: crisp promise + one primary CTA
- Proof: sample dashboards + security/compliance signals
- Avoid: noisy gradients; keep depth subtle and intentional

#### Dashboard
- Filters row + date range
- KPI tiles (4–6), 1–2 charts, results table
- Optional activity rail: alerts + recent exports

#### Settings
- Section nav + card-based forms
- Inline validation; toast confirmation

#### Profile
- Identity, roles, access controls
- Token management (mask secrets, revoke flow)

#### Checkout (enterprise add-ons)
- Stepper: Plan → Billing → Review
- Sticky summary; cancellation terms visible

---

### 3.2 User Flows

#### Onboarding
Welcome → Connect source → Choose templates → First export
- Progressive disclosure
- Confirm each step (inline success + primary next)

#### Authentication
- SSO-first
- Helpful error copy with one next step

#### Search
- Suggestions grouped by type (Docs / Dashboards / Exports)
- No results: show tips + filter reset

#### Filtering
- Chips show active filters
- “Clear all” appears after multiple filters

#### Empty states
- “No data yet” ≠ “No results” ≠ “No access”

---

### 3.3 Feedback Patterns
- **Success:** local confirmation + optional toast
- **Error:** inline + top-level alert when needed
- **Loading:** skeleton for content, indeterminate for background jobs
- **Empty:** guidance + primary next step

---

## Tokens

> This JSON is the canonical handoff structure. Implement as CSS variables (or platform equivalents) and reference tokens by role.

```json 
{ "ds": { "name": "DocOps Signal", "version": "1.0.0", "color": { "brand": { "ink": { "value": "#0B1220" }, "cloud": { "value": "#F6F8FB" }, "slate": { "value": "#2A3447" }, "steel": { "value": "#8A96AD" }, "cyan": { "value": "#00B7D6" }, "gold": { "value": "#C9A227" } }, "semantic": { "success": { "value": "#16A34A" }, "warning": { "value": "#D97706" }, "error": { "value": "#DC2626" }, "info": { "value": "#2563EB" } }, "mode": { "light": { "canvas": { "value": "{ds.color.brand.cloud}" }, "surface1": { "value": "#FFFFFF" }, "surface2": { "value": "#EEF2F7" }, "border": { "value": "rgba(11,18,32,0.12)" }, "textPrimary": { "value": "{ds.color.brand.ink}" }, "textSecondary": { "value": "{ds.color.brand.slate}" }, "textTertiary": { "value": "{ds.color.brand.steel}" }, "accent": { "value": "{ds.color.brand.cyan}" } }, "dark": { "canvas": { "value": "#070B14" }, "surface1": { "value": "{ds.color.brand.ink}" }, "surface2": { "value": "#111A2D" }, "border": { "value": "rgba(241,245,255,0.14)" }, "textPrimary": { "value": "#F1F5FF" }, "textSecondary": { "value": "#B7C2D9" }, "textTertiary": { "value": "#8EA0C4" }, "accent": { "value": "#28D9F5" } } } }, "type": { "fontFamily": { "sans": { "value": "IBM Plex Sans, ui-sans-serif, system-ui" }, "mono": { "value": "IBM Plex Mono, ui-monospace, SFMono-Regular" } }, "size": { "display": { "desktop": 40, "tablet": 36, "mobile": 32 }, "headline": { "desktop": 28, "tablet": 26, "mobile": 24 }, "title": { "desktop": 22, "tablet": 20, "mobile": 19 }, "body": { "desktop": 16, "tablet": 16, "mobile": 16 }, "callout": { "desktop": 15, "tablet": 15, "mobile": 15 }, "subheadline": { "desktop": 14, "tablet": 14, "mobile": 14 }, "footnote": { "desktop": 12, "tablet": 12, "mobile": 12 }, "caption": { "desktop": 11, "tablet": 11, "mobile": 11 } }, "lineHeight": { "display": { "desktop": 48, "tablet": 44, "mobile": 40 }, "headline": { "desktop": 34, "tablet": 32, "mobile": 30 }, "title": { "desktop": 28, "tablet": 26, "mobile": 24 }, "body": { "desktop": 24, "tablet": 24, "mobile": 24 }, "callout": { "desktop": 22, "tablet": 22, "mobile": 22 }, "subheadline": { "desktop": 20, "tablet": 20, "mobile": 20 }, "footnote": { "desktop": 18, "tablet": 18, "mobile": 18 }, "caption": { "desktop": 16, "tablet": 16, "mobile": 16 } }, "letterSpacing": { "display": { "value": -0.6 }, "headline": { "value": -0.3 }, "title": { "value": -0.2 }, "body": { "value": 0.0 }, "subheadline": { "value": 0.1 }, "footnote": { "value": 0.2 }, "caption": { "value": 0.2 } }, "weight": { "regular": { "value": 400 }, "medium": { "value": 500 }, "semibold": { "value": 600 }, "bold": { "value": 700 } } }, "space": { "4": { "value": 4 }, "8": { "value": 8 }, "12": { "value": 12 }, "16": { "value": 16 }, "24": { "value": 24 }, "32": { "value": 32 }, "48": { "value": 48 }, "64": { "value": 64 }, "96": { "value": 96 }, "128": { "value": 128 } }, "radius": { "sm": { "value": 8 }, "md": { "value": 12 }, "lg": { "value": 16 }, "pill": { "value": 999 } }, "shadow": { "sm": { "value": "0 6px 18px rgba(11,18,32,0.08)" }, "md": { "value": "0 16px 40px rgba(11,18,32,0.12)" }, "focusGlow": { "value": "0 0 0 4px rgba(0,183,214,0.22)" } }, "motion": { "duration": { "fast": { "value": "120ms" }, "base": { "value": "220ms" }, "slow": { "value": "420ms" } }, "easing": { "standard": { "value": "cubic-bezier(0.16, 1, 0.3, 1)" }, "snappy": { "value": "cubic-bezier(0.2, 0.9, 0.2, 1)" } } }, "component": { "button": { "height": { "default": 40, "compact": 32, "large": 48 }, "paddingX": { "default": 16, "compact": 12, "large": 18 }, "radius": { "value": "{ds.radius.md}" } }, "field": { "height": { "value": 44 }, "paddingX": { "value": 12 }, "radius": { "value": "{ds.radius.md}" } } } } }
```

---

## Documentation

### 5.1 Principles

1. **Clarity over cleverness**  
   Label values directly (“NPS Score — 72”). Don’t make users decode visuals.

2. **Hierarchy is a contract**  
   One primary action per region/card. Everything else is secondary or tertiary.

3. **Depth with restraint**  
   Use elevation and glass only when it improves comprehension (overlays, focus), not as decoration.

---

### 5.2 Dos and Donts (10)

1. **Do:** One Primary CTA per view.  
   **Don’t:** Multiple primaries competing in the same card.

2. **Do:** Use gold for “Review/Audit.”  
   **Don’t:** Use error red to indicate “needs attention.”

3. **Do:** Provide empty states with a next step.  
   **Don’t:** Show blank containers with “No data.”

4. **Do:** Use Plex Mono for numeric density.  
   **Don’t:** Use proportional numerals in KPI grids and tables.

5. **Do:** Inline validation near the field.  
   **Don’t:** Error modals for simple form mistakes.

6. **Do:** Use skeletons that match the layout.  
   **Don’t:** Generic shimmer blocks unrelated to content.

7. **Do:** Keep focus rings visible and consistent.  
   **Don’t:** Remove outlines or rely on hover-only cues.

8. **Do:** Charts for trends; tables for exactness.  
   **Don’t:** Use charts when precision is the goal.

9. **Do:** Toast for confirmations + durable activity log for jobs.  
   **Don’t:** Hide background tasks without a traceable status.

10. **Do:** Use semantic colors only for semantic meaning.  
    **Don’t:** Decorate UIs with success/error colors.

---

### 5.3 Developer Implementation Guide

#### 1) Implement tokens first
- Export tokens to CSS variables (or platform equivalents).
- Components should reference **role tokens** (`mode.*`) rather than raw brand colors.

#### 2) Build accessible primitives
Start with: Button, Field, Select, Dialog, Tooltip, Table.  
Define keyboard behavior early:
- Tab/Shift+Tab navigation
- Enter/Space activate
- Esc closes overlays
- Arrow key navigation in tabs/menus/listboxes

#### 3) Motion (HIG-style restraint)
- Use a single orchestrated “page load” reveal (staggered card animations).
- Respect reduced motion preferences: keep motion minimal and non-essential.

#### 4) QA checklist
- Contrast: verify light + dark
- Focus visible: every interactive element
- Hit targets: ≥ 44×44
- Screen reader labels: icon buttons, error messages, dynamic updates (toasts/jobs)

---

**Changelog**
- v1.0: Initial release of Foundations, Components, Patterns, Tokens, and Guidance.