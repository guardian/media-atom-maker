# HMAC

Node script to perform an HMAC request to an endpoint.

This script is primarily designed to use in DEV and as we use a self-signed certificate in DEV,
you'll want to set `NODE_TLS_REJECT_UNAUTHORIZED=0` before making any calls. For example:

```bash
NODE_TLS_REJECT_UNAUTHORIZED=0 ./make-hmac-request.js --path api2/atoms
```

## Usage
```bash
./make-hmac-request.js --path api2/atoms
```

By default, a `GET` request is made.

### Advanced Usage
Add the `--method` argument to specify an alternative HTTP verb. For example:

```bash
./make-hmac-request.js --path api2/atoms --method delete
```

To make a request with a data payload, for example a `PUT` or `POST`:

```bash
./make-hmac-request.js --path api2/atoms --method post --data '{"foo": "bar"}'
```

Alternatively, use an external file:

```bash
./make-hmac-request.js --path api2/atoms --method post --data-file $PWD/new-atom.json
```

## Configuration
By default, configuration is read from `/etc/gu/media-atom-maker.private.conf` (the same config file used by the Play app).

### Advanced Configuration
Specify a custom configuration file using the `--config-file` argument.

This is useful if you want to make an HMAC request to CODE or PROD. 
