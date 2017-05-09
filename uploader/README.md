# Video Upload Pipeline

**NB: this describes a work in-progress**

The direct video upload pipeline is implemented using AWS step functions co-ordinating
a number of lambdas. It has its own cloud-formation stack (`media-atom-pipeline`) that
is deployed automatically by Riff-Raff alongside the main `media-atom-maker` app. Each
lambda has a separate entry point into a single Scala code bundle (the `uploader` SBT
project).

The cloud-formation is assembled from a series of templates in the `uploader` project
during packaging.

## Deploying the DEV stack

You will need the AWS CLI and `jq` installed.

Run `./scripts/deploy-pipeline-dev.sh`. It will:

- Package the `uploader` project (including generating the cloud-formation)
- Update `media-atom-pipeline-DEV` with the new cloud-formation
- Update each lambda with the new code

NB: any changes to CloudFormation parameters must be updated manually.

## Adding a new step to the pipeline
