# Bookmarklets

A quick and hacky solution to allow CP to delete atoms without reliance on us.

This is temporary until Permissions are integrated into the Tool.

## Building
Run the command:

```bash
npm run build-bookmarklet
```

This will generate the file `delete-atom.bookmarklet`. This code can then be added as a bookmarklet in Chrome.

## Compatibility
Only tested in Chrome. Should work in any browser that [supports](http://caniuse.com/#search=Fetch) 
the [FetchAPI](https://developer.mozilla.org/en/docs/Web/API/Fetch_API)... but why would you not use Chrome?
