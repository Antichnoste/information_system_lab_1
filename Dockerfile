FROM quay.io/wildfly/wildfly:35.0.1.Final-jdk17
COPY --chown=jboss:root build/drivers/postgresql.jar /tmp/postgresql.jar
COPY --chown=jboss:root deploy/configure.cli /tmp/configure.cli

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure.cli \
    && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history
COPY --chown=jboss:root build/libs/ROOT.war /opt/jboss/wildfly/standalone/deployments/ROOT.war
