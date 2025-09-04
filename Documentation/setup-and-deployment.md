git clone <your-repo-url> inventory-management-system
docker-compose up -d mysql redis
docker-compose logs -f mysql
mvn test
docker-compose ps mysql
docker-compose logs mysql
docker-compose restart mysql
mvn flyway:clean flyway:migrate
tail -f logs/inventory-management.log
docker-compose ps redis
docker exec -it inventory-redis redis-cli ping
mvn checkstyle:check
#### Catalog Service
- 2-3 replicas minimum, HPA based on CPU+RPS
- Optional OpenSearch deployed via operator
- Ingress routes `/catalog/**` to catalog-svc

#### Inventory Service
- Requires low latency to MySQL; use node affinity to DB nodes
- Configure priority class to protect from eviction
- Enable PodDisruptionBudget for at least 1 running replica
- Kafka topic partitions sized for ledger throughput

#### Purchasing Service
- Scheduled jobs (K8s CronJobs) for forecasting/replenishment
- Use WorkQueue and DLQ topics for failed computations

#### Pricing Service
- ConfigMaps for pricing rules; reload via Spring Cloud Kubernetes
- Redis cache layer as managed add-on with eviction policies

### 5. Security & Compliance
- Enforce mTLS via service mesh and NetworkPolicies
- OPA/Gatekeeper constraints for security policies
- Secrets via External Secrets referencing cloud KMS
- Regular key rotation on schedule

### 6. Observability & Monitoring
- Prometheus Operator for metrics scraping
- ServiceMonitors per service
- Dashboards for latency, error rates, inventory lag
- SLOs defined in Grafana with alerting

## Image & Release Standards

### Container Standards
- Semantic version tags (`1.3.0`) with immutable digests
- SBOM (Software Bill of Materials) included
- Base images scanned for vulnerabilities
- Distroless images where possible

### Release Management
- Helm values per environment: resources, HPA, autoscaling thresholds
- Secret references managed externally
- Immutable artifact promotion through environments

## Deployment Workflow

### End-to-End Pipeline

1. **Build Stage**
   - CI compiles, tests, generates SBOM
   - Signs images with cosign
   - Vulnerability scanning gates

2. **Publish Stage**
   - Push images to registry
   - Helm chart version bump
   - Supply chain security attestations

3. **Staging Deployment**
   ```bash
   helm upgrade inventory-app ./helm/inventory \
     -f values-staging.yaml \
     --namespace inventory-stg
   ```
   - Run smoke tests and synthetic E2E tests
   - Canary deployment if service mesh present

4. **Production Deployment**
   ```bash
   helm upgrade inventory-app ./helm/inventory \
     -f values-production.yaml \
     --namespace inventory-prd
   ```
   - Progressive traffic shift (5%/25%/100%)
   - Monitor SLOs and error budgets
   - Auto-rollback on threshold breach

5. **Post-Deploy**
   - Capture release notes
   - Tag Git repository
   - Archive Helm values
   - Trigger data quality jobs

### Environment-Specific Configurations

#### Staging Environment
```yaml
# values-staging.yaml
replicaCount: 2
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"

hpa:
  minReplicas: 2
  maxReplicas: 5
  targetCPUUtilizationPercentage: 70
```

#### Production Environment
```yaml
# values-production.yaml
replicaCount: 3
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"

hpa:
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 60

podDisruptionBudget:
  minAvailable: 2
```

## Performance & Scalability

### Autoscaling Strategy
- **HPA (Horizontal Pod Autoscaler)**: CPU and RPS-based scaling
- **VPA (Vertical Pod Autoscaler)**: Resource recommendation in staging
- Cluster autoscaling for node capacity management

### Traffic Management
- Canary deployments with traffic splitting
- Circuit breakers and retry policies in service mesh
- Rate limiting at ingress and service levels

### Data Optimization
- Horizontal sharding for high-volume services
- Kafka topic partitioning for parallel processing
- Database connection pooling and read replicas

## Security Posture

### Container Security
- Pod Security Admission: `baseline`/`restricted` profiles
- NetworkPolicies with default-deny rules
- Non-root container execution
- Read-only root filesystems where possible

### Secrets Management
- External Secrets Operator integration
- Key rotation automation
- Audit logging for secret access
- Secret scanning in CI/CD pipelines

### Network Security
- mTLS between services via service mesh
- Ingress-level TLS termination with cert-manager
- WAF/Bot protection at edge
- Egress filtering and monitoring

## Disaster Recovery & Business Continuity

