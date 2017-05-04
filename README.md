# Media-Atom-Maker
This is app a Play app for editing media atoms, and a
publisher that pushes media atoms into a Kinesis stream.

## Credentials
0. You will need `media-service` credentials to run the app.
0. You will need read access to S3 files in the `workflow` profile for panda
   auth to work.

## Setting up dev environment
0. The project makes use of [panda
   auth](https://github.com/guardian/pan-domain-authentication). For this to
   work, follow the instructions
   [here](https://github.com/guardian/dev-nginx#nginx-dev-setup) to set up
   nginx locally.
0. The nginx bucket size should be `128`, up from the default of `64`. Edit `nginx.conf` and modify the `http` section to contain `server_names_hash_bucket_size 128;`
0. You'll need to get the private configuration information from the S3 bucket (atom-maker-conf) and put it in your /etc/gu folder. This will be referenced in `application.conf`
 on start up.
`aws s3 cp s3://atom-maker-conf/DEV/media-atom-maker.private.conf /etc/gu/media-atom-maker.private.conf --profile media-service`


## Running the apps
There are two apps which you can run:
1. An angular app which provides a simple way of editing and publishing media
   atoms.
2. A react app which allows for browsing atoms and more complex atom
   management. This is the app used by editorial for managing atoms.

### Running the react app
0. Run the setup script: `./scripts/setup.sh`
0. Run the app by running `scripts/start.sh` or `scripts/client-dev.sh` if you
   need to do client side development
0. Test that everything is working
   [here](https://media-atom-maker.local.dev-gutools.co.uk/video/videos#)

### Running the angular app
0. Make sure you have `gulp` installed
0. Run `npm install`
0. Generate javascript files by running `gulp`. You can watch changes to
   javascript by running `gulp watch`
0. `./sbt run`
0. Test everything is working
   [here](https://media-atom-maker.local.dev-gutools.co.uk/atoms)

## Publishing to CAPI CODE from dev
0. You will need `composer` credentials.
0. Modify the kinesis stream names in `/etc/gu/media-atom-maker.private.conf` to point to CAPI CODE.
0. Modify `AwsCredentials.scala` to set the `crossAccount` field to `composer` profile credentials.

## Testing

To run unit tests `sbt test`

To run the blackbox integration tests against a deployed environment:
0. Download the config from S3 `aws s3 cp s3://atom-maker-conf/TEST/media-atom-maker-integration-test.private.conf integration-tests/src/test/conf/media-atom-maker-integration-test.private.conf`
0. Run `sbt integrationTests/test`
