# Testing

## Unit tests
To run unit tests, run:

```bash
sbt test
```

### Draft creation authentication

Using the Java version in `.tool-versions`, run the focused regression suite:

```bash
sbt 'app/testOnly controllers.ApiCreationTest'
sbt 'app/scalafmtCheck' 'app/Test/scalafmtCheck'
```

`ApiCreationTest` uses the generated application router, Play's default HTTP
filters with the same allowed-hosts exclusion as `MediaAtomMaker`, the real HMAC
verifier, and locally signed Pan-Domain cookies. It checks creation and draft
semantics, service attribution, invalid/missing authentication, browser permission
and MFA/domain validation, browser CSRF protection, and invalid request bodies.
Persistence, publishing and external clients are mocked; no credentials or live
infrastructure are needed.

The fixture deliberately does not load `application.conf` or its private config
include, or boot the production dependency wiring. Deployed configuration
overrides, network access, OAuth login and real AWS/YouTube integrations are not
covered.

HMAC tests sign the original HTTP-date header independently of the dependency's
locale-sensitive formatter, using a fixed verifier clock. They cover standard
English `Sep` and legacy English `Sept`, every month and weekday, the five-minute
past/future validity boundaries, malformed dates/tokens and rejection if the
date spelling or request path changes without re-signing. Non-English HTTP dates
are rejected; English dates work regardless of the server's JVM locale.

`PanDomainAuthActions` overrides the dependency's per-secret verification with
explicit English date parsing. Only the freshness check normalizes `Sept` to
`Sep`; signature verification uses the exact original header text. This applies
to all existing HMAC actions, while retaining key selection and browser fallback.
It does not change the JVM locale or upgrade dependencies.

To exercise locale independence in separate JVMs without changing global locale
state inside parallel tests:

```bash
sbt -Duser.language=en -Duser.country=GB 'app/testOnly controllers.ApiCreationTest'
sbt -Duser.language=fr -Duser.country=FR 'app/testOnly controllers.ApiCreationTest'
sbt -Duser.language=de -Duser.country=DE 'app/testOnly controllers.ApiCreationTest'
```

## Blackbox tests
To run the blackbox tests against a deployed environment, first download the config:

```bash
./scripts/fetch-blackbox-test-config.sh
```

Then run the tests:

```bash
sbt integrationTests/test
```
