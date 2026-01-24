# Domain Visualization

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #059669 0%, #10b981 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/DomainVisualizationIcon.svg" alt="Domain Visualization Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #059669; font-size: 32px;">DocOps Domain Visualization</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Map your technology landscape with layered domain architecture diagrams</p>
    </div>
  </div>
</div>

[TOC]

## What is Domain Visualization?

Domain Visualization creates structured, hierarchical views of your technology ecosystem, organizing components into logical layers and categories. Perfect for documenting platform architectures, technology stacks, and system landscapes with clarity and visual appeal.

### Key Features

- **Layered Architecture** - Organize components into logical domain layers (Common, Ingestion, Processing, etc.)
- **Interactive Links** - Each component can link to detailed documentation or resources
- **Emoji Support** - Use emojis to create visual category identifiers
- **Multiple Rows** - Group related components within each domain layer
- **Flexible Layout** - Automatically arranges components for optimal readability
- **Theme Support** - Choose between classic and neural network-inspired designs
- **Clickable Nodes** - Direct navigation to wikis, repositories, or documentation

<div style="background: #d1fae5; border-left: 4px solid #10b981; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #065f46; font-weight: 600;">🏗️ Architecture Communication</p>
  <p style="margin: 8px 0 0 0; color: #047857;">Perfect for system architects, platform engineers, and technical leads who need to communicate complex technology landscapes to diverse audiences.</p>
</div>

---

## Enterprise Data Platform

A comprehensive data platform showing all layers from ingestion through ML/AI:

[docops:domain]
main,ENTERPRISE DATA PLATFORM

type,emoji,rowIndex,nodes
COMMON,,0,"[[https://keycloak.org Single Sign-On]];[[https://wiki.company.com/governance Data Governance]];[[https://wiki.company.com/privacy Privacy Controls]]"
COMMON,,1,"[[https://grafana.company.com Monitoring]];[[https://wiki.company.com/lineage Data Lineage]];[[https://wiki.company.com/catalog Data Catalog]]"
INGESTION,📥,0,"[[https://kafka.apache.org Apache Kafka]];[[https://pulsar.apache.org Apache Pulsar]];[[https://nifi.apache.org Apache NiFi]]"
INGESTION,📥,1,"[[https://airbyte.com Airbyte]];[[https://fivetran.com Fivetran]];[[https://wiki.company.com/api API Connectors]]"
INGESTION,📥,2,"[[https://wiki.company.com/batch Batch Jobs]];[[https://wiki.company.com/streaming Stream Processing]];[[https://wiki.company.com/cdc Change Data Capture]]"
STORAGE,💾,0,"[[https://snowflake.com Snowflake]];[[https://databricks.com Delta Lake]];[[https://iceberg.apache.org Apache Iceberg]]"
STORAGE,💾,1,"[[https://aws.amazon.com/s3 Amazon S3]];[[https://hadoop.apache.org HDFS]];[[https://wiki.company.com/cache Redis Cache]]"
PROCESSING,⚡,0,"[[https://spark.apache.org Apache Spark]];[[https://flink.apache.org Apache Flink]];[[https://beam.apache.org Apache Beam]]"
PROCESSING,⚡,1,"[[https://dbt.com dbt Core]];[[https://wiki.company.com/etl Custom ETL]];[[https://airflow.apache.org Apache Airflow]]"
ANALYTICS,📊,0,"[[https://tableau.com Tableau]];[[https://powerbi.microsoft.com Power BI]];[[https://superset.apache.org Apache Superset]]"
ANALYTICS,📊,1,"[[https://jupyter.org Jupyter Notebooks]];[[https://rstudio.com RStudio]];[[https://wiki.company.com/reports Custom Reports]]"
ML/AI,🤖,0,"[[https://mlflow.org MLflow]];[[https://kubeflow.org Kubeflow]];[[https://wiki.company.com/experiments Experiment Tracking]]"
ML/AI,🤖,1,"[[https://tensorflow.org TensorFlow]];[[https://pytorch.org PyTorch]];[[https://scikit-learn.org Scikit-learn]]"
ML/AI,🤖,2,"[[https://wiki.company.com/models Model Registry]];[[https://wiki.company.com/serving Model Serving]];[[https://wiki.company.com/monitoring ML Monitoring]]"
[/docops]

---

## Neural Theme Variant

The same architecture with a modern neural network-inspired visual design:

[docops:domain]
main,ENTERPRISE DATA PLATFORM,useNeural=true

