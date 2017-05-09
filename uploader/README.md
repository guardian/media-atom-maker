# Video Upload Pipeline

**NB: this describes a work in-progress**

The direct video upload pipeline is implemented using AWS step functions co-ordinating
a number of lambdas. It has its own cloud-formation stack (`media-atom-pipeline`) that
is deployed automatically by Riff-Raff alongside the main `media-atom-maker` app. Each
lambda has a separate entry point into a single Scala code bundle (the `uploader` SBT
project).

## Deploying the DEV stack

## Adding a new step to the pipeline
