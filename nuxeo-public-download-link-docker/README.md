# nuxeo-public-download-link Docker Image

This module is responsible for building the nuxeo-public-download-link Docker image.

Building the image requires a valid registration [CLID](https://doc.nuxeo.com/nxdoc/docker-image/#nuxeo_clid) which must be set as an environment variable before running maven. 

```bash
export NUXEO_CLID=<MY_CLID>
mvn clean install
```

A docker-compose file is provided to easily run the image.

```bash
export NUXEO_CLID=<MY_CLID>
docker-compose up
```

It's possible to skip Docker build by setting default `skipDocker` property value to `true` in `pom.xml` file.

```bash
# Skipping Docker build
mvn -DskipDocker=true clean install
```