type,emoji,rowIndex,nodes
COMMON,,0,"[[https://keycloak.org Single Sign-On]];[[https://wiki.company.com/governance Data Governance]];[[https://wiki.company.com/privacy Privacy Controls]]"
COMMON,,1,"[[https://grafana.company.com Monitoring]];[[https://wiki.company.com/lineage Data Lineage]];[[https://wiki.company.com/catalog Data Catalog]]"
INGESTION,📥,0,"[[https://kafka.apache.org Apache Kafka]];[[https://pulsar.apache.org Apache Pulsar]];[[https://nifi.apache.org Apache NiFi]]"
INGESTION,📥,1,"[[https://airbyte.com Airbyte]];[[https://fivetran.com Fivetran]];[[https://wiki.company.com/api API Connectors]]"
INGESTION,📥,2,"[[https://wiki.company.com/batch Batch Jobs]];[[https://wiki.company.com/streaming Stream Processing]];[[https://wiki.company.com/cdc Change Data Capture]]"
STORAGE,💾,0,"[[https://snowflake.com Snowflake]];[[https://databricks.com Delta Lake]];[[https://iceberg.apache.org Apache Iceberg]]"
STORAGE,💾,1,"[[https://aws.amazon.com/s3 Amazon S3]];[[https://hadoop.apache.org HDFS]];[[https://wiki.company.com/cache Redis Cache]]"
PROCESSING,⚡,0,"[[https://spark.apache.org Apache Spark]];[[https://flink.apache.org Apache Flink]];[[https://beam.apache.org Apache Beam]]"
PROCESSING,⚡,1,"[[https://dbt.com dbt Core]];[[https://wiki.company.com/etl Custom ETL]];[[https://airflow.apache.org Apache Airflow]]"
ANALYTICS,📊,0,"[[https://tableau.com Tableau]];[[https://powerbi.microsoft.com Power BI]];[[https://superset.apache.org Apache Superset]]"
ANALYTICS,📊,1,"[[https://jupyter.org Jupyter Notebooks]];[[https://rstudio.com RStudio]];[[https://wiki.company.com/reports Custom Reports]]"
ML/AI,🤖,0,"[[https://mlflow.org MLflow]];[[https://kubeflow.org Kubeflow]];[[https://wiki.company.com/experiments Experiment Tracking]]"
ML/AI,🤖,1,"[[https://tensorflow.org TensorFlow]];[[https://pytorch.org PyTorch]];[[https://scikit-learn.org Scikit-learn]]"
ML/AI,🤖,2,"[[https://wiki.company.com/models Model Registry]];[[https://wiki.company.com/serving Model Serving]];[[https://wiki.company.com/monitoring ML Monitoring]]"

[/docops]

---

## Technology Stack Overview

A simplified view of technology layers showing frontend, backend, and infrastructure:

[docops:domain]
main,TECHNOLOGY

type,emoji,rowIndex,nodes
COMMON,,0,"FRONTEND,BACKEND,DATABASE"
COMMON,,1,"DEVOPS,DOCOPS"
COMMON,,2,"SECURITY"
FRONTEND,🧑🏻‍💻,0,"FRONTEND,HTML,CSS,JAVASCRIPT,TYPESCRIPT,KOTLIN,SWIFT"
BACKEND,🧑🏻‍💻,1,"BACKEND,KOTLIN,JAVA,.NET,PYTHON,GO,RUST,RUBY,PHP"
[/docops]

---

## Architecture Governance

Document decision-making processes, architectural patterns, and review procedures:

[docops:domain]
main,ARCHITECTURE GOVERNANCE

type,emoji,rowIndex,nodes
COMMON,,0,"[[https://wiki.company.com/standards Coding Standards]];[[https://wiki.company.com/security Security Guidelines]];COMPLIANCE"
COMMON,,1,"[[https://wiki.company.com/dr Disaster Recovery]];[[https://monitoring.company.com Observability]];SLA MGMT"
DECISIONS,📋,0,"[[https://github.com/company/adrs/tree/main/database ADR-001 Database]];[[https://github.com/company/adrs/tree/main/auth ADR-002 Auth]];[[https://github.com/company/adrs/tree/main/deployment ADR-003 Deploy]]"
PATTERNS,🏗️,0,"[[https://microservices.io Event Sourcing]];[[https://martinfowler.com/eaaCatalog CQRS]];[[https://wiki.company.com/patterns Domain Patterns]]"
REVIEWS,👥,0,"[[https://github.com/company/rfc RFCs]];TECH REVIEWS;[[https://wiki.company.com/architecture Architecture Board]]"
[/docops]

