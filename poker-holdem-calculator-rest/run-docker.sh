#!/bin/bash
docker run -d --name holdem-calc-rest -p 127.0.0.1:8081:8080 --memory=256m --env WORKER=10 --env WORKER_CLASS=sync holdem-calc-rest:latest
