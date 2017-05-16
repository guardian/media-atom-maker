# Client side dev

## Adding a requirement
Want to add a module? Add it with `yarn`:

```bash
yarn install <module>
```

This will also update the [`yarn.lock`](https://yarnpkg.com/lang/en/docs/yarn-lock/) file which should also be checked in.

## Linting
We use [`eslint`](http://eslint.org/) and [`prettier`](https://github.com/prettier/prettier) to ensure a consistent code style. 
You can run `yarn lint`.

Additionally, we lint in a pre-commit hook, so just `git add` and `git commit` as usual.
