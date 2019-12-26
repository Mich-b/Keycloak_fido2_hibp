FROM jboss/keycloak
COPY --chown=jboss:root secrets/keystore.jks /opt/jboss/keycloak/standalone/configuration/
COPY --chown=jboss:root secrets/standalone-ha.xml /opt/jboss/keycloak/standalone/configuration/
