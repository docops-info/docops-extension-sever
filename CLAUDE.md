# CLAUDE.md

## Project Overview

**DocOps Extension Server** is a Spring Boot microservice that generates SVG-based documentation visualizations. It serves as a backend for AsciiDoc/documentation pipelines, providing 25+ visualization types including charts, diagrams, timelines, badges, and more.

- **Version:** 2026.01
- **Port:** 8010
- **Context Path:** `/extension`

## Build System

**Maven** with Kotlin plugin.

```bash
mvn clean package       # Build JAR
mvn spring-boot:run     # Run locally
mvn test                # Run tests
mvn process-aot         # AOT compilation (GraalVM)
```

**NPM** for CSS preprocessing (Tailwind CSS + PostCSS):

```bash
npm run minify-css-global     # Minify global styles
npm run minify-css-pro        # Minify pro styles
npm run minify-css-brutalist  # Minify brutalist styles
```

**Docker:**

```bash
docker build -t docops-extension-server .
docker run -p 8010:8010 docops-extension-server
```

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3.10 (JVM 17 target) |
| Framework | Spring Boot 4.0.2 |
| Templating | FreeMarker (.ftlh) |
| Serialization | Kotlinx Serialization 1.8.1 |
| Diagrams | PlantUML 1.2026.1 |
| Docs | AsciiDoctorJ 3.0.1 |
| CSS | Tailwind CSS 4.1.8 |
| Observability | Spring Actuator + OpenTelemetry |
| Testing | JUnit 5 + Spring Boot Test |

No database. No message queue. Stateless design.

## Project Structure

```
src/main/kotlin/io/docops/docopsextensionssupport/
├── adr/           # Architecture Decision Records
├── aop/           # Timing, metrics, tracing aspects
├── badge/         # Badge/shield generation
├── button/        # Interactive buttons
├── cal/           # Calendar visualization
├── callout/       # Callout boxes
├── chart/         # Bar, line, pie, gauge, quadrant, combo charts
├── diagram/       # Mermaid, PlantUML, connectors, treechart
├── domainviz/     # Domain model visualization
├── gherkin/       # BDD specification rendering
├── metricscard/   # Metrics cards
├── releasestrategy/ # Release timeline
├── roadmap/       # Roadmap/planner
├── scorecard/     # Scorecard generation
├── support/       # ThemeFactory, color utilities
├── svgsupport/    # SVG utilities, PNG conversion
├── swimlane/      # Swimlane diagrams
├── timeline/      # Timeline visualization
├── todo/          # Todo list visualization
├── treemap/       # Treemap visualization
├── util/          # Parsing, URL utilities
├── vcard/         # Virtual card generation
├── web/           # REST controllers and routing
└── wordcloud/     # Word cloud generation

src/main/resources/
├── application.yaml   # Spring configuration
├── templates/         # FreeMarker UI templates
├── static/            # CSS, JS, gallery examples
├── gallery/           # Sample data for generators
└── samples/           # Sample JSON configurations
```

## Key Files

| File | Purpose |
|---|---|
| `web/DocOpsRouter.kt` | Central router — dispatches all `/api/docops/svg` requests to handlers |
| `web/DocOpsHandler.kt` | Handler interface (`handleSVG()`) |
| `web/BaseDocOpsHandler.kt` | Abstract base with logging |
| `web/MainController.kt` | Template-based views (50+ HTML endpoints) |
| `support/ThemeFactory.kt` | Theme selection by name and version |
| `aop/AutoTimingConfiguration.kt` | AOP-based timed metrics |

## API

**Primary endpoint:**
```
POST /extension/api/docops/svg
```

Parameters:
- `payload` — compressed/encoded data string
- `kind` — handler type (e.g., `ADR`, `Badge`, `Button`, `BarChart`, `PieChart`, `Timeline`, etc.)
- `scale` — optional scale factor
- `dark` — optional dark mode flag
- `theme` — optional theme name

## Architecture Patterns

- **Factory/Strategy:** `DocOpsRouter` instantiates handlers by `kind`; `ThemeFactory` returns theme implementations
- **Template Method:** `BaseDocOpsHandler` defines the contract; subclasses implement SVG generation
- **AOP:** `@Timed` and `@Counted` on handlers for metrics; `TraceIdFilter` for distributed tracing
- **Event-Driven:** `DocOpsExtensionEvent` published per generation for metrics tracking
- **Compression:** Payloads are compressed/URL-encoded for transmission

## Theme System

`ThemeFactory` supports multiple visual themes with light/dark variants:
- Classic, Modern, Cyber, Pro, Brutalist, Hex
- Version-based strategy for backward compatibility

## Testing

Tests live in `src/test/kotlin/`. Run with:

```bash
mvn test
```

Test classes:
- `DocopsExtensionsSupportApplicationTests` — context load smoke test
- `CombinationChartImprovedTest` — chart rendering
- `VBarMakerTest` — bar chart
- `PlannerMakerTest` — planner
- `ScoreCardMakerTest` — scorecard

## Logging

File-based logging to `docops-extension-service.log` (configured in `application.yaml`).

## Observability

- Spring Boot Actuator enabled
- Custom metrics at `/extension/actuator/docopsstats`
- OpenTelemetry integration available (disabled by default)
