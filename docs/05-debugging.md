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
