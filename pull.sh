DOCKER_ID=$1
DOCKER_PASSWORD=$2

sudo docker login -u ${DOCKER_ID} -p ${DOCKER_PASSWORD}
echo ">>docker login succeed"

sudo docker pull -a mapz/mapz-backend-prod

echo ">>docker image pull succeed from docker hub"