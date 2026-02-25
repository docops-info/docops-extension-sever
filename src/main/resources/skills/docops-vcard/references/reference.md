# DocOps VCard Reference

DocOps VCard transforms vCard 3.0 data into professional SVG business cards with QR codes.

## Asciidoc Format

```asciidoc
[docops,vcard,controls=true]
----
design=modern_card
theme=light
---
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
----
```

## Markdown Format

```md
[docops:vcard]
design=tech_pattern_background
theme=dark
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
NOTE:Designing simple products for complex problems.
END:VCARD
[/docops]
```

## Configuration Options

| Option | Values | Purpose |
|--------|--------|---------|
| `design` | `modern_card`, `tech_pattern_background`, `neo_brutalist` | Visual template style |
| `theme` | `light`, `dark` | Color scheme |
| `scale` | float | Size multiplier (default: 1.0) |
| `useDark`| `true`/`false` | Dark mode (Asciidoc attribute) |

## Supported vCard Fields

### Required Fields
- `BEGIN:VCARD`
- `VERSION:3.0`
- `FN`: Full Name (Display Name)
- `N`: Structured Name (`Last;First;Middle;Prefix;Suffix`)
- `END:VCARD`

### Common Optional Fields
- `ORG`: Company/Organization
- `TITLE`: Job Title
- `EMAIL`: Email address
- `TEL`: Phone number (use `TYPE=WORK` or `TYPE=CELL`)
- `URL`: Website or social profile
- `ADR`: Address (`;;Street;City;State;ZIP;Country`)
- `NOTE`: Brief specialization or tagline

## Design Templates

- **modern_card**: Contemporary design with generous spacing (default).
- **tech_pattern_background**: Tech motif with geometric patterns.
- **neo_brutalist**: Bold, modern design with strong visual hierarchy and borders.

## Best Practices

- **Keep FN readable**: Use the full display name.
- **Consistent Titles**: Use standard job titles for clarity.
- **Contact Minimum**: Include at least one email or phone number.
- **QR Scannability**: The generated QR code allows users to instantly save the contact to their phone.
- **Reusability**: Store vCards in `data/` directories and use `include::` in Asciidoc for team directories.
