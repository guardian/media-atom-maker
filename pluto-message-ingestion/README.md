# Pluto Message Ingestion

An AWS Lambda that provides integration with [Pluto](https://github.com/guardian/pluto) via a Kinesis stream.

Pluto and MAM do not communicate directly as the AWS VPCs are not setup. Instead, they communicate via a Kinesis stream.
Pluto puts JSON messages on this stream and this Lambda picks them up.

The Lambda switches on `message.type` and calls the relevant MAM end-point.

## Message types processed

> [!INFO]
> To test the lambda in CODE, instead of sending messages on the stream
> you can create a test event in the Lambda console.
> Start by creating a message in one of the formats below, then base64-encode
> it, then insert that encoded message into an event that looks like:
> ```json
> {
>   "Records": [
>     {
>       "kinesis": {
>         "data": "[your encoded message]"
>       }
>     }
>   ]
> }
> ```
> Your fake Pluto project and commission should now be sent to MAM and stored
> in the `media-atom-maker-CODE-pluto-[projects/commissions]-table`s

### `project-created`
Pluto will send a message to Kinesis when a new Project has been created. MAM will then store a copy of the metadata in Dynamo.

Pluto sends a JSON blob that looks like:

```json
{
  "type": "project-created",
  "id": "foo",
  "title": "bar",
  "status": "New",
  "commissionId": "baz",
  "commissionTitle": "baz",
  "productionOffice": "UK",
  "created": "2017-04-24T17:20:11.503Z"
}
```

### `project-updated`
Pluto will send a message to Kinesis when an existing Project has been updated. MAM will then update its copy in Dynamo.

Pluto sends a JSON blob that looks like:

```json
{
  "type": "project-updated",
    "id": "foo",
    "title": "bar",
    "status": "New",
    "commissionId": "baz",
    "commissionTitle": "baz",
    "productionOffice": "UK",
    "created": "2017-04-24T17:20:11.503Z"
}
```

## Behaviour

### Updating pre-existing projects
If an old Pluto Project is updated, a `project-updated` message still gets put on the Kinesis stream.
As MAM has not seen this project before, it does not know what to update and thus the message is discarded,
logging `attempted to update a project that does not exist`.

This means only projects created **after** the Pluto and MAM changes have gone out can be selected as a destination
for Direct Upload.

### Unknown message types
If a message is put on the Kinesis stream that is not recognised, it will be logged and the lambda will succeed.
This is because Kinesis blocks on failure; succeeding takes the message off the stream so we can continue with
remaining messages.

See [http://docs.aws.amazon.com/lambda/latest/dg/retries-on-errors.html](http://docs.aws.amazon.com/lambda/latest/dg/retries-on-errors.html) for more info.
