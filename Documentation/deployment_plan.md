# Deployment Plan — Modern Store Inventory Management System (Kubernetes, Helm, MySQL)
_Last updated: 2025-08-31 12:28 UTC_

## Target Platform
- **Containers:** Docker images per service
- **Orchestration:** Kubernetes (EKS/AKS/GKE or on‑prem), namespaces per env (`dev`, `staging`, `prod`)
- **Package Management:** Helm charts (one per service + umbrella chart)
- **Ingress:** NGINX Ingress (or Cloud LB) + cert-manager (Let's Encrypt)
- **Service Mesh (optional):** Istio/Linkerd for mTLS and traffic shaping
- **Secrets:** External Secrets Operator (AWS/GCP/Azure KMS/Vault)
- **Observability:** Prometheus, Grafana, Loki, Tempo/Jaeger, Alertmanager
- **CI/CD:** GitHub Actions/GitLab CI with environments & approvals

## Kubernetes Layout
```
namespaces:
  - inventory-dev
  - inventory-stg
  - inventory-prd
components:
  - api-gateway
  - catalog-svc
  - inventory-svc
  - purchasing-svc
  - pricing-svc
  - order-svc
  - notify-svc
  - edge-svc
  - mysql-cluster
  - kafka / rabbitmq
  - prometheus / grafana / loki / jaeger
  - ingress-nginx / cert-manager
```

## Image & Release Standards
- Semantic version tags (`1.3.0`), immutable digests; SBOM included.
- Base images scanned; distroless where possible.
- Helm values per env: resources, HPA, autoscaling thresholds, secrets references.

---

## Feature-by-Feature Deployment Plan

### 1) Architecture & Platform
- Deploy **api-gateway** and core services via Helm with `readiness/liveness` probes, PodSecurity, and NetworkPolicies.
- Shared libraries baked into images; sidecars for logging/metrics where needed.

### 2) Multi‑Store, Multi‑Channel, Multi‑Tenant
- Enforce tenant scoping via OIDC claims at gateway layer.
- Configure per-tenant limits (rate limits/quotas) with API gateway or service mesh.
- Channel adapters deployed as separate deployments with dedicated scaling.

### 3) Item & Catalog Management
- catalog-svc: 2–3 replicas min, HPA based on CPU+RPS.
- Optional OpenSearch deployed via operator; index lifecycle policy for retention.
- Ingress routes `/catalog/**` to catalog-svc.

### 4) Stock Control & Movements
- inventory-svc requires low latency to MySQL; use **node affinity** to DB nodes if self-managed.
- Configure **priority class** to protect from eviction; enable PDB for at least 1 running replica.
- Kafka topic partitions sized for ledger throughput; idempotent consumers enabled.

### 5) Purchasing & Replenishment
- purchasing-svc with scheduled jobs (K8s CronJobs) for forecasting/replenishment proposals.
- Use WorkQueue and DLQ topics for failed computations.

### 6) Pricing & Promotions
- pricing-svc with config maps for rules; reload via Spring Cloud Kubernetes.
- Cache layer (Redis) as managed add-on; set eviction policies and persistence (AOF).

### 7) Barcoding, Scanning & RFID
- notify/label services with PDF rendering container; CPU bursts allowed via limits.
- Mobile edge endpoints exposed via API gateway; enable CORS as required.
- RFID adapters consume from IoT/MQ broker; deploy as autoscaled consumers.

### 8) Orders, Reservations & Fulfillment
- order-svc with strict HPA and **resilience policies** (retry/circuit breaker) in mesh.
- Reservations use single-writer pattern or MySQL cluster group replication to avoid split-brain.

### 9) Data Quality & Governance
- Deploy data-quality jobs as CronJobs; quarantine S3/PVC for invalid files.
- Audit sinks shipped to Loki + archive to object storage with lifecycle rules.

### 10) Security & Compliance
- Enforce mTLS (mesh) and NetworkPolicies; limit egress.
- OPA/Gatekeeper constraints: allowed registries, required labels, non-root users.
- Secrets via External Secrets referencing cloud KMS; rotate keys on schedule.

### 11) Observability & Reliability
- Prometheus Operator for scraping; ServiceMonitors per service.
- Dashboards for latency, error rates, inventory lag; SLOs in Grafana.
- Chaos tests in staging (pod kill, network delay); verify error budgets.

### 12) Reporting, Analytics & Insights
- Debezium or CDC agents deployed adjacent to MySQL to stream to Kafka.
- ETL jobs (Argo/Temporal/Airflow) on schedule; cost control via resource requests/limits.

### 13) Extensibility & Integration
- Webhook gateway with rate limits and signature verification.
- SFTP gateway as StatefulSet with PVC; ClamAV sidecar for file scanning.

### 14) User Experience (Web & Mobile)
- Admin UI deployed as static assets behind CDN; origin Ingress to `admin-ui` service.
- Mobile API rate limits; WAF/Bot protection at ingress layer.

### 15) Performance & Scalability Targets
- Autoscaling: HPA (CPU/RPS), VPA in staging for recommendations.
- Horizontal sharding for high‑volume services; partition Kafka topics accordingly.
- Canary deploys with mesh traffic split (e.g., 5%/25%/100%).

### 16) Testing & Quality
- CI: unit/integration, contract tests; image scan gates.
- CD: staged rollouts with manual approval in `prd`.
- Synthetic probes test receive → adjust → reserve → ship flows after each deploy.

### 17) DevSecOps & Delivery
- Helmfile/Argo CD for GitOps optionally; drift detection alerts.
- Supply chain security: cosign image signing; provenance attestations (SLSA).

### 18) Data Model (Minimum Entities)
- MySQL deployed as **InnoDB Cluster** (3+ nodes) via Operator (e.g., Oracle MySQL Operator or Percona).
- Storage: SSD-backed PVs; backup via XtraBackup; PITR with binlogs to object storage.
- ProxySQL/HAProxy for read/write routing; k8s Service for app connectivity.

### 19) Accessibility & Internationalization
- i18n bundles shipped with admin UI; locale negotiated at gateway; CDN edge caching by locale.
- A11y audits run in CI (Pa11y/Lighthouse) and reported to Grafana via webhooks.

### 20) Governance Checklists (Go‑Live Readiness)
- Run **capacity tests** and confirm p95 targets.
- Security review: pen test findings closed; SBOM published.
- DR test: restore from backups; RTO/RPO validated.
- Incident runbooks published; on‑call rotations configured in Alertmanager/PagerDuty.

---

## Deployment Workflow (End-to-End)
1. **Build:** CI compiles, tests, generates SBOM, signs images.
2. **Publish:** Push images to registry; Helm chart version bump.
3. **Staging:** Helm upgrade `-f values-stg.yaml`; run smoke + synthetic E2E; canary if mesh present.
4. **Production:** Helm upgrade `-f values-prd.yaml`; progressive traffic shift; monitor SLOs; auto‑rollback on error budget burn.
5. **Post-Deploy:** Capture release notes; tag Git; archive Helm values; trigger data quality jobs.

## Security Posture
- Pod Security Admission: `baseline`/`restricted` as appropriate.
- NetworkPolicies default‑deny; allow minimal intra‑service ports.
- Secrets never logged; audit sidecars redact sensitive fields.
