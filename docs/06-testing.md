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
include, or boot the production dependency wiring. It uses the HMAC dependency's
date formatter when signing synthetic requests. Deployed configuration overrides,
client/server locale interoperability, network access, OAuth login and real
AWS/YouTube integrations are not covered.

The existing HMAC dependency uses the JVM default locale for HTTP dates. On
Java 21 with `en_GB`, its September abbreviation is `Sept`, and a standard HTTP
date containing `Sep` is rejected. This was observed during local testing; the
suite uses the dependency's formatter to remain compatible with its verifier.
Verify the deployed JVM locale against the future client's HTTP-date format
before rollout. Changing the dependency or production locale is outside this
authentication-only change.

## Blackbox tests
To run the blackbox tests against a deployed environment, first download the config:

```bash
./scripts/fetch-blackbox-test-config.sh
```

Then run the tests:

```bash
sbt integrationTests/test
```
