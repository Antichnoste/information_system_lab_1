FROM quay.io/wildfly/wildfly:35.0.1.Final-jdk17
COPY --chown=jboss:root build/libs/ROOT.war /opt/jboss/wildfly/standalone/deployments/ROOT.war
