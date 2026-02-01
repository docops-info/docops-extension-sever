# DocOps Visualization Documentation Patterns

## Streamlined Documentation Pattern

When documenting DocOps visualizations, follow this structure:

### 1. Hero Section
- Title + icon
- One-sentence purpose ("Metrics cards turn KPI tables into clean snapshots")
- Key features (3-5 bullet points focused on outcomes)

### 2. Default Example
- Show the most common use case first
- Use realistic, relatable data
- No configuration complexity

### 3. 2-3 Focused Examples
- Show variety (dark theme, different contexts)
- Each example should demonstrate one clear use case
- Prioritize visual diversity over feature completeness

### 4. Common Use Cases
- Brief bullet list of real-world scenarios
- Focus on **when** to use it, not **how**

### 5. Format Options (concise)
- Show the simplest format first
- One clear example per format type
- Common options only (useDark, scale, controls)
- Skip API documentation

### 6. Best Practices
- 4-6 actionable tips
- Focus on content quality, not technical details
- Add one tip callout box

## What to Remove from AsciiDoc
- ❌ "Basic Concepts" deep dives
- ❌ Component property tables
- ❌ Multiple syntax variations
- ❌ API endpoint documentation
- ❌ "Advanced Usage" sections
- ❌ More than 3-4 examples
- ❌ Lengthy "Interactive Features" sections

## What to Keep
- ✅ Visual examples
- ✅ Realistic data
- ✅ Clear use cases
- ✅ Simple format explanations
- ✅ Practical best practices
- ✅ Quick reference tables (when appropriate)

## Completed Streamlined Types (3/24)

### 1. ✅ Release Strategy (290 lines, 60% reduction)
- Product roadmaps with TLS/R layouts
- 3 examples: Product, Mobile App, Cloud Platform
- Best practice: Use Roadmap style with quarterly dates for multi-year plans

### 2. ✅ Quadrant Charts (195 lines, 41% reduction)
- 2D priority mapping for strategic decisions
- 3 examples: Strategic Matrix, Feature Prioritization, Risk Assessment
- Best practice: Label quadrants contextually (Quick Wins vs. Big Bets)

### 3. ✅ Gherkin (180 lines, streamlined from verbose original)
- BDD test scenarios with Given-When-Then
- 3 examples: Authentication, Shopping Cart, Search (dark mode)
- Best practice: Embed .feature files for single source of truth
- Special: JSON format with status indicators (PASSING/FAILING/PENDING/SKIPPED)

## Quick Reference: Remaining Types (6)

### Charts (6 types)
1. ✅ Bar Charts
2. ⏳ Pie Charts
3. ✅ Line Charts
4. ✅ Combination Charts
5. ✅ Gauge Charts
6. ✅ Metrics Cards

### Navigation & Organization (3 types)
7. ✅ Scorecards
8. ✅ Timeline
9. ✅ Buttons

### Process & Planning (4 types remaining)
10. ✅ Callouts
11. ✅ Planner (Kanban-style roadmaps)
12. ✅ Connectors (workflow arrows)
13. ✅ Release Strategy ← DONE
14. ✅ Quadrant ← DONE

### Technical & Architecture (3 types)
15. ✅ Word Clouds
16. ✅ Domain Visualization
17. ✅ Gherkin ← DONE
18. ⏳ Treemap/Treechart

### Metadata & Status (6 types)
19. ✅ Badges & Shields
20. ⏳ Placemat (label grids)
21. ⏳ Todo (task lists)
22. ⏳ VCard (contact cards)
23. ⏳ ADR (Architecture Decision Records)


## Universal Pattern Applied (3 types)
All completed types follow:
- Hero section with emoji + tagline
- Default example (most common case)
- 2-3 focused examples (variety showcase)
- 5 common use cases (when to use)
- Format options (simplest first)
- 6 best practices + 1 TIP callout

## Next Priority
Continue with remaining types in this order:
1. Placemat (Process & Planning category)
2. ADR (Navigation & Organization)
3. VCard Clouds (Technical & Architecture)
4. Todo (Metadata & Status)
5. Treemap
6. Treechart
7. Pie Charts (multi pies incomplete)