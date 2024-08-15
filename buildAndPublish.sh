#!/bin/bash

if [[ -z "${STEF_JFROG_USER}" ]]; then
  echo "ERROR: Required 'STEF_JFROG_USER' environment variable is undefined"
  echo "HINT: export STEF_JFROG_USER=<jfrog_user>"
  exit 1
fi

if [[ -z "${STEF_JFROG_KEY}" ]]; then
  echo "ERROR: Required 'STEF_JFROG_KEY' environment variable is undefined"
  echo "HINT: export STEF_JFROG_KEY=<jfrog_key>"
  exit 1
fi

./gradlew clean build -P registryUser="$STEF_JFROG_USER" -P registryPassword="$STEF_JFROG_KEY" -Ppublish