### Backup Strategy
- MySQL: Daily full backups + continuous binlog shipping
- Application state: PVC snapshots
- Configuration: GitOps repository backups
- Cross-region backup replication

### Recovery Procedures
- **RTO (Recovery Time Objective)**: < 4 hours
- **RPO (Recovery Point Objective)**: < 15 minutes
- Automated failover for database clusters
- Infrastructure-as-code for rapid environment recreation

### Testing
- Monthly DR drills with full environment recreation
- Backup restoration validation
- Chaos engineering in staging environments

## Monitoring & Alerting

### Key Metrics
- Application metrics: request rates, latency, errors
- Business metrics: inventory accuracy, order fulfillment rates
- Infrastructure metrics: CPU, memory, disk, network
- Database metrics: connection pools, query performance

### Alert Thresholds
```yaml
# Critical alerts (immediate response)
- InventoryServiceDown > 30s
- DatabaseConnectionsExhausted > 5min
- ErrorRate > 5% for 2min

# Warning alerts (within business hours)
- HighLatency > 500ms for 5min
- LowInventoryAccuracy < 95% for 15min
```

### Dashboards
- Executive dashboard: business KPIs and system health
- Operations dashboard: service health and performance
- Developer dashboard: application metrics and logs

## Compliance & Governance

### Go-Live Readiness Checklist
- [ ] Capacity testing completed with p95 targets met
- [ ] Security review and penetration test findings resolved
- [ ] SBOM published and vulnerability scanning clear
- [ ] Disaster recovery procedures tested and validated
- [ ] Incident runbooks published and team trained
- [ ] On-call rotations configured in PagerDuty/Alertmanager
- [ ] Compliance requirements (SOC 2, GDPR) validated
- [ ] Performance benchmarks and SLOs established

### Audit & Compliance
- Comprehensive audit logging for all user actions
- Immutable audit trails with retention policies
- GDPR compliance for data export and deletion
- Regular security assessments and compliance reporting

## Useful Commands & References

### Development Commands
```bash
# View running containers
docker-compose ps

# Stop all services
docker-compose down

# View application logs
tail -f logs/inventory-management.log

# Database console access
mysql -h 127.0.0.1 -P 3307 -u inventory_user -p

# Redis console access
docker exec -it inventory-redis redis-cli
```

### Production Commands
```bash
# Check cluster status
kubectl get nodes
kubectl get pods -n inventory-prd

# View service logs
kubectl logs -f deployment/inventory-service -n inventory-prd

# Check ingress
kubectl get ingress -n inventory-prd

# Monitor resource usage
kubectl top pods -n inventory-prd
```

### Monitoring URLs
- **Local Development**:
  - Health check: http://localhost:8080/api/actuator/health
  - Metrics: http://localhost:8080/api/actuator/metrics
  - API docs: http://localhost:8080/api/swagger-ui.html

- **Production**:
  - Grafana: https://monitoring.inventory.example.com
  - Prometheus: https://prometheus.inventory.example.com
  - Kibana: https://logs.inventory.example.com

## Next Steps

### Development
1. **Set Initial Passwords**: Use password reset functionality for default users
2. **Create Additional Users**: Use admin interface for user management
3. **Configure Email**: Set up SMTP settings for notifications
4. **Customize Configuration**: Adjust password policies and security settings
5. **Implement Frontend**: Connect frontend applications to the API

### Production
1. **Infrastructure Setup**: Provision Kubernetes cluster and supporting services
2. **Security Hardening**: Implement security policies and compliance controls
3. **Monitoring Setup**: Deploy observability stack and configure alerts
4. **Load Testing**: Validate performance under expected load
5. **Go-Live Planning**: Execute deployment and cutover procedures

---

## Verification Checklist

After completing setup, verify these items:

**Local Development**:
- [ ] Docker containers are running (MySQL, Redis)
- [ ] Database connection is working on port 3307
- [ ] Flyway migrations completed successfully
- [ ] Spring Boot application starts without errors
- [ ] Health check endpoint returns UP status
- [ ] Swagger UI is accessible
- [ ] Can create and authenticate users
- [ ] API endpoints respond correctly

**Production Deployment**:
- [ ] All services deployed and healthy
- [ ] Ingress and TLS certificates configured
- [ ] Monitoring and alerting operational
- [ ] Load testing completed successfully
- [ ] Disaster recovery procedures validated
- [ ] Security scans passed
- [ ] Performance targets met
- [ ] Compliance requirements satisfied

**Congratulations!** Your Inventory Management System is ready for operation.

## References

This document consolidates information from:
- SETUP_INSTRUCTIONS.md
- deployment_plan.md