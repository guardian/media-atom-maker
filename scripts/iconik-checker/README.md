# Iconik checker

Run locally from the repository root with Java 21 and sbt. The analysis command
talks directly to DynamoDB, Secrets Manager, and Iconik. It does not start Play
or call the Media Atom Maker service.

```sh
AWS_PROFILE=media-service sbt 'iconikChecker/runMain com.gu.media.iconik.IconikChecker CODE <iconik-credentials-secret-id>'
```

Use `PROD` instead of `CODE` to check production. The secret must contain
`{"app-id":"...", "token":"..."}`. The default region is `eu-west-1`;
override it with `AWS_REGION`. AWS credentials use the SDK's default provider
chain, including `AWS_PROFILE` or environment credentials. If `STAGE` is set,
it must match the requested stage.

The analysis is read-only. It writes:

* `iconik-checker-CODE.jsonl` — one validation report per entity.
* `iconik-checker-CODE.missing.jsonl`,
  `iconik-checker-CODE.name-mismatches.jsonl`, and
  `iconik-checker-CODE.parent-mismatches.jsonl` — defect-only reports.
* `iconik-checker-CODE.unrepairable.jsonl` — entities whose repair cannot be
  planned automatically (for example, a project moved to a commission missing
  from DynamoDB), with a `reason` field.
* `iconik-checker-CODE.repairs.jsonl` — proposed DynamoDB changes, one change
  per line, followed by a completion record.

Output files are overwritten on each analysis. Use `--output <file>` and
`--plan-output <file>` to choose other paths. Each report line is flushed as it
is written so completed results remain available if a later check fails.
The apply runner requires the completion record, so it rejects a partial plan
left by a failed analysis.

Each repair-plan line includes the stage, entity type, ID, target table, the
full logical item in `backup` and `after`, and an `update` object with the old
and proposed values for each changed field. Review the plan before applying
it. The backup is for inspection and recovery planning; do not use it as a
whole-item `put-item`, which could overwrite concurrent changes.

Apply selected entities by their `entity-type:id` selector:

```sh
AWS_PROFILE=media-service sbt 'iconikChecker/runMain com.gu.media.iconik.IconikChecker apply PROD /path/to/iconik-checker-PROD.repairs.jsonl --id project:<project-id> --id commission:<commission-id>'
```

After reviewing a canary, apply the entire plan only with the explicit
`--all` flag:

```sh
AWS_PROFILE=media-service sbt 'iconikChecker/runMain com.gu.media.iconik.IconikChecker apply PROD /path/to/iconik-checker-PROD.repairs.jsonl --all'
```

The apply runner validates every plan line and checks the requested stage and
table before making any writes. It calls DynamoDB `UpdateItem` via the AWS SDK
once per selected entity, updating only the listed fields. Each request is
conditional on the old values still matching DynamoDB, and the runner stops on
the first failed update. A failed run can therefore have applied earlier lines;
use small ID selections for canaries. Applying does not call Iconik or need the
Iconik secret. It requires `dynamodb:UpdateItem` on the three stage-specific
Iconik tables.

Analysis needs `dynamodb:Scan` and `dynamodb:GetItem` on those tables, plus
`secretsmanager:GetSecretValue` on the supplied secret (and `kms:Decrypt` if
the secret uses a customer-managed key). Repairs are never written during
analysis.

The checker updates one progress line on stderr with a bold cyan `ICONIK`
badge, a bar, the completed count, and percentage. It turns green at
completion. Set `NO_COLOR=1` or `TERM=dumb` for plain output. Reports stay in
files, not the terminal.
