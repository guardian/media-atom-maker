# Video Upload Pipeline

The direct video upload pipeline is implemented using AWS step functions co-ordinating
a number of lambdas. It has its own cloud-formation stack (`media-atom-pipeline`) that
is deployed automatically by Riff-Raff alongside the main `media-atom-maker` app. Each
lambda has a separate entry point into a single Scala code bundle (the `uploader` SBT
project).

The cloud-formation is assembled from a series of templates in the `uploader` project
during packaging.

## Deploying the DEV stack

You will need the AWS CLI and `jq` installed.

Run in root:

```
sbt uploader/universal:packageBin
```

This will compile both the cloud-formation template and the lambda code.

The cloud-formation template will be output at:

```
uploader/target/scala-2.11/resource_managed/main/media-atom-pipeline.yaml
```

Use the AWS console to update the DEV stack with the new template. Unfortunately this
cannot be scripted as federated Janus credentials cannot modify IAM roles 😞.

Finally run:

```
./scripts/deploy-pipeline-dev.sh
```

This will update each DEV lambda with our local build.

## State Machine Linting

The [statelint](https://github.com/awslabs/statelint) Ruby gem can verify the syntax of
the state machine before deploying it. In most cases the errors you get here are way more
readable than those from the cloud formation console.

```
statelint uploader/src/main/resources/state-machine.json
```

We do not specify the lambda ARN directly in the JSON so you will always see the following error:

```
State Machine.States.GetChunkFromS3.Resource is "${GetChunkFromS3.Arn}" but should be A URI
```

## Adding a new step to the pipeline

The pipeline graph is defined in [state-machine.json](src/main/resources/state-machine.json).
Adding any states other than [Task](http://docs.aws.amazon.com/step-functions/latest/dg/concepts-tasks.html#concepts-tasks)
is as simple as updating the JSON and re-deploying.

To add a new task:

- Create the new handler in [com.gu.media.upload](src/main/scala/com/gu/media/upload)
- Add an entry for the task in [state-machine.json](src/main/resources/state-machine.json)

```json
{
  "NewTaskName": {
    "Type": "Task",
    "Resource": "${NewTaskName.Arn}",
    "Next": "NextState"
  }
}
```

- Add an entry for the task in [build.sbt](../build.sbt#L71)

```scala
  "NewTaskName" -> LambdaConfig(
    description = "Does something awesome!"
  )
```

The compiled cloudformation will contain a new entry for the lambda.

- Add an entry for the new lambda to [riff-raff.yaml](../conf/riff-raff.yaml)

```yaml
  functionNames:
    - "media-atom-pipeline-SomeFunction-"
    - "media-atom-pipeline-NewTaskName-"
```

## Customising Cloud Formation

The cloud-formation compiler script combines various templates, injecting parameters as specified
in `build.sbt`. Currently there is only one parameter:

- Lambda Description

To add a new parameter:

- Edit [lambda-template.yaml](https://github.com/guardian/media-atom-maker/blob/mbarton/step-functions/uploader/src/main/resources/lambda-template.yaml)
adding a new entry for the variable:

```yaml
MemorySize: {{MemorySize}}
```

- Edit [StateMachines.scala](https://github.com/guardian/media-atom-maker/blob/mbarton/step-functions/project/StateMachines.scala)
adding the new variable to `LambdaConfig`:

```scala
case class LambdaConfig(description: String, timeout: Int = 60, memorySize: Int = 512)
```

- Further edit [StateMachines.scala](https://github.com/guardian/media-atom-maker/blob/mbarton/step-functions/project/StateMachines.scala)
 adding the variable to the `compileTemplate` task:

```scala
val instance = lambdaTemplate
  .replace("{{name}}", name)
  .replace("{{description}}", description)
  .replace("{{timeout}}", timeout.toString)
  .replace("{{memorySize}}", memorySize.toString)
```
