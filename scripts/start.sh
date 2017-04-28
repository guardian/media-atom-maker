#!/usr/bin/env bash
yarn run build-dev &
./sbt $@ app/run
