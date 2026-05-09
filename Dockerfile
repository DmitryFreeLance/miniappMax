FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app

RUN mkdir -p /opt/app/data/uploads
COPY --from=build /app/target/miniapp-max-1.0.0.jar /opt/app/app.jar

EXPOSE 8080

ENV SERVER_PORT=8080 \
    APP_DB_PATH=/opt/app/data/app.db \
    APP_UPLOADS_DIR=/opt/app/data/uploads \
    APP_PUBLIC_BASE_URL=http://localhost:8080 \
    APP_BOOTSTRAP_ADMIN_ID=1

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
