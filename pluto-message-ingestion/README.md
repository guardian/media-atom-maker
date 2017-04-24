# Pluto Message Ingestion

An AWS Lambda that provides integration with [Pluto](https://github.com/guardian/pluto) via a Kinesis stream.

Pluto and MAM do not communicate directly as the AWS VPCs are not setup. Instead, they communicate via a Kinesis stream. 
Pluto puts JSON messages on this stream and this Lambda picks them up.

The Lambda switches on `message.type` and calls the relevant MAM end-point.

## Message types processed

### `project-created`
Pluto will send a message to Kinesis when a new Project has been created. MAM will then store a copy of the metadata in Dynamo.

Pluto sends a JSON blob that looks like:

```json
{
  "type": "project-created",
  "project_id": "foo",
  "gnm_project_headline": "bar",
  "gnm_project_production_office": "baz",
  "gnm_project_status": "gu",
  "created": "2017-04-24T17:20:11.503Z"
}
```

### `project-updated`
Pluto will send a message to Kinesis when an existing Project has been updated. MAM will then update its copy in Dynamo.

Pluto sends a JSON blob that looks like:

```json
{
  "type": "project-updated",
  "project_id": "foo",
  "gnm_project_headline": "bar",
  "gnm_project_production_office": "baz",
  "gnm_project_status": "gu",
  "created": "2017-04-24T17:20:11.503Z"
}
```
