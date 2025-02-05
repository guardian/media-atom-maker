# Running

Media Atom Maker requires Node 18.X, so ensure you're running that version, or run `nvm use` first.

## Credentials

You need media-service credentials and capi `API Gateway invocation` credentials to run the app.

## React App
Once running, the React App can be seen [here](https://video.local.dev-gutools.co.uk/videos)

There are two ways to run the React App:

### With hot-reloading
To run the React App with hot-reloading, run:

```bash
./scripts/client-dev.sh
```
The app will be served at https://video.local.dev-gutools.co.uk/

### Without hot-reloading
To run the React App without hot-reloading, run:

```bash
./scripts/start.sh
```

## Publishing to CAPI CODE from DEV
- You will need `composer` Janus credentials.
- Modify the kinesis stream names in `/etc/gu/media-atom-maker.private.conf` to point to CAPI CODE.
- Modify `AwsCredentials.scala` to set the `crossAccount` field to `composer` profile credentials.
