# Contributing to MicroMart

Thank you for considering contributing to MicroMart! This document outlines the guidelines for contributing to this project.

## Branch Workflow

We follow the GitFlow branching model:

```
main (production-ready code)
  │
  └── dev (integration branch)
        │
        └── feature/* (feature branches)
```

### Branch Types

| Branch | Purpose | Base Branch | Merge Into |
|--------|---------|-------------|------------|
| `main` | Production-ready code | - | - |
| `dev` | Integration branch | `main` | `main` |
| `feature/*` | New features | `dev` | `dev` |
| `bugfix/*` | Bug fixes | `dev` | `dev` |
| `hotfix/*` | Critical production fixes | `main` | `main` & `dev` |
| `release/*` | Release preparation | `dev` | `main` & `dev` |

## Getting Started

1. **Fork the repository** (if external contributor)
2. **Clone the repository**
   ```bash
   git clone https://github.com/Poojithvsc/micromart-project.git
   cd micromart-project
   ```
3. **Switch to dev branch**
   ```bash
   git checkout dev
   ```
4. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Commit Message Convention

We follow the Conventional Commits specification:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation changes |
| `style` | Code style changes (formatting, semicolons, etc.) |
| `refactor` | Code refactoring without feature/fix changes |
| `test` | Adding or modifying tests |
| `chore` | Maintenance tasks (build, CI, dependencies) |
| `perf` | Performance improvements |

### Scopes

Use the service name or component as scope:
- `user-service`
- `product-service`
- `order-service`
- `api-gateway`
- `eureka-server`
- `terraform`
- `docker`
- `ci`

### Examples

```bash
# Feature
feat(user-service): add JWT token refresh endpoint

# Bug fix
fix(order-service): resolve race condition in inventory check

# Documentation
docs(readme): update installation instructions

# Refactoring
refactor(product-service): extract price calculation to Money value object
```

## Pull Request Process

1. **Ensure your code builds** and all tests pass
   ```bash
   mvn clean verify
   ```

2. **Update documentation** if needed

3. **Create a Pull Request** with:
   - Clear title following commit convention
   - Description of changes
   - Link to related issues (if any)
   - Screenshots (for UI changes)

4. **PR Template**
   ```markdown
   ## Summary
   Brief description of changes

   ## Changes
   - Change 1
   - Change 2

   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Integration tests added/updated
   - [ ] Manual testing performed

   ## Related Issues
   Closes #123
   ```

## Code Style Guidelines

### Java

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Maximum line length: 120 characters
- Use constructor injection for dependencies
- Document public APIs with Javadoc

### Configuration

- Use YAML format for Spring configuration
- Externalize environment-specific values
- Never commit secrets or credentials

## Testing Requirements

- Unit tests for all service layer methods
- Integration tests for repositories
- API tests for controllers
- Minimum 80% code coverage for new code

## Local Development

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL (or use Docker)

### Running Locally

```bash
# Start infrastructure (PostgreSQL, Kafka)
docker-compose -f infrastructure/docker/docker-compose.yml up -d

# Build all modules
mvn clean install

# Run a specific service
cd user-service
mvn spring-boot:run
```

## Questions?

If you have any questions, feel free to open an issue or reach out to the maintainers.
