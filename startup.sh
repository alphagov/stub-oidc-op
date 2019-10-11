#!/usr/bin/env bash
set -e

CONFIG_FILE=./verify-stub-op.yml

cd "$(dirname "$0")"

./gradlew installDist

if [ ! $(docker top localredis > /dev/null 2>&1) ]
then
docker run --name localredis -d -p 6379:6379 --rm redis
fi



./build/install/verify-stub-op/bin/verify-stub-op server $CONFIG_FILE
