# DocOps Buttons
[TOC]

## Regular

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

## Pill

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

## Hex

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
"ref": "../images/docops.svg"
},
"date": "",
"author": [
"Steve Roach",
"Ian Rose"
]
},
{
"label": "Consumer Https Changes",
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
"buttonType": "HEX",
"theme": {
"hexLinesEnabled": false,
"strokeColor": "#ffad00",
"colorTypeMap": {"software": "#058296", "social": "#3a3bf6"},
"colors": [
"#1E93AB", "#007bff", "#28a745", "#dc3545", "#6c757d"
],
"scale": 1,
"columns": 3,
"buttonStyle": {
"labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 16px; font-weight: 700; font-style: normal; font-variant: small-caps; text-decoration: none;"
}
}
}

[/docops]

## Rectangle 

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

## Circle

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

## Large
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

### Large with QR Code

[docops:buttons]
{
"buttons": [
{
"label": "X",
"link": "https://twitter.com",
"description": "Follow us",
"type": "formerly twitter",
"embeddedImage": {
"qrEnabled": true
}
},
{
"label": "Facebook",
"link": "https://facebook.com",
"description": "Like our page",
"type": "old people"
},
{
"label": "Instagram",
"link": "https://instagram.com",
"description": "See our photos",
"type": "doom scroll",
"embeddedImage": {
"qrEnabled": true
}
},
{
"label": "LinkedIn",
"link": "https://linkedin.com",
"description": "Connect with us",
"type": "business social"
},
{
"label": "YouTube",
"link": "https://youtube.com",
"description": "Watch our videos",
"type": "videos",
"embeddedImage": {
"qrEnabled": true
}
}
],
"buttonType": "LARGE",
"theme": {
"colors": [
"#E11D48",
"#22D3EE",
"#8B5CF6",
"#10B981",
"#F59E0B"
],
"colorTypeMap": {
"social": "#FF0000"
},
"columns": 5,
"scale": 1,
"sortBy" : {
"sort": "ORDER"
}
"buttonStyle": {
"labelStyle": "font-family: 'Roboto', sans-serif; font-size: 16px; fill: #ffffff; font-weight: bold; text-anchor: middle;",
"descriptionStyle": "font-family: 'Roboto', sans-serif; font-size: 10px; fill: #ffffff; text-anchor: middle;"
}
}
}

[/docops]
## Round

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

## Oval

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