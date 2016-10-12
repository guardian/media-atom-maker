#!/usr/bin/env bash
npm run build-dev &
sbt run -Dhttp.port=9001
