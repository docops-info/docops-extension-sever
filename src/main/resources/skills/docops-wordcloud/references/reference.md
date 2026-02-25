# DocOps WordCloud Reference

## Asciidoc Format

```asciidoc
[docops,wordcloud]
----
title= Key Technologies
minFontSize= 12
maxFontSize= 60
---
Word | Weight | Color
Kubernetes | 95 | #3498db
Docker | 88 | #2ecc71
Python | 82 | #e74c3c
JavaScript | 78 | #f39c12
PostgreSQL | 72 | #9b59b6
----
```

## Markdown Format

```md
[docops:wordcloud]
title= Survey Results
minFontSize= 12
maxFontSize= 60
---
Word | Weight | Color
Performance | 90 | #3498db
Usability | 85 | #2ecc71
Support | 70 | #e74c3c
[/docops]
```

## Options

- `title`: Cloud title.
- `minFontSize`: Smallest font size (default: 10).
- `maxFontSize`: Largest font size (default: 60).
- `width`: Canvas width (default: 800).
- `height`: Canvas height (default: 600).
- `useDark`: `true` for dark theme.
- `scale`: Overall scale multiplier.

## Weight Guidelines
- 90-100: Dominant (largest)
- 70-89: Important (large)
- 50-69: Common (medium)
- 30-49: Supporting (small)
- 10-29: Rare (smallest)
