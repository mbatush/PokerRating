#!/bin/bash

# http://redsymbol.net/articles/unofficial-bash-strict-mode/
set -euo pipefail
IFS=$'\n\t'

if ! ./gradlew spotlessCheck; then
  ./gradlew spotlessApply
  echo ""
  echo ""
  echo -e "\033[0;33mCode has been formatted; please git diff/add and recommit."
  echo ""
  echo ""
fi

./gradlew clean check -Pit.enable "$@"
