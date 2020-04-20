# Supermarkt Frontend

This repository contains all code for our hackatum supermarket challenge.

## Deployment

Deployment is done via docker.

```bash
$ docker build -t hackatum-frontend:latest .
$ docker run -p 8080:8080 hackatum-frontend:latest
```
