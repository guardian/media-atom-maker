# Other

## Publishing to CAPI CODE from DEV
- You will need `composer` Janus credentials.
- Modify the kinesis stream names in `/etc/gu/media-atom-maker.private.conf` to point to CAPI CODE.
- Modify `AwsCredentials.scala` to set the `crossAccount` field to `composer` profile credentials.
