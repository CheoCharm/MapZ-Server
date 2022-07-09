#!/usr/bin/env bash

PROFILE=$1
DOCKER_IMAGE=$2

BLUE=$(sudo docker port mapz-blue)
GREEN=$(sudo docker port mapz-green)

if [ -n "$BLUE" ]; then
  IDLE_PORT=8081
  REMOVE_CONTAINER=mapz-blue
  IDLE_CONTAINER=mapz-green
elif [ -n "$GREEN" ]; then
  IDLE_PORT=8080
  REMOVE_CONTAINER=mapz-green
else
  IDLE_PORT=8080
  IDLE_CONTAINER=mapz-blue
fi

sudo docker run -d -p ${IDLE_PORT}:8080 -e "SPRING_ENV=${PROFILE}" --name=${IDLE_CONTAINER} ${DOCKER_IMAGE}

echo "after 10 seconds, health check start will be start"
echo "check response ---> curl -s http://127.0.0.1:$IDLE_PORT/actuator/health"
sleep 10

for retry_count in {1..10}
  do
    response=$(curl -s http://127.0.0.1:$IDLE_PORT/actuator/health)
    up_count=$(echo $response | grep 'UP' | wc -l)
    if [ $up_count -ge 1 ]; then
      echo "health check succeed"
      break
    else
      echo "health check no response"
      echo "Health check: ${response}"
    fi

    if [ $retry_count -ge 10 ]; then
      echo "Health check failed"
      echo "stop and remove container"
      sudo docker stop ${IDLE_CONTAINER}
      sudo docker rm ${IDLE_CONTAINER}
    fi

    echo "health check retry after 10 seconds"
    sleep 10
  done

echo "set \$service_url=127.0.0.1:${IDLE_PORT};" | sudo tee /etc/nginx/conf.d/service-url.inc

echo "Nginx reload"
sudo service nginx reload

if [ -n "$REMOVE_CONTAINER" ]; then
  docker stop ${REMOVE_CONTAINER}
  docker rm ${REMOVE_CONTAINER}
fi