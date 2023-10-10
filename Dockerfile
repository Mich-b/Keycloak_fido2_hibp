FROM maven:3.5-jdk-8-alpine AS build
WORKDIR /opt/build
COPY hibp/pom.xml pom.xml
RUN mvn install
COPY hibp/src src
RUN mvn clean package

FROM jboss/keycloak:12.0.4
COPY --from=build /opt/build/target/keycloak-hibp-policy*.jar /opt/jboss/keycloak/standalone/deployments/hibp-password-policy.jar
COPY --chown=jboss:root secrets/keycloak.jks /opt/jboss/keycloak/standalone/configuration/
COPY --chown=jboss:root secrets/standalone-ha.xml /opt/jboss/keycloak/standalone/configuration/
