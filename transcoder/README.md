Transcoder
----------

A lambda that will transcode master files in one bucket location to mp4s
and store in another location. This transcoder can be invoked if we are
required to self host a video.

The lambda will invoke a Job on a Pipeline in AWS Elastic Transcoder.

Pipeline
--------
Cloudformation does not support the creation of a Pipeline. Currently
just one Pipeline per environment is required so these have been created
manually. If we need more Pipelines per environment we should create this
with code.

A Pipeline requires an input bucket (holding the master files) and an output
bucket (were the mp4s will be stored). These have been cloudformed in
media-atom-maker.json. The Pipeline name is stored as an Output on
the cloudformation for documentation purposes only.

Lambda
------
Requires the Pipeline ID and the master file name to trigger a transcode.
This must be sent in the format
```
{
  "pipelineId": "[pipeline-id]",
  "masterFileName": "[folder-path-in-S3-bucket]/filename"
}
```

The mp4 will be stored with the same folder structure and filename (but
as .mp4) to the Pipeline Output bucket location.

