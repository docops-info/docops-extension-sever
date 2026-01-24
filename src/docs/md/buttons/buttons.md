
# Interactive Buttons

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #0891b2 0%, #06b6d4 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/ButtonIcon.svg" alt="Button Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #0891b2; font-size: 32px;">DocOps Buttons</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Create interactive navigation with 9 versatile button styles and layouts</p>
    </div>
  </div>
</div>

[TOC]

## What are DocOps Buttons?

DocOps Buttons are SVG-based interactive navigation elements that transform your documentation into an engaging, clickable experience. With nine distinct styles and extensive customization options, you can create everything from simple navigation menus to complex interactive dashboards.

### Key Features

- **9 Button Styles** - REGULAR, PILL, RECTANGLE, ROUND, CIRCLE, LARGE, SLIM, OVAL, and HEX
- **Full Customization** - Colors, gradients, fonts, spacing, and layout control
- **Rich Metadata** - Support for descriptions, dates, authors, and embedded images
- **Smart Sorting** - Organize by label, type, date, author, or custom order
- **Grid Layouts** - Responsive column-based arrangements
- **Interactive Elements** - Hover effects, links, and sub-navigation

<div style="background: #ecfeff; border-left: 4px solid #06b6d4; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #0e7490; font-weight: 600;">🎨 Design Flexibility</p>
  <p style="margin: 8px 0 0 0; color: #0891b2;">Each button type offers unique visual characteristics perfect for different use cases—from compact navigation to feature showcases.</p>
</div>

---

## Button Types Gallery

### Regular Buttons

Classic rectangular buttons with rounded corners—perfect for standard navigation.

[docops:buttons]
{
"buttons": [
{
"link": "https://www.google.com",
"label": "#[Google]",
"description": "",
"type": "search"
},
{
"link": "https://www.apple.com",
"label": "Apple",
"description": "",
"type": "hardware"
},
{
"link": "https://www.microsoft.com",
"label": "Microsoft",
"description": "",
"type": "software"
},
{
"link": "https://www.amazon.com",
"label": "Amazon",
"description": "books",
"type": "books"
},
{
"link": "https://www.netflix.com",
"label": "Netflix",
"description": "movies",
"type": "movies"
}
],
"buttonType": "REGULAR",
"theme": {
"colors": [
"#003b6b",
"#FF6F36",
"#2C7865",
"#C40C0C",
"#45618E",
"#FF5BAE"
],
"strokeColor": "#111111",
"columns": 2,
"sortBy": {
"sort": "ORDER"
},
"buttonStyle": {
"labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #fcfcfc; letter-spacing: normal;font-weight: bold;",
"dateStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #000000; letter-spacing: normal;font-weight: normal;",
"descriptionStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 10px; fill: #fcfcfc; letter-spacing: normal;font-weight: normal;",
"typeStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; letter-spacing: normal;font-weight: bold; font-style: italic;",
"authorStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px;  fill: #fcfcfc; letter-spacing: normal;font-weight: normal; font-style: italic;"
},
"scale": 1.0
}
}
[/docops]

---

### Pill Buttons

Fully rounded buttons with smooth, modern aesthetics—ideal for primary actions.

[docops:buttons]
{
"buttons": [
{
"label": "Amazon",
"link": "https://www.amazon.com",
"description": "Amazon.com, Inc. is an American multinational technology company which focuses on e-commerce, cloud computing, digital streaming, and artificial intelligence",
"type": "storefront",
"date": "",
"author": [
"Jeff Bezos"
]
},
{
"label": "Apple",
"link": "https://www.apple.com",
"description": "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. ",
"type": "Hardware",
"date": "01/30/1977",
"author": [
"Steve Jobs",
"Steve Wozniak"
]
},
{
"label": "DocOps.io",
"link": "#[link-server]#[app]",
"description": "Sharing documentation experience for developers to extend with AsciiDoctor",
"type": "docs",
"date": "",
"author": [
"Steve Roach",
"Ian Rose"
]
}
],
"buttonType": "PILL",
"theme": {
"colors": [
"#003b6b",
"#FF6F36",
"#2C7865",
"#C40C0C",
"#45618E",
"#FF5BAE"
],
"scale": 0.5,
"columns": 3,
"buttonStyle": {
"labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 24px; fill: #fcfcfc; letter-spacing: normal;font-weight: bold;",
"descriptionStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 10px; fill: #000000; letter-spacing: normal;font-weight: normal;",
"authorStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 10px; fill: #000000; letter-spacing: normal;font-weight: bold; font-style: italic;",
"dateStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #000000; letter-spacing: normal;font-weight: bold; font-style: normal;"
}
}
}

