# Iconik checker

Run locally from the repository root with Java 21 and sbt. This standalone
Scala script talks directly to DynamoDB, Secrets Manager and Iconik. It does
not start Play or call the Media Atom Maker service.

```sh
AWS_PROFILE=media-service sbt 'iconikChecker/runMain com.gu.media.iconik.IconikChecker CODE <iconik-credentials-secret-id>'
```

Reports go to `iconik-checker-CODE.jsonl` (or `iconik-checker-PROD.jsonl`)
in the working directory by default. Use `--output <file>` to choose a path:

```sh
AWS_PROFILE=media-service sbt 'iconikChecker/runMain com.gu.media.iconik.IconikChecker CODE <iconik-credentials-secret-id> --output /tmp/iconik-code.jsonl'
```

The output file is overwritten on each run. Each line is one JSON report,
flushed immediately so completed results remain available if a later check fails.
Three sibling files contain only defects, using the same JSONL format:

| file (CODE default) | contents |
| --- | --- |
| `iconik-checker-CODE.missing.jsonl` | collections missing from Iconik |
| `iconik-checker-CODE.name-mismatches.jsonl` | stored titles that differ from Iconik |
| `iconik-checker-CODE.parent-mismatches.jsonl` | stored parent references that differ from Iconik |

`--output /tmp/report.jsonl` produces `/tmp/report.missing.jsonl` and the
corresponding mismatch files. An entity with multiple defects appears in each
relevant file. Healthy entities appear only in the full report. Defect files
are created even when empty and overwritten on each run. With `--repair`,
they describe the defects found before repair and retain the `repaired` flag.

The checker updates a single progress line on stderr with a bold cyan `ICONIK`
badge, a bar, the completed count and percentage. It turns green at completion.
Set `NO_COLOR=1` or `TERM=dumb` for plain output. Reports stay in the file,
not the terminal.

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

Reports describe the state before any
repair and include `repaired: true` when an update was made. Entities are
checked one at a time with a one-second pause. Request or storage failures
stop the run with a non-zero exit status; reported mismatches do not.
