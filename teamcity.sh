#!/bin/bash

# This is a hacky solution to a boring problem...
# The branch `aa-pluto-project-ingestion` introduces a new Node lambda, and with it, new build steps.
# We can't add the new build steps to TC as all other branches will start failing because the build steps are invalid.
# We could clone the TC project, but that means build numbers get duplicated and every push gets two builds 🤢.
# So, lets run this file as a build step in every branch. That way, master just gets a friendly message, and other branches,
# namely `aa-pluto-project-ingestion` can customise it.

echo 'howdy partner'
