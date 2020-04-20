# Supermarket Backend

This repository contains all code for the backend of our hackatum project.

## Requirements

- Java >= 1.8
- mvn
- mariaDb

## Development

Setting up development is straight forward.

```bash
$ git clone ssh://git@repo.goma-cms.org:7999/tumsupermarket/backend.git && cd backend
$ mvn depdency:resolve
```

Then you can import the project into your IDE of choice and start development.


## Deployment

Deployment is done via docker. To build the docker-container run:

```bash
$ mvn clean package # build jar file with dependencies
$ docker build -t hackatum-backend:latest . # build docker image including jar file
$ docker run hackatum-backend:latest # run the docker file
```
