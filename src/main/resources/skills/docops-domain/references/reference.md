# Domain Visualization Reference

## Asciidoc Format

```asciidoc
[docops,domain,useDark=true]
----
main,TECHNOLOGY

type,emoji,rowIndex,nodes
COMMON,,0,"FRONTEND,BACKEND,DATABASE"
COMMON,,1,"DEVOPS,DOCOPS"
FRONTEND,🧑🏻‍💻,0,"HTML,CSS,JAVASCRIPT"
BACKEND,🧑🏻‍💻,0,"KOTLIN,JAVA,PYTHON"
----
```

## Markdown Format

```md
[docops:domain]
main,SYSTEM_LANDSCAPE

type,emoji,rowIndex,nodes
COMMON,,0,"Auth,Logging,Monitoring"
SALES,🚀,0,"CRM,Billing"
SUPPORT,🎧,0,"Ticketing,Chat"
[/docops]
```

## Data Structure

- **main,NAME**: Declares the root node.
- **type,emoji,rowIndex,nodes**: Header row (required).
- **type**: `COMMON` for top-level shared nodes or a custom name for vertical groups.
- **emoji**: Optional emoji for the vertical group.
- **rowIndex**: `0`, `1`, etc. (determines vertical order within the group).
- **nodes**: Comma-separated list of node names. Use quotes if node names contain commas.

## Wiki-Style Links

Embed links within node names:
`"[[https://docs.com API Docs]],SERVICE,DATABASE"`

Multiple links per node (semicolon-separated):
`"[[https://github.com Code];[[https://wiki.com Docs]]"`

## Configuration Options (Attributes)

- `useDark=true`: Enable dark theme.
- `useNeural=true`: Alternative neural network visual style.
- `controls=true`: Show UI controls.
