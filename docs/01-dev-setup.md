# DEV Setup

## Requirements
Ensure you have the following installed:
- awscli
- Java 8
- nginx
- node v10+
- npm
- yarn
- nvm (optional)

### Other repositories
You'll also need to checkout these repositories:
- [dev-nginx](https://github.com/guardian/dev-nginx#nginx-dev-setup)

### Security credentials
You'll also need Janus credentials to the `media-service` account.

## nginx
Create a new site config in nginx using [dev-nginx](https://github.com/guardian/dev-nginx#nginx-dev-setup):

```bash
sudo /path/to/dev-nginx/setup-app.rb nginx/nginx-mapping.yml
```

### server_names_hash_bucket_size
The nginx `server_names_hash_bucket_size` should be `128`, up from the default of `64`.

Edit `/usr/local/etc/nginx/nginx.conf` setting the `server_names_hash_bucket_size` directive to `128` within the `http` directive.

For example:

```
http {
    include mime.types;
    include sites-enabled/*;
    client_max_body_size 20m;

    server_names_hash_bucket_size 128;

    default_type  application/octet-stream;
}
```

## Config
We use a shared DEV stack, with a shared config. Fetch it by running:

```bash
./scripts/fetch-dev-config.sh
```

## Client side requirements
Install client side requirements by running:

```bash
./scripts/setup.sh
```