[/docops]

---

### Hexagonal Buttons

Honeycomb-style buttons—perfect for creating interconnected navigation systems.

[docops:buttons]
{
"buttons": [
{
"label": "Amazon Web Services",
"link": "https://www.amazon.com",
"description": "Amazon.com, Inc. is an American multinational technology company which focuses on e-commerce, cloud computing, digital streaming, and artificial intelligence",
"type": "storefront",
"date": "",
"author": [
"Jeff Bezos"
]
},
{
"label": "Apple",
"link": "https://www.apple.com",
"description": "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. ",
"type": "Hardware",
"date": "01/30/1977",
"embeddedImage": {
"ref": "<Apple>"
},
"author": [
"Steve Jobs",
"Steve Wozniak"
]
},
{
"label": "DocOps.io",
"link": "#[link-server]#[app]",
"description": "Sharing documentation experience for developers to extend with AsciiDoctor",
"type": "docs",
"embeddedImage": {
"ref": "../../images/docops.svg"
},
"date": "",
"author": [
"Steve Roach",
"Ian Rose"
]
},{
"link": "https://www.microsoft.com",
"label": "Microsoft",
"description": "",
"type": "software"
},
{
"link": "https://www.netflix.com",
"label": "Netflix",
"description": "movies",
"type": "movies"
},{
"link": "https://www.google.com",
"label": "Google",
"description": "",
"type": "search"
}
],
"buttonType": "HEX",
"theme": {
"hexLinesEnabled": false,
"strokeColor": "#ffad00",
"colorTypeMap": {"software": "#058296", "social": "#3a3bf6"},
"colors": [
"#E11D48",
"#22D3EE",
"#8B5CF6",
"#C40C0C",
"#45618E",
"#FF5BAE"
],
"scale": 0.8,
"columns": 3,
"buttonStyle": {
"labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 16px; font-weight: 700; font-style: normal; font-variant: small-caps; text-decoration: none;"
}
}
}

[/docops]

---

### Rectangle Buttons with Sub-Links

Rectangular buttons with hierarchical navigation—great for content-rich menus.

[docops:buttons]
{
"buttons": [
{
"link": "https://www.google.com",
"label": "Google Search",
"description": "World's most popular search engine",
"type": "search",
"links": [
{"label": "Google Images", "href": "https://images.google.com"},
{"label": "Google Scholar", "href": "https://scholar.google.com"},
{"label": "Google Maps", "href": "https://maps.google.com"}
]
},
{
"link": "https://www.apple.com",
"label": "Apple",
"description": "Innovative technology products",
"type": "hardware",
"links": [
{"label": "iPhone", "href": "https://www.apple.com/iphone"},
{"label": "iPad", "href": "https://www.apple.com/ipad"},
{"label": "Mac", "href": "https://www.apple.com/mac"},
{"label": "Apple Store", "href": "https://www.apple.com/retail"}
]
},
{
"link": "https://www.microsoft.com",
"label": "Microsoft",
"description": "Enterprise software solutions",
"type": "software",
"links": [
{"label": "Office 365", "href": "https://www.office.com"},
{"label": "Azure", "href": "https://azure.microsoft.com"},
{"label": "Visual Studio", "href": "https://visualstudio.microsoft.com"}
]
},
{
"link": "https://www.amazon.com",
"label": "Amazon",
"description": "Online marketplace and cloud services",
"type": "ecommerce",
"links": [
{"label": "Prime Video", "href": "https://www.primevideo.com"},
{"label": "AWS", "href": "https://aws.amazon.com"},
{"label": "Kindle", "href": "https://www.amazon.com/kindle"},
{"label": "Alexa", "href": "https://www.amazon.com/alexa"}
]
},
{
"link": "https://www.netflix.com",
"label": "Netflix",
"description": "Streaming entertainment service",
"type": "entertainment",
"links": [
{"label": "Browse Movies", "href": "https://www.netflix.com/browse"},
{"label": "TV Shows", "href": "https://www.netflix.com/browse/genre/83"},
{"label": "Netflix Originals", "href": "https://www.netflix.com/originals"}
]
},
{
"link": "https://github.com",
"label": "GitHub",
"description": "Code hosting and collaboration",
"type": "development",
"links": [
{"label": "Explore", "href": "https://github.com/explore"},
{"label": "GitHub Pages", "href": "https://pages.github.com"},
{"label": "GitHub Actions", "href": "https://github.com/features/actions"},
{"label": "Copilot", "href": "https://github.com/features/copilot"}
]
}
],
"buttonType": "RECTANGLE",
"theme": {
"colors": [
"#003b6b",
"#FF6F36",
"#2C7865",
"#C40C0C",
"#45618E",
"#FF5BAE"
],
"strokeColor": "#111111",
"columns": 2,
"sortBy": {
"sort": "ORDER"
},
"buttonStyle": {
"labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 14px; fill: #1a202c; letter-spacing: normal; font-weight: bold;",
"descriptionStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 10px; fill: #4a5568; letter-spacing: normal; font-weight: normal;",
"linkStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 11px; fill: #2b6cb0; letter-spacing: normal; font-weight: normal;"
},
"scale": 1.0
}
}
[/docops]

