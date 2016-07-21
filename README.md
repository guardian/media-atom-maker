# media-atom-maker
This app has two functions: it is a simple Play app for editing media atoms,
and it's a publisher that pushes media atoms into a Kinesis stream.

## Setting up dev environment
0. You will need read access to S3 files in the `workflow` profile.
0. The project makes use of [panda auth](https://github.com/guardian/pan-domain-authentication),
so follow the instructions [here](https://github.com/guardian/dev-nginx#nginx-dev-setup) to set up nginx locally.
0. Start up the Play app.
0. Test everything is working: https://media-atom-maker.local.dev-gutools.co.uk/atoms
