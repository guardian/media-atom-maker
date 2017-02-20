#!/usr/bin/env bash
npm run build-dev &
./sbt $@ app/run
