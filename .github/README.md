# MicroMart - GitHub Actions CI/CD

This directory contains GitHub Actions workflows for continuous integration and deployment.

## Workflows Overview

```
.github/
├── workflows/
│   ├── ci.yml           # Build and test on push/PR
│   ├── cd.yml           # Deploy to AWS on merge to main
│   ├── docker-build.yml # Build and publish Docker images
│   └── pr-checks.yml    # PR validation and labeling
├── labeler.yml          # PR auto-labeling configuration
└── README.md            # This file
```

## Workflow Details

### 1. CI Workflow (`ci.yml`)

**Triggers:**
- Push to `dev` branch
- Pull requests to `main` branch

**Jobs:**
| Job | Description |
|-----|-------------|
| `build-common` | Builds the common module (dependency for all services) |
| `build-services` | Matrix build for all microservices |
| `integration-tests` | Runs Testcontainers-based integration tests |
| `code-quality` | Checkstyle and SpotBugs analysis |
| `docker-build` | Validates Docker builds (without push) |

**Features:**
- Maven dependency caching
- Parallel service builds with matrix strategy
- Test result artifacts
- GitHub Actions job summary

### 2. CD Workflow (`cd.yml`)

**Triggers:**
- Push to `main` branch
- Manual dispatch with environment selection

**Jobs:**
| Job | Description |
|-----|-------------|
| `prepare` | Determines services to deploy and image tags |
| `build-and-push` | Builds and pushes Docker images to ECR |
| `deploy` | Deploys to EC2 via SSM |
| `post-deploy` | Creates deployment record |
| `rollback` | Automatic rollback on failure |

**Required Secrets:**
```
AWS_ACCOUNT_ID      - AWS account ID for ECR
AWS_ACCESS_KEY_ID   - AWS access key
AWS_SECRET_ACCESS_KEY - AWS secret key
```

**Environments:**
- `dev` - Development environment
- `staging` - Staging environment
- `prod` - Production environment (requires approval)

### 3. Docker Build Workflow (`docker-build.yml`)

**Triggers:**
- Push tags matching `v*` (e.g., `v1.0.0`)
- Manual dispatch with version input

**Features:**
- Multi-platform builds (amd64, arm64)
- Semantic versioning tags
- SBOM (Software Bill of Materials) generation
- Trivy security scanning
- Automatic GitHub releases

**Image Registry:** GitHub Container Registry (`ghcr.io`)

### 4. PR Checks Workflow (`pr-checks.yml`)

**Triggers:**
- Pull requests to `main` branch

**Jobs:**
| Job | Description |
|-----|-------------|
| `labeler` | Auto-labels PR based on changed files |
| `validate-title` | Enforces conventional commit format |
| `breaking-changes` | Detects API and database changes |
| `size-check` | Warns about large PRs |
| `dependency-check` | Checks for dependency updates |

## Setting Up Secrets

### GitHub Repository Secrets

Navigate to: Repository → Settings → Secrets and variables → Actions

**Required for AWS Deployment:**
```bash
# AWS credentials
AWS_ACCOUNT_ID=123456789012
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
```

### Environment Secrets

For environment-specific secrets, create GitHub Environments:
1. Go to Settings → Environments
2. Create `dev`, `staging`, `prod` environments
3. Add environment-specific secrets
4. Configure protection rules for `prod` (require reviewers)

## Branch Strategy

```
main (protected)
  ↑
  └── PR with reviews
        ↑
        dev (development)
          ↑
          └── feature branches
```

1. Create feature branches from `dev`
2. Open PR to merge into `dev` (triggers CI)
3. Open PR from `dev` to `main` (triggers CI + PR checks)
4. Merge to `main` triggers CD deployment

## Manual Deployment

To manually deploy a specific version:

1. Go to Actions → "CD - Deploy to AWS"
2. Click "Run workflow"
3. Select environment (`dev`, `staging`, `prod`)
4. Select services to deploy
5. Click "Run workflow"

## Creating a Release

1. Create and push a version tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. This triggers the Docker Build workflow which:
   - Builds all service images
   - Pushes to GitHub Container Registry
   - Creates a GitHub Release with SBOMs

## Troubleshooting

### CI Failures

**Common issues:**
- Maven cache miss → Re-run workflow
- Test flakiness → Check test logs in artifacts
- Testcontainers issues → Verify Docker service is available

### CD Failures

**Common issues:**
- ECR login failed → Check AWS credentials
- SSM command failed → Verify EC2 instance is running
- Health check failed → Check service logs via CloudWatch

### View Logs

```bash
# SSH to EC2
aws ssm start-session --target <instance-id>

# View service logs
docker logs user-service
docker logs product-service
docker logs order-service

# View all logs
docker-compose logs -f
```

## Best Practices

1. **Keep PRs small** - Easier to review and less risky
2. **Write meaningful commit messages** - Follow conventional commits
3. **Don't skip tests** - They exist for a reason
4. **Review security scan results** - Fix critical vulnerabilities
5. **Use environment protection** - Require approvals for prod
