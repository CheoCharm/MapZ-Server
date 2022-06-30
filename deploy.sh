#!/usr/bin/env bash

PROFILE = $1
DOCKER_ID = $2
DOCKER_PASSWORD = $3

echo ">>start build project"

sh gradlew clean build

echo ">>project build succeed"

TIMESTAMP = $(date +%s)
DOCKER_TAG=latest-${PROFILE}-${TIMESTAMP}
DOCKER_IMAGE=MapZ-${PROFILE}:${DOCKER_TAG}

sudo docker build -t ${DOCKER_IMAGE} --build-arg SPRING_ENV=${PROFILE} --platform linux/amd64 .
echo ">>docker build succeed"

sudo docker login -u ${DOCKER_ID} -p ${DOCKER_PASSWORD}
echo ">>docker login succeed"

sudo docker push ${DOCKER_IMAGE}
echo ">>docker push succeed"

sudo docker stop $(docker ps -a -q)
sudo docker rm $(docker ps -a -q)

sudo docker run -d -p 8080:8080 -e "SPRING_ENV=${PROFILE}" --name=MapZ ${DOCKER_IMAGE}