---

### Circle Buttons with Icons

Perfectly circular buttons featuring embedded brand icons—excellent for social media links.

[docops:buttons]
{
"buttons": [
{
"label": "Google",
"link": "https://www.google.com",
"description": "Google is is an American multinational technology company that specializes in Internet-related services and products ",
"type": "advertisement",
"date": "07/30/1998",
"embeddedImage": {
"ref": "<Google>"
},
"author": [
"Sergey Brin",
"Larry Page"
]
},
{
"label": "Apple",
"link": "https://www.apple.com",
"description": "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. ",
"type": "Hardware",
"date": "01/30/1977",
"embeddedImage": {
"ref": "<Apple>"
},
"author": [
"Steve Jobs",
"Steve Wozniak"
]
}
],
"buttonType": "CIRCLE",
"theme": {
"colors": [
"#ffffff"
],
"columns": 2,
"scale": 1.0
}
}

[/docops]

---

### Large Card Buttons

Feature-rich card-style buttons with extended metadata—perfect for showcasing products or features.

[docops:buttons]
{
"buttons": [
{
"label": "Google",
"link": "https://www.google.com",
"description": "Google is is an American multinational technology company that specializes in Internet-related services and products ",
"type": "advertisement",
"date": "07/30/1998",
"author": [
"Sergey Brin",
"Larry Page"
]
},
{
"label": "Apple",
"link": "https://www.apple.com",
"description": "Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. ",
"type": "Hardware",
"date": "01/30/1977",
"author": [
"Steve Jobs",
"Steve Wozniak"
]
},{
"label": "DocOps.io",
"link": "#[link-server]#[app]",
"description": "Sharing documentation experience for developers to extend with AsciiDoctor",
"type": "docs",
"embeddedImage": {
"ref": "../images/docops.svg"
},
"date": "",
"author": [
"Steve Roach",
"Ian Rose"
]
}
],
"buttonType": "LARGE",
"theme": {
"colors": [
"#E11D48",
"#22D3EE",
"#8B5CF6",
"#C40C0C",
"#45618E",
"#FF5BAE"
],
"columns": 3,
"buttonStyle": {
"labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #111111; letter-spacing: normal;font-weight: bold;",
"descriptionStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 10px; fill: #000000; letter-spacing: normal;font-weight: normal;",
"authorStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 10px; fill: #000000; letter-spacing: normal;font-weight: bold; font-style: italic;",
"dateStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #000000; letter-spacing: normal;font-weight: bold; font-style: normal;"
}
}
}

[/docops]

---

### Round Buttons

Softly rounded buttons with modern styling—versatile for most use cases.

[docops:buttons]
{
"buttons": [
{
"link": "https://www.google.com",
"label": "Google",
"description": "",
"type": "search"
},
{
"link": "https://www.apple.com",
"label": "Apple",
"description": "",
"type": "hardware"
},
{
"link": "https://www.microsoft.com",
"label": "Microsoft",
"description": "",
"type": "software"
},
{
"link": "https://www.amazon.com",
"label": "Amazon",
"description": "books",
"type": "books"
},
{
"link": "https://www.netflix.com",
"label": "Netflix",
"description": "movies",
"type": "movies"
}
],
"buttonType": "ROUND",
"theme": {
"colors": [
"#003b6b",
"#FF6F36",
"#2C7865",
"#C40C0C",
"#45618E",
"#FF5BAE"
],
"strokeColor": "#111111",
"columns": 2,
"sortBy": {
"sort": "ORDER"
},
"buttonStyle": {
"labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #fcfcfc; letter-spacing: normal;font-weight: bold;",
"dateStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #000000; letter-spacing: normal;font-weight: normal;",
"descriptionStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 10px; fill: #fcfcfc; letter-spacing: normal;font-weight: normal;",
"typeStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; letter-spacing: normal;font-weight: bold; font-style: italic;",
"authorStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px;  fill: #fcfcfc; letter-spacing: normal;font-weight: normal; font-style: italic;"
},
"scale": 1.0
}
}

