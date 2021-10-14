#!/bin/bash

mvn clean install -DskipTests
docker-compose build
docker-compose up -d