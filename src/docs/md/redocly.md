# Redocly 

<style>

/* Redoc left navigation — light mode */
[data-role="menu-content"],
.menu-content {
  background-color: #f5f6f7 !important;
  border-right: 1px solid #e5e7eb;
}

/* Nav item text */
.menu-content li > label {
  color: #3d4151 !important;
}

/* Active / hover */
.menu-content li[data-item-id]:hover > label,
.menu-content li.active > label {
  color:            #1a56db !important;
  background-color: rgba(26, 86, 219, 0.07) !important;
}

/* Search box */
[data-role="menu-content"] input[type="search"] {
  background: #ffffff;
  border:     1px solid #d1d5db;
  color:      #1a1d2e;
}
</style>

## Example 


[redocly:https://raw.githubusercontent.com/bump-sh/examples/refs/heads/main/apis/bump.yml title="Payments API" disableSearch=false hideHostname=true requiredPropsFirst=true primaryColor="#32329f"]

## Source

```markdown
[redocly:https://raw.githubusercontent.com/bump-sh/examples/refs/heads/main/apis/bump.yml title="Payments API" disableSearch=false hideHostname=true requiredPropsFirst=true primaryColor="#32329f"]
```