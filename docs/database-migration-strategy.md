# Database Migration Strategy

## Rules
- Schema is SQL-first and version-controlled in `api/src/main/resources/db/migration`.
- File naming is mandatory: `V{number}__description.sql`.
- Never edit an already applied migration in shared environments.
- Do not use Hibernate schema mutation (`ddl-auto` must stay `validate`).
- No manual production DDL outside Flyway.

## Creating a New Migration
1. Create a new file with next version number.
2. Include deterministic SQL only.
3. Prefer additive changes.
4. Add indexes and constraints in the same migration when required.
5. Run locally against an empty DB and current DB.

Example:
- `V3__add_payment_status_column.sql`
- `V4__add_booking_lookup_index.sql`

## Local Run
```bash
mvn -f api/pom.xml clean verify -DskipITs
mvn -f api/pom.xml spring-boot:run
```
Flyway runs at startup before JPA validation.

## Rollback Strategy
Flyway Community does not auto-rollback SQL migrations.
Use restore-based rollback:
1. Stop application.
2. Restore PostgreSQL backup to last known good point.
3. Deploy corrected migration as a forward fix.

## Handling Failed Migration
1. Fix SQL issue in a new migration (preferred), or repair only in isolated non-prod if safe.
2. Validate migration state:
```bash
mvn -f api/pom.xml flyway:info
```
3. If checksum mismatch occurred due accidental edit, revert file and re-deploy.
4. Never force manual schema drift in production.

## Release Checklist
- Empty DB bootstrap test passes.
- Existing DB migration test passes.
- `ddl-auto=validate` unchanged.
- Migration reviewed by another engineer.
