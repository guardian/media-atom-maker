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

There is a chance that the IAM key used for local development (media-atom-maker-DEV) has been disabled if it has not been rotated in a while. If this is the case, and you need the key, you will need to rotate the IAM key. To do this, increment the serial property in the CloudFormation template, and update the stack with the new template. This will generate the new IAM key, which you should update in the dev config file in S3 (under the settings `upload.accessKey` and `upload.secretKey`).

Next, setup nginx and install client side requirements by running:

```bash
./scripts/setup.sh
```
