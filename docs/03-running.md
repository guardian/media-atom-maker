# Running

Media Atom Maker requires Node 6.X, so ensure you're running that version, or run `nvm use` first.

## (legacy) Angular App
To run the Angular App:

- Generate javascript files by running `gulp watch`
- Start sbt by running `./sbt app/run`
- Open the app by going [here](https://video.local.dev-gutools.co.uk/atoms)

## React App
There are two ways to run the React App.

Once its running, open the app by going [here](https://video.local.dev-gutools.co.uk/videos)

### With hot-reloading
To run the React App with hot-reloading, run:

```bash
./scripts/client-dev.sh
```

### Without hot-reloading
To run the React App without hot-reloading, run:

```bash
./scripts/start.sh
```
