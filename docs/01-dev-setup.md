# DEV Setup

## Requirements
Ensure you have the following installed:
- awscli
- Java 8
- nginx
- node v10+
- npm
- yarn
- nvm
- [dev-nginx](https://github.com/guardian/dev-nginx#installation)

You'll also need Janus credentials to the `media-service` account.

## Local setup 

We use a shared DEV stack, with a shared config. Fetch it by running:

```bash
./scripts/fetch-dev-config.sh
```

Next, setup nginx and install client side requirements by running:

```bash
./scripts/setup.sh
```
