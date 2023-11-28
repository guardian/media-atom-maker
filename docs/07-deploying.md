# Deploying

## Infrastructure
The infrastructure is managed in a Cloud Formation template stored in the Editorial tools platform repository. This is deployed using the riff-raff project `media-service:media-atom-maker::cloudformation`.

## App
The app is deployed with RiffRaff under `media-service:media-atom-maker`.

## Prout
We use [Prout](https://github.com/guardian/prout) to check if a PR has been deployed.
