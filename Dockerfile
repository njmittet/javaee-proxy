FROM njmittet/alpine-wildfly:10.0.0.Final
ADD target/javaee-proxy.war /opt/jboss/wildfly/standalone/deployments/javaee-proxy.war
