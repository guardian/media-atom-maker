#!/bin/bash

set -euo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

pushd "$SCRIPT_DIR"

rm -r "$SCRIPT_DIR"/output/*

docker build -t youtube-partner-api-gen .
docker run --rm -v "$SCRIPT_DIR"/output:/data/output youtube-partner-api-gen

pushd output

mvn package source:jar

echo Build complete: your jars are here:

ls -1 "$SCRIPT_DIR"/output/target/*.jar

echo
echo done
