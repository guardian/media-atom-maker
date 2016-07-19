# media-atom-maker
This Play app is a simple UI for editing media atoms.

## Setting up dev environment
0. You will need read access to S3 files in the `workflow` profile.
0. The project makes use of [pan-domain authentication](https://github.com/guardian/pan-domain-authentication),
so follow the instructions [here](https://github.com/guardian/dev-nginx#nginx-dev-setup) to set up nginx locally.
0. Start up the app on port 9001: `sbt -Dhttp.port=9001`
0. Test everything is working: https://media-atom-maker.local.dev-gutools.co.uk/atoms
