# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS deps
WORKDIR /workspace
COPY api/pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline

FROM deps AS build
COPY api/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy AS runtime
RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /app
COPY --from=build /workspace/target/idp-0.0.1-SNAPSHOT.jar /app/idp.jar
COPY api/api-entrypoint.sh /app/api-entrypoint.sh
RUN chmod +x /app/api-entrypoint.sh
USER app
EXPOSE 9000
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=5 CMD wget -qO- http://127.0.0.1:9000/actuator/health || exit 1
ENTRYPOINT ["/app/api-entrypoint.sh"]
