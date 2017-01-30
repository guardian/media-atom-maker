[![CircleCI](https://circleci.com/gh/guardian/media-atom-maker.svg?style=svg)](https://circleci.com/gh/guardian/media-atom-maker)

# Media-Atom-Maker
This is app a Play app for editing media atoms, and a
publisher that pushes media atoms into a Kinesis stream.

## Credentials
0. You will need `media-service` credentials to run the app and `composer`
   credentials if you want to be able to publish media atoms.
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
- There are two apps which you can run:
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

## Publishing to CODE kinesis stream
0. If you want to publish to the CODE kinesis streams, you will need
   credentials for `composer` profile.
0. If you are still unable to publish atoms, make sure that the
   `readFromComposer` property is set to true in `/etc/gu/media-atom-maker.private.conf` the kinesis
   stream names are the code stream names. You can find these streams in the S3 `atom-maker-conf` bucket
   `composer` aws account.
