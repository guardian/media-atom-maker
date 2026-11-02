# Iconik checker

Run locally from the repository root with Java 21 and sbt. This standalone
Scala script talks directly to DynamoDB, Secrets Manager and Iconik. It does
not start Play or call the Media Atom Maker service.

```sh
AWS_PROFILE=media-service sbt 'iconikChecker/runMain com.gu.media.iconik.IconikChecker CODE <iconik-credentials-secret-id>'
```

Use `PROD` instead of `CODE` to check production. The secret must contain
`{"app-id":"...", "token":"..."}`. The default region is `eu-west-1`;
override it with `AWS_REGION`. AWS credentials use the SDK's default provider
chain, including `AWS_PROFILE` or environment credentials.
If `STAGE` is set, it must match the requested stage.

The AWS token needs `dynamodb:Scan` and `dynamodb:GetItem` on the three
`media-atom-maker-<stage>-iconik-*-table-v2` tables, and
`secretsmanager:GetSecretValue` on the supplied secret (plus `kms:Decrypt`
if the secret uses a customer-managed key). These permissions are only needed
when running this script, not when starting the service.

Checks are read-only by default. Add `--repair` to update mismatched titles
and parent references in DynamoDB, and prefix missing collections' titles
with `[deleted]`. Repair mode also needs `dynamodb:PutItem`.

```sh
AWS_PROFILE=media-service sbt 'iconikChecker/runMain com.gu.media.iconik.IconikChecker CODE <iconik-credentials-secret-id> --repair'
```

Each entity produces a JSON report. Reports describe the state before any
repair and include `repaired: true` when an update was made. Entities are
checked one at a time with a one-second pause. Request or storage failures
stop the run with a non-zero exit status; reported mismatches do not.
