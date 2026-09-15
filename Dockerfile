FROM node:22-bookworm-slim AS frontend-build
WORKDIR /workspace
RUN corepack enable
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc tsconfig.base.json ./
COPY artifacts/support-platform/package.json ./artifacts/support-platform/package.json
COPY lib ./lib
COPY artifacts/support-platform ./artifacts/support-platform
RUN pnpm install --frozen-lockfile --ignore-scripts
RUN pnpm approve-builds esbuild || true
RUN pnpm rebuild esbuild
ENV PORT=4173
ENV BASE_PATH=/
RUN pnpm --filter ./artifacts/support-platform run build

FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY artifacts/api-server/spring-app/pom.xml ./pom.xml
RUN mvn -B -q dependency:go-offline
COPY artifacts/api-server/spring-app/src ./src
COPY --from=frontend-build /workspace/artifacts/support-platform/dist/public ./src/main/resources/static
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=backend-build /app/target/support-platform-api-*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
