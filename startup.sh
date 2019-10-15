#!/usr/bin/env bash
set -e

CONFIG_FILE=./verify-stub-op.yml

cd "$(dirname "$0")"

./gradlew installDist

trap "docker container stop opRedis" EXIT
docker run --name opRedis -d -p 6379:6379 --rm redis



./build/install/verify-stub-op/bin/verify-stub-op server $CONFIG_FILE
