FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app

COPY certs/russian_trusted_root_ca.crt /usr/local/share/ca-certificates/russian_trusted_root_ca.crt
COPY certs/russian_trusted_sub_ca.crt /usr/local/share/ca-certificates/russian_trusted_sub_ca.crt
RUN set -eux; \
    update-ca-certificates; \
    keytool -delete -alias russian-trusted-root-ca \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit 2>/dev/null || true; \
    keytool -delete -alias russian-trusted-sub-ca \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit 2>/dev/null || true; \
    keytool -importcert -noprompt -trustcacerts \
        -alias russian-trusted-root-ca \
        -file /usr/local/share/ca-certificates/russian_trusted_root_ca.crt \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit; \
    keytool -importcert -noprompt -trustcacerts \
        -alias russian-trusted-sub-ca \
        -file /usr/local/share/ca-certificates/russian_trusted_sub_ca.crt \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit

RUN mkdir -p /opt/app/data/uploads
COPY --from=build /app/target/miniapp-max-1.0.0.jar /opt/app/app.jar

EXPOSE 8080

ENV SERVER_PORT=8080 \
    APP_DB_PATH=/opt/app/data/app.db \
    APP_UPLOADS_DIR=/opt/app/data/uploads \
    APP_PUBLIC_BASE_URL=http://localhost:8080 \
    APP_BOOTSTRAP_ADMIN_ID=1

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