---

## Product Platform

Showcase multi-channel product architecture with web, mobile, and data layers:

[docops:domain]
main,PRODUCT PLATFORM

type,emoji,rowIndex,nodes
COMMON,,0,"[[https://auth0.com Identity Provider]];OBSERVABILITY;[[https://wiki.company.com/security Security Hub]]"
COMMON,,1,"[[https://stripe.com Billing API]];NOTIFICATIONS;[[https://algolia.com Search Engine]]"
WEB,🧑🏻‍💻,0,"[[https://nextjs.org Next.js]];[[https://remix.run Remix]];SPA;[[https://cloudflare.com Edge Cache]]"
MOBILE,📱,0,"[[https://reactnative.dev React Native]];[[https://flutter.dev Flutter]];[[https://ionic.io Ionic]];CROSS-PLATFORM"
DATA,🧠,0,"[[https://snowflake.com Data Warehouse]];[[https://databricks.com Lakehouse]];[[https://feast.dev Feature Store]]"
[/docops]

---

## Format Structure

### CSV-Like Format

Domain visualizations use a CSV-style format with headers and data rows:

text main,TITLE HERE
type,emoji,rowIndex,nodes 

LAYER_NAME,🔧,0,"COMPONENT1;COMPONENT2;COMPONENT3" 
LAYER_NAME,🔧,1,"COMPONENT4;COMPONENT5" 
ANOTHER_LAYER,📊,0,"ITEM1;ITEM2;ITEM3;ITEM4"

### Component Anatomy

| Column | Description | Required | Example |
|--------|-------------|----------|---------|
| **type** | Domain layer name | Yes | `INGESTION`, `STORAGE`, `PROCESSING` |
| **emoji** | Visual category identifier | No | `📥`, `💾`, `⚡` |
| **rowIndex** | Row number within the layer (0-based) | Yes | `0`, `1`, `2` |
| **nodes** | Semicolon-separated list of components | Yes | `"Kafka;Pulsar;NiFi"` |

### Link Format

Use WikiLink syntax to create clickable components:

text [[URL Component Label]]


**Examples:**
- `[[https://kafka.apache.org Apache Kafka]]` - External link with label
- `[[https://wiki.company.com/api API Connectors]]` - Internal wiki link
- `COMPONENT_NAME` - Plain text (no link)

<div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #92400e; font-weight: 600;">💡 Organization Tip</p>
  <p style="margin: 8px 0 0 0; color: #b45309;">Start with COMMON layer for cross-cutting concerns like security, monitoring, and governance. Then organize domain-specific layers in logical flow order (e.g., Ingestion → Storage → Processing → Analytics).</p>
</div>

---

## Design Best Practices

### Layer Organization

- **COMMON First** - Place cross-cutting concerns at the top
- **Logical Flow** - Arrange layers to match data or process flow
- **Consistent Naming** - Use clear, descriptive layer names in UPPERCASE
- **Appropriate Granularity** - 3-8 layers is ideal; too many creates confusion

### Component Grouping

- **Related Items Together** - Group similar technologies in the same row
- **Multiple Rows per Layer** - Use rows to organize sub-categories or alternatives
- **Balanced Distribution** - Aim for 3-5 components per row for visual balance

### Emoji Usage

- **Category Identifiers** - Use emojis consistently to represent categories
- **Visual Scanning** - Helps users quickly identify layer types
- **Optional but Recommended** - Enhances visual appeal and usability
- **Common Choices**:
    - 📥 Ingestion
    - 💾 Storage
    - ⚡ Processing
    - 📊 Analytics
    - 🤖 ML/AI
    - 🔒 Security
    - 👥 Collaboration

### Link Strategy

- **External Documentation** - Link to official product pages and documentation
- **Internal Resources** - Connect to wiki pages, runbooks, and team docs
- **Source Control** - Link to repositories, ADRs, and RFCs
- **Mix Linked and Plain** - Not every component needs a link; use judiciously

---

## Theme Options

### Default Theme
Clean, professional appearance with subtle gradients and modern card design. Best for formal documentation and executive presentations.

### Neural Theme
Modern, tech-forward design with neural network-inspired connections and dynamic visual elements. Great for AI/ML platforms and cutting-edge technology showcases.

