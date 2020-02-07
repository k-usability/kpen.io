#!/bin/bash

set -Eeuxo pipefail

WEB_DIR=web
CLIENT_DIR=${WEB_DIR}/client
STATIC_DIR=${WEB_DIR}/src/main/resources/static

if [ "$#" -ne 1 ]; then
    echo "You must enter exactly 1 command line arguments"
    exit 1
fi


if [ $1 = "web" ]; then
  docker start -a -i `docker create -p 3010:3010  -t -i kpen-io:latest /home/user/web.sh`
  exit 0
fi

if [ $1 = "worker" ]; then
  docker start -a -i `docker create  -t -i kpen-io:latest /home/user/worker.sh`
  exit 0
fi

if [ $1 = "jar" ]; then
  (cd ${WEB_DIR}; ./gradlew clean bootJar)
  docker build -t kpen-io -f Dockerfile.kpen .
  docker run -p 3010:3010 kpen-io:latest
  exit 0
fi

rm -rf ${STATIC_DIR}
(cd ${CLIENT_DIR}; npm run-script build)
mv ${CLIENT_DIR}/build ${STATIC_DIR}
(cd ${WEB_DIR}; ./gradlew clean bootJar)

docker build -t kpen-io:latest -f Dockerfile.kpen .

if [ $1 = "push" ]; then
  `aws --profile consensys ecr get-login --no-include-email --region us-east-2`
  docker tag kpen-io:latest 472805539381.dkr.ecr.us-east-2.amazonaws.com/kpen-io:latest
  docker push 472805539381.dkr.ecr.us-east-2.amazonaws.com/kpen-io:latest
  exit 0
fi

if [ $1 = "run" ]; then
  docker run -p 3010:3010 -it kpen-io:latest /bin/bash
  exit 0
fi
