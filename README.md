javaee-proxy
============

Simple Java EE 7 HTTP reverse proxy application. Use the Dockerfile to
create a Docker container for the applicaton.

Supported methods
-----------------

* GET

Build
-----

The Application must be built before the docker image:

    mvn clean install

Build the Dokcer image:

    docker build -t javaee-proxy .

Backend
-------

[njmittet/spring-rest-api](https://github.com/njmittet/spring-rest-api) is a suitable
backend REST applicaton.
