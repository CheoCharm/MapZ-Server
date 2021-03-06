#!/usr/bin/env bash

PROFILE=$1
DOCKER_ID=$2
DOCKER_PASSWORD=$3

TIMESTAMP=$(date +%s)
DOCKER_TAG=latest-${PROFILE}-${TIMESTAMP}
DOCKER_IMAGE=mapz/mapz-backend-prod:${DOCKER_TAG}

sudo docker login -u ${DOCKER_ID} -p ${DOCKER_PASSWORD}
echo ">>docker login succeed"

sudo docker build -t ${DOCKER_IMAGE} --build-arg SPRING_ENV=${PROFILE} --platform linux/amd64 .
echo ">>docker build succeed"

# Now no use this command
#sudo docker push ${DOCKER_IMAGE}
#echo ">>docker push succeed"
#
#sudo docker stop $(sudo docker ps -aq)
#sudo docker rm $(sudo docker ps -aq)
#
#sudo docker run -d -p 8080:8080 -e "SPRING_ENV=${PROFILE}" --name=MapZ ${DOCKER_IMAGE}



