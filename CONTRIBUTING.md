# Contributing

## Requirements
- Java 17+
- Node.js 20+
- Docker Desktop

## Development Workflow
1. Run backend checks: `mvn -f api/pom.xml clean verify`
2. Run frontend checks: `cd web && npm run lint && npm run test && npm run build`
3. Verify compose startup (local or prod profile).

## Commit Convention
Use conventional commits, for example:
- `feat(api): add paginated audit log endpoint`
- `fix(web): harden callback error handling`
- `chore(ci): add trivy scan step`

## Pull Requests
- Keep changes scoped.
- Include tests for behavior changes.
- Update docs and `.env` examples when introducing config.
- Do not commit real secrets.