[/docops]

---

### Oval Buttons

Elongated elliptical buttons—ideal for timeline-style layouts.

[docops:buttons]
{
"buttons": [
{
"label": "Project Start",
"link": "#phase1",
"description": "Initial planning",
"type": "milestone",
"date": "01/15/2023"
},
{
"label": "Design Phase",
"link": "#phase2",
"description": "UI/UX development",
"type": "milestone",
"date": "03/01/2023"
},
{
"label": "Development",
"link": "#phase3",
"description": "Core functionality",
"type": "milestone",
"date": "05/15/2023"
},
{
"label": "Testing",
"link": "#phase4",
"description": "QA and bug fixes",
"type": "milestone",
"date": "07/30/2023"
},
{
"label": "Launch",
"link": "#phase5",
"description": "Public release",
"type": "milestone",
"date": "09/15/2023"
}
],
"buttonType": "OVAL",
"theme": {
"colors": [
"#8e44ad",
"#9b59b6",
"#2980b9",
"#3498db",
"#16a085"
],
"columns": 3,
"scale": 1.0,
"sortBy": {
"sort": "DATE",
"direction": "ASCENDING"
},
"buttonStyle": {
"labelStyle": "font-family: 'Open Sans', sans-serif; font-size: 14px; fill: #ffffff; font-weight: bold;",
"descriptionStyle": "font-family: 'Open Sans', sans-serif; font-size: 10px; fill: #ffffff;",
"dateStyle": "font-family: 'Open Sans', sans-serif; font-size: 12px; fill: #ffffff; font-weight: bold;"
}
}
}

[/docops]

---

## Button Anatomy

Each button can include:

| Component | Description | Optional |
|-----------|-------------|----------|
| **Label** | Button text/title | Required |
| **Link** | Destination URL | Required |
| **Description** | Additional context | Yes |
| **Type** | Category/classification | Yes |
| **Date** | Associated date (MM/DD/YYYY) | Yes |
| **Author** | Creator(s) | Yes |
| **Color** | Custom button color | Yes |
| **Embedded Image** | Logo or icon | Yes |
| **Sub-Links** | Secondary navigation | Yes |

## Customization Options

### Theme Configuration

```json 
{
  "theme": {
    "colors": [
      "#3498db",
      "#2ecc71",
      "#e74c3c"
    ],
    "columns": 3,
    "scale": 1.0,
    "newWin": true,
    "useDark": false,
    "strokeColor": "#111111",
    "sortBy": {
      "sort": "LABEL",
      "direction": "ASCENDING"
    }
  }
}
```

### Sorting Options
- **LABEL** - Alphabetical by button label
- **TYPE** - Group by category
- **DATE** - Chronological order
- **AUTHOR** - By creator
- **ORDER** - Manual ordering

<div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #92400e; font-weight: 600;">💡 Pro Tip</p>
  <p style="margin: 8px 0 0 0; color: #b45309;">Use color-coded buttons with the <code>colorTypeMap</code> to automatically assign colors based on button types, creating visual consistency across your documentation.</p>
</div>

---

## Common Use Cases

### Navigation Menus
Create site-wide navigation with consistent button styles and clear hierarchy.

### Product Showcases
Use LARGE buttons with embedded images and detailed descriptions for feature highlights.

### Social Media Links
Circle buttons with brand icons provide recognizable social media navigation.

### Timeline & Roadmaps
Oval or SLIM buttons arranged chronologically tell your project's story.

### Dashboard Links
Hexagonal buttons create unique, interconnected dashboard interfaces.

---

<div align="center" style="margin-top: 48px; padding: 24px; background: #fafbfc; border-radius: 8px;">
  <p style="color: #64748b; margin: 0;">Ready to create interactive button navigation?</p>
  <p style="color: #0891b2; font-weight: 600; margin: 8px 0 0 0;">Explore all 9 button styles with the DocOps button extension</p>
</div>