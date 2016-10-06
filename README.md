# media-atom-maker
This app is a simple Play app for editing media atoms,
and it's a publisher that pushes media atoms into a Kinesis stream.

## Setting up dev environment
0. Set up a dev stack by generating a `media-atom-maker-dev.json` template from the corresponding `.yml` file
in the cloudformation folder
0. Run `./scripts/setup-dev-conf` to generate an `application.conf` file (`cd` into `scripts` first)
0. You will need read access to S3 files in the `workflow` profile.
0. The project makes use of [panda auth](https://github.com/guardian/pan-domain-authentication),
so follow the instructions [here](https://github.com/guardian/dev-nginx#nginx-dev-setup) to set up nginx locally.
0. Make sure you have `gulp` installed
0. Run `npm install`
0. Generate javascript files by running `gulp`. You can watch changes to javascript by running `gulp watch`
0. `./sbt run`
0. Test everything is working: https://media-atom-maker.local.dev-gutools.co.uk/atoms

## Publishing to CODE kinesis stream
0. If you want to publish to the CODE kinesis streams, you will need credentials for `composer` profile.
0. You also need to set the `readFromComposer` property to true in `application.conf` and change the kinesis
stream names to the code stream names, found in the `composer` account.
