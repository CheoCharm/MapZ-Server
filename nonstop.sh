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
        IDLE_CONTAINER=mapz-blue
else
        IDLE_PORT=8080
        IDLE_CONTAINER=mapz-blue
fi

echo "IDLE_PORT --->${IDLE_PORT}"
echo "IDLE_CONTAINER --->${IDLE_CONTAINER}"
echo "REMOVE_CONTAINER --->${REMOVE_CONTAINER}"

sudo docker run -d -p ${IDLE_PORT}:8080 -e "SPRING_ENV=${PROFILE}" --name=${IDLE_CONTAINER} ${DOCKER_IMAGE}

echo "After 10 seconds, health check start will be start"
echo "Check response ---> curl -s http://127.0.0.1:$IDLE_PORT/actuator/health"
sleep 10

for retry_count in 1 2 3 4 5 6 7 8 9 10
do
    response=$(curl -s http://127.0.0.1:$IDLE_PORT/actuator/health)
    up_count=$(echo $response | grep 'UP' | wc -l)
    if [ $up_count -ge 1 ]
    then
      echo "health check succeed"
      break
    else
      echo "health check no response"
    fi

    if [ $retry_count -ge 10 ]
    then
      echo "Health check failed"
      echo "stop and remove container"
      sudo docker stop ${IDLE_CONTAINER}
      sudo docker rm ${IDLE_CONTAINER}
      exit 1
    fi

    echo "health check retry after 10 seconds"
    sleep 10
done

echo "set \$service_url http://127.0.0.1:${IDLE_PORT};" | sudo tee /etc/nginx/conf.d/service-url.inc

echo "Nginx reload"
sudo service nginx reload

if [ -n "$REMOVE_CONTAINER" ]
then
  sudo docker stop ${REMOVE_CONTAINER}
  sudo docker rm ${REMOVE_CONTAINER}
fi

echo "nonstop.sh completed"