#!/usr/bin/env bash
set -e

CONFIG_FILE=./stub-oidc-op.yml

cd "$(dirname "$0")"

LOCAL_IP="$(ipconfig getifaddr en0)"
export REDIS_URI="redis://${LOCAL_IP}:6379"

./gradlew installDist

trap "docker container stop opRedis" EXIT
docker run --name opRedis -d -p 6379:6379 --rm redis



./build/install/stub-oidc-op/bin/stub-oidc-op server $CONFIG_FILE
