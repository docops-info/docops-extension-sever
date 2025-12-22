# Development Tasks Checklist

Derived from `docs/plan.md` and `docs/requirements.md`.
Legend:
- Requirements: R1..R12 (see `docs/requirements.md`)
- Plan Items: P1..P11 (see `docs/plan.md`)

Use `[ ]` to mark open and `[x]` when completed. Keep links to Plan and Requirements intact.

## Phase 1 — Setup

1. [x] Confirm repository builds and tests pass on main before changes (Plan: P4; Reqs: R5, R11)
2. [x] Create feature branch `feature/timeline-horizontal` (Plan: P1; Reqs: R1, R3)
3. [x] Review current timeline components: `TimelineHandler`, `TimelineParser`, `TimelineSvgGenerator`, `TimelineEvent` (Plan: P1, P3, P6; Reqs: R6, R8)

## Phase 2 — Parsing & Configuration

4. [x] Implement config section split at line `---` in `TimelineHandler` (Plan: P1; Reqs: R2, R8)
5. [x] Parse simple `key=value` pairs from the config block (Plan: P1; Reqs: R2)
6. [x] Implement `type` normalization: accept `H`/`V` case-insensitive (Plan: P2; Reqs: R1, R3)
7. [x] Handle invalid/unknown `type`: default to vertical and log warning (Plan: P2; Reqs: R3, R7)
8. [x] Ensure absence of config treats entire input as payload; default vertical (Plan: P1; Reqs: R2, R3)

## Phase 3 — Rendering Integration

9. [x] Add orientation selection flow: handler delegates to generator for H vs V (Plan: P5; Reqs: R1, R5, R8)
10. [x] Keep external API unchanged (Plan: P5; Reqs: R8)
29. [x] Extract VCard SVG generators into Strategy pattern with per-design renderer classes; delegate selection via registry with fallback (Plan: P3; Reqs: R4, R11)

## Phase 4 — Horizontal Rendering Path

11. [x] Implement horizontal layout in `TimelineSvgGenerator` following `designs/timeline-h.svg` (Plan: P3; Reqs: R1, R4)
12. [x] Reuse existing style constants and color palette where possible (Plan: P3; Reqs: R4)
13. [x] Implement markers/connectors and label placement for horizontal axis (Plan: P3; Reqs: R4, R12)
14. [x] Handle overflow: spacing or width growth consistent with current behavior (Plan: P3; Reqs: R4, R11)
15. [x] Ensure vertical rendering behavior is preserved exactly (Plan: P4; Reqs: R3, R5)

## Phase 5 — Data Parsing & Validation

16. [x] Verify `TimelineParser` correctly parses repeated `date:`/`text:` pairs (Plan: P6; Reqs: R6)
17. [x] Skip invalid entries with non-fatal warnings (Plan: P6; Reqs: R7)

## Phase 6 — Tests

18. [x] Add unit tests for config split with `---` (Plan: P7; Reqs: R2, R9)
19. [x] Add unit tests for `type=H` (horizontal) and `type=V` (vertical) (Plan: P7; Reqs: R1, R3, R9)
20. [x] Add unit tests for invalid `type` -> default vertical with warning (Plan: P7; Reqs: R3, R7, R9)
21. [x] Add tests asserting structural SVG differences H vs V (Plan: P8; Reqs: R4, R5, R9, R12)

## Phase 7 — Documentation & Examples

22. [x] Update `src/docs/asciidoc/timelineshowcase.adoc` to show config block, `type=H` vs `type=V`, and default behavior (Plan: P9; Reqs: R10)
23. [x] Add example payload in docs mirroring the provided sample (Plan: P9; Reqs: R10)

## Phase 8 — Performance & Accessibility

24. [x] Sanity-check performance/memory on typical timelines (Plan: P10; Reqs: R11)
25. [x] Validate text readability, wrapping/truncation, and non-overlap in horizontal layout (Plan: P11; Reqs: R12)
30. [x] Improve connector text visibility on dark backgrounds using luminance-based adaptive coloring (Plan: P3; Reqs: R4, R12)

## Phase 9 — Release Readiness

26. [x] Update CHANGELOG with feature entry "Horizontal timeline" (Plan: P9; Reqs: R10)
27. [x] Ensure backward compatibility note and migration guidance (defaults to vertical) (Plan: P4, P9; Reqs: R3, R5, R10)
28. [x] Final review and merge feature branch (Plan: P4, P5; Reqs: R5)
31. [x] Update Release Strategy visual style: replace generic fonts (Arial) with distinctive ones ('Plus Jakarta Sans', 'Outfit'), add background patterns, and enhance card aesthetics with glassmorphism and better gradients to match high-impact visual strategy (Plan: P3; Reqs: R4, R11)
32. [x] Improve Combination Chart aesthetics: switch to 'JetBrains Mono', implement Midnight IDE theme for dark mode, add dot background patterns, and staggered entry animations (Plan: P3; Reqs: R4, R11)
