#!/bin/sh
set -eu
docker compose up -d --build
trap 'docker compose down' EXIT
i=0
until curl -fsS http://localhost:8080/actuator/health/readiness >/dev/null; do i=$((i+1)); [ "$i" -lt 30 ] || exit 1; sleep 2; done
curl -fsS http://localhost:8080/v3/api-docs >/dev/null
