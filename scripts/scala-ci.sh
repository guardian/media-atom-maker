#!/usr/bin/env bash

set -e
sbt clean compile test \
  app/Debian/packageBin \
  app/normalisePackageName \
  uploader/Universal/packageBin \
  expirer/Universal/packageBin \
  scheduler/Universal/packageBin \
  uploader/Compile/resourceManaged
