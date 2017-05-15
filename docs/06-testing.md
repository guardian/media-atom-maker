# Testing

## Unit tests
To run unit tests, run:

```bash
./sbt test
```

## Blackbox tests
To run the blackbox tests against a deployed environment, first download the config:

```bash
./scripts/fetch-blackbox-test-config.sh
```

Then run the tests:

```bash
./sbt integrationTests/test
```
