# Debugging

## Client side
The following Chrome extensions are useful when developing React Apps:
- [React Developer Tools](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi)
- [Redux DevTools](https://chrome.google.com/webstore/detail/redux-devtools/lmhkpmbekcpmknklioeibfkpmmfibljd)

## Server side
As mentioned in [03-running.md](./03-running.md), there are a different ways to run the app.
Adding the `--debug` flag to any script will launch `sbt` with remote debugging on port `9100`.

For example:

```bash
./scripts/client-dev.sh --debug
```

You can then [setup IntelliJ with a remote run configuration] on this port and add breakpoints, step through code etc.

## Fault injection

The app includes a guarded fault injection mode for deliberately triggering exceptions in DEV or other non-production environments.

Enable it with:

```bash
export FAULT_INJECTION_ENABLED=true
```

Backend triggers:

- `GET /debug/throw-exception/sync`
- `GET /debug/throw-exception/async`

Frontend trigger:

- append `?throwClientException=1` to a UI route such as `/videos?throwClientException=1`

When `faultInjection.enabled` is disabled, the backend endpoints return `404` and the frontend trigger is ignored.
