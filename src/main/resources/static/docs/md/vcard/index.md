# DocOps VCard Generator

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/VCardIcon.svg" alt="VCard Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #3b82f6; font-size: 32px;">DocOps VCard</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Transform contact information into professional, scannable business cards</p>
    </div>
  </div>
</div>

[TOC]

## What is DocOps VCard?

DocOps VCard converts standard vCard 3.0 format contact information into beautiful, professional SVG business cards with embedded QR codes. Perfect for team directories, speaker bios, and documentation contact references.

### Key Features

- **Standard vCard format** - Uses industry-standard vCard 3.0
- **Multiple design templates** - Modern, tech-pattern, and neo-brutalist styles
- **Auto QR generation** - Scannable codes for instant contact saves
- **Light & dark themes** - Adapts to your documentation style
- **Professional layouts** - Clean hierarchy with role and organization

---

## Default Look

[docops:vcard]
BEGIN:VCARD
VERSION:3.0
FN:Sarah Chen
N:Chen;Sarah;;;
ORG:Acme Corporation
TITLE:Product Manager
TEL;TYPE=WORK:+1-555-0123
EMAIL:sarah.chen@acme.com
URL:https://linkedin.com/in/sarachen
END:VCARD
[/docops]

## Design Templates

### Modern Card (Default)

Clean, contemporary design with generous spacing:

[docops:vcard]
design=modern_card
theme=light
---
BEGIN:VCARD
VERSION:3.0
N:Doe;John;;Mr;
FN:John Doe
ORG:Acme Corporation
TITLE:Senior Software Engineer
EMAIL;TYPE=WORK,INTERNET:john.doe@acme.com
TEL;TYPE=WORK,VOICE:+1 (555) 123-4567
TEL;TYPE=CELL,VOICE:+1 (555) 987-6543
ADR;TYPE=WORK:;;123 Innovation Drive;Tech City;CA;94043;USA
URL:https://www.acme.com
NOTE:Building the future of anvil technology.
END:VCARD
[/docops]



### Tech Pattern Background

Tech motif with geometric patterns - great for technical teams:

[docops:vcard]

design=tech_pattern_background
theme=light
---
BEGIN:VCARD
VERSION:3.0
N:Rivera;Jordan;;;
FN:Jordan Rivera
ORG:Creative Studio
TITLE:Digital Product Designer
EMAIL;TYPE=INTERNET:hello@jordanrivera.design
TEL;TYPE=CELL:+1 (415) 555-0199
URL;type=pref:https://jordanrivera.design
URL;type=linkedin:https://linkedin.com/in/jordanrivera
URL;type=twitter:https://twitter.com/jordan_creates
NOTE:Designing simple products for complex problems.
END:VCARD
[/docops]


### Neo-Brutalist (Dark)

Bold, modern design with strong visual hierarchy:

[docops:vcard,useDark=true]
design=neo_brutalist
theme=dark
---
BEGIN:VCARD
VERSION:3.0
N:Smith;Alex;;;
FN:Alex Smith
EMAIL:alex@example.org
TEL:+1-202-555-0102
END:VCARD
[/docops]



## Format Options

### Basic Structure

Minimal example with required fields only:

[docops:vcard]
BEGIN:VCARD 
VERSION:3.0 
FN:Full Name 
N:LastName;FirstName;;; 
EMAIL:email@example.com 
TEL:+1-555-0000 END:VCARD 
[/docops]

### With Design Options

Configure template and theme:

[docops:vcard]
BEGIN:VCARD 
VERSION:3.0 
FN:Name ... 
END:VCARD 
[/docops]

### Configuration Options

| Option | Values | Purpose |
|--------|--------|---------|
| design | `modern_card`, `tech_pattern_background`, `neo_brutalist` | Visual template style |
| theme  | `light`, `dark` | Color scheme |

---

## VCard Fields Reference

### Required Fields

BEGIN:VCARD VERSION:3.0 
FN:Display Name (How the name appears) 
N:Last;First;;; (Structured for sorting) 
END:VCARD

### Common Optional Fields

```text
ORG:Company Name (Organization) 
TITLE:Job Title (Role/position) 
TEL;TYPE=WORK:+1-555-0000 (Work phone) 
TEL;TYPE=CELL:+1-555-0001 (Mobile) 
EMAIL:email@company.com (Email) 
URL:https://profile.com (Website) ADR;
TYPE=WORK:;;Street;City;State;ZIP;Country 
NOTE:Additional context (Brief notes)
```
### Multiple Contact Methods

```text
EMAIL;TYPE=WORK,INTERNET:work@company.com 
EMAIL;TYPE=INTERNET:personal@email.com 
TEL;TYPE=WORK,VOICE:+1-555-0123 
TEL;TYPE=CELL:+1-555-0456 
URL;type=pref:[https://website.com](https://website.com) 
URL;type=linkedin:[https://linkedin.com/in/user](https://linkedin.com/in/user)
```

---

## Best Practices

- **Keep FN readable** - Use full display name like "Jordan A. Rivera"
- **Structure N properly** - Format: `Last;First;Middle;Prefix;Suffix`
- **Be consistent with TITLE** - "Product Manager" not "PM / Product Strategist / Team Lead"
- **Include at least one contact** - Email or phone minimum
- **Add professional URLs** - LinkedIn, portfolio, or company profile
- **Use NOTE sparingly** - Brief specialization or tagline only
- **Store in data files** - Keep vCards in `data/docops/` for reusability
- **Match design to context** - `modern_card` for corporate, `neo_brutalist` for tech

<div style="background: #eff6ff; border-left: 4px solid #3b82f6; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #1e40af; font-weight: 600;">👤 VCard Tip</p>
  <p style="margin: 8px 0 0 0; color: #475569;">For team directories, store each person's vCard in separate text files and use consistent design templates. The QR code lets mobile users save contact info instantly by scanning.</p>
</div>

---

## Common Use Cases

- **Team directories** - Engineering team contact cards in documentation
- **Speaker profiles** - Conference or webinar presenter bios
- **Documentation ownership** - Link technical writers or SMEs to content areas
- **Client portals** - Customer-facing account manager information
- **Handoff documents** - Project transition with key stakeholder contacts
- **Support pages** - Help desk or support team contact information

---

## International Contacts

DocOps VCard fully supports international names, addresses, and phone numbers:

[docops:vcard]
---
design=modern_card
theme=light
---
BEGIN:VCARD
VERSION:3.0
N:Hanakura;Ren;;;
FN:Ren Hanakura (花倉 蓮)
ORG:Future Tech (フューチャーテック)
TITLE:R&D Lead
EMAIL;TYPE=WORK:ren.hanakura@futuretech.jp
TEL;TYPE=CELL:+81 90-1234-5678
ADR;TYPE=WORK:;;1-2-3 Marunouchi;Chiyoda-ku;Tokyo;100-0005;Japan
URL:https://www.futuretech.jp
NOTE:Specializing in AI and Robotics.
END:VCARD
[/docops]

---

## Design Template Reference

| Template | Best For | Theme Support |
|----------|----------|---------------|
| **modern_card** | Corporate, professional contexts | Light, Dark |
| **tech_pattern_background** | Tech teams, developer docs | Light, Dark |
| **neo_brutalist** | Bold, modern aesthetic | Light, Dark |

### Template Selection Guide

- **Corporate/Business** → `modern_card` with `light` theme
- **Developer Documentation** → `tech_pattern_background` with `dark` theme
- **Creative/Design Teams** → `neo_brutalist` with `light` theme
- **Internal Wiki** → `modern_card` with `light` theme


