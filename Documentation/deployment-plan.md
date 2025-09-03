# Deployment Plan

_Last updated: 2025-08-31_

This document outlines a recommended deployment pattern for the Inventory Management System: container images, Kubernetes/Helm deployment, secrets, observability, and rollout strategy.

## Target Platform & Tools
- Containers: Docker
- Orchestration: Kubernetes (EKS/AKS/GKE or on-prem)
- Helm charts per service + umbrella chart
- Ingress: NGINX or cloud LB + cert-manager
- Secrets: External Secrets Operator (Vault/KMS)
- Observability: Prometheus, Grafana, Loki, Jaeger

## Key Deployment Practices
- Semantic image tags and immutability; include SBOM
- Readiness/liveness probes, resource requests/limits, PodDisruptionBudgets
- Canary or progressive rollout with monitoring of SLOs

## Database & Migrations
- MySQL 8.0+ (InnoDB), managed or cluster operator
- Use Flyway for migrations; never edit applied migrations in production — apply new forward migrations
- Backup and PITR strategy: binlog archive to object storage

## Network & Security
- Enforce network policies and minimal egress/ingress
- mTLS recommended between services (service mesh optional)
- OPA/Gatekeeper policies for admission controls

## CI/CD Workflow
- Build, test, sign images in CI; publish to registry
- Helm package & publish chart; deploy to staging then prod with approvals
- Run smoke tests and synthetic transactions after deploy

## DB Connection Fixes
- Small DB-level fixes (connection strings, user grants) and connection troubleshooting notes have been consolidated here from `DATABASE_CONNECTION_FIX.md` so operators have one place to look for DB connection steps and fixes.