To use the neural theme, add `useNeural=true` parameter:

text main,PLATFORM NAME,useNeural=true

---

## Common Use Cases

### Platform Architecture
Document your entire platform stack from infrastructure to application layer, showing how components interconnect.

### Technology Radar
Create a living inventory of your technology choices, linked to ADRs and documentation.

### Onboarding Documentation
Help new team members understand your technology landscape with clear, visual architecture maps.

### Vendor Evaluation
Compare different technology options within each domain layer during architectural planning.

### Compliance & Governance
Show security controls, compliance tools, and governance processes across your architecture.

### Migration Planning
Document current and future state architectures during platform modernization efforts.

---

## Advanced Patterns

### Multi-Environment Views

Create separate domain visualizations for different environments (dev, staging, production) to show configuration differences.

### Team Ownership Mapping

Use domain layers to represent team boundaries, showing which team owns each set of components.

### Cost Center Breakdown

Organize by cost centers or departments to visualize technology spending across the organization.

### Maturity Levels

Use different rows within layers to indicate technology maturity (experimental, stable, deprecated).

---

## Integration with Other DocOps Components

### With ADRs
Link domain components directly to Architecture Decision Records that explain technology choices.

### With Buttons
Create navigation buttons that link to detailed domain visualization pages for each major platform area.

### With Connectors
Show high-level domains in visualization, then use connectors to illustrate detailed workflows within each layer.

### With Timelines
Document platform evolution by creating domain visualizations for different time periods.

---

## Complete Example with Best Practices

[docops:domain]
main,CLOUD NATIVE PLATFORM, useNeural=true

type,emoji,rowIndex,nodes 
COMMON,🔐,0,"[[https://vault.hashicorp.com Secrets Management]];[[https://keycloak.org SSO & Identity]];[[https://falco.org Runtime Security]]" 
COMMON,📊,1,"[[https://grafana.com Grafana Stack]];[[https://sentry.io Error Tracking]];[[https://wiki.company.com/sla SLA Dashboards]]" 
INGRESS,🌐,0,"[[https://nginx.org NGINX]];[[https://traefik.io Traefik]];[[https://cloudflare.com Cloudflare CDN]]" 
COMPUTE,🎯,0,"[[https://kubernetes.io Kubernetes]];[[https://knative.dev Knative]];[[https://aws.amazon.com/fargate AWS Fargate]]" 
COMPUTE,🎯,1,"[[https://argo-cd.readthedocs.io ArgoCD]];[[https://fluxcd.io FluxCD]];[[https://helm.sh Helm Charts]]" 
STORAGE,💾,0,"[[https://postgresql.org PostgreSQL]];[[https://redis.io Redis]];[[https://aws.amazon.com/s3 S3 Object Storage]]" 
STORAGE,💾,1,"[[https://elastic.co Elasticsearch]];[[https://mongodb.com MongoDB]];[[https://cassandra.apache.org Cassandra]]" 
MESSAGING,📨,0,"[[https://kafka.apache.org Kafka]];[[https://rabbitmq.com RabbitMQ]];[[https://aws.amazon.com/sqs AWS SQS]]" 
OBSERVABILITY,👁️,0,"[[https://prometheus.io Prometheus]];[[https://opentelemetry.io OpenTelemetry]];[[https://jaegertracing.io Jaeger]]" 
CI/CD,🚀,0,"[[https://github.com/features/actions GitHub Actions]];[[https://jenkins.io Jenkins]];[[https://spinnaker.io Spinnaker]]"
[/docops]

This example demonstrates:

- **Clear Layer Hierarchy** - From COMMON cross-cutting concerns through specialized domains
- **Consistent Emoji Usage** - Visual identifiers for each domain category
- **Balanced Component Distribution** - 3 components per row for optimal readability
- **Mix of Internal and External Links** - Official documentation and internal wiki pages
- **Logical Flow** - Organized from ingress through compute, storage, messaging, and operations
- **Multiple Rows per Layer** - COMMON has 2 rows, COMPUTE has 2 rows for sub-categories

---

<div align="center" style="margin-top: 48px; padding: 24px; background: #fafbfc; border-radius: 8px;">
  <p style="color: #64748b; margin: 0;">Ready to map your technology landscape?</p>
  <p style="color: #059669; font-weight: 600; margin: 8px 0 0 0;">Create clear, interactive domain visualizations with DocOps</p>
</div>