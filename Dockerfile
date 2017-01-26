FROM njmittet/alpine-wildfly:10.1.0.Final
COPY target/javaee-proxy.war /opt/jboss/wildfly/standalone/deployments/javaee-proxy.war
COPY docker/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml