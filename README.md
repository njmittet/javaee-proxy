javaee-proxy
============

Simple Java EE 7 HTTP reverse proxy application. Use the Dockerfile to
create a Docker container running the applicaton in a [Wildfly 10.0.0.Final](http://wildfly.org/downloads/) instance.

Supported methods
-----------------

* GET

Build
-----

The Application must be built before the docker image:

    mvn clean install

Build the Docker image:

    docker build -t javaee-proxy .

Run
---

Run in Docker:

    docker run -it --rm --link $BACKEND_IMAGE:$BACKEND_NAME -p 8080:8080 --name javaee-proxy -e 'PROXY_HOST=$BACKEND_NAME' -e 'PROXY_PORT=$BACKEND_EXPOSED_PORT' javaee-proxy

Backend
-------

[njmittet/spring-rest-api](https://github.com/njmittet/spring-rest-api) is a suitable
backend REST applicaton.
