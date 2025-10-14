# Architecture

## Graph

```mermaid
graph LR
    Pluto((Pluto)):::External
    YouTube((YouTube)):::External
    CAPI((CAPI)):::External
    Composer((Composer))
    Workflow((Workflow))
    uploads-to-pluto{{uploads-to-pluto<br/>Kinesis}}:::Stream
    media-atom-maker-ingested-videos{{media-atom-maker-ingested-videos<br/>SQS}}:::Stream
    PlutoIntegrationIncomingStream{{PlutoIntegrationIncomingStream<br/>Kinesis}}:::Stream
    uploads-to-pluto --> Pluto
    Pluto --> media-atom-maker-ingested-videos
    Pluto --> PlutoIntegrationIncomingStream
    SendToPluto[SendToPluto<br/>lambda] --> uploads-to-pluto
    media-atom-maker-ingested-videos -->|Akka Actor| MediaAtomMaker{Media Atom Maker<br/>Scala Play<br/>EC2}
    PlutoIntegrationIncomingStream --> PlutoMessage[Pluto Message<br/>Ingestion<br/>Lambda]
    PlutoMessage --> MediaAtomMaker
    MediaAtomMaker <--> PlutoCommissions[(Pluto<br/>Commissions<br/>dynamo<br/>table)]
    MediaAtomMaker <--> PlutoProjects[(Pluto<br/>Projects<br/>dynamo<br/>table)]
    MediaAtomMaker <--> MediaAtomMakerProd[(MediaAtom<br/>Maker<br/>dynamo<br/>table)]
    MediaAtomMaker <--> PublishedMedia[(PublishedMedia<br/>AtomMaker<br/>dynamo<br/>table)]
    MediaAtomMaker --> MediaAtomMakerUpload[[Media Atom<br/>Maker Upload<br/>S3 bucket]]
    MediaAtomMaker --> uploads-to-pluto
    User(((User)))
    User --> ClientSide
    User --> Pluto
    ClientSide[Client Side UI<br/>React<br/>]
    ClientSide --> MediaAtomMaker
    ClientSide --> MediaAtomMakerUpload
    ClientSide -->|API| Composer
    MediaAtomMakerUpload --> Uploader
    Uploader[Uploader<br/>Step Function]
    Uploader --> SendToPluto
    Uploader --> YouTube
    Uploader --> UploadsOrigin[[uploads-<br/>origin.guim.co.uk<br/>S3 bucket]]
    Uploader --> ManualPluto[(ManualPluto<br/>MediaAtomMaker<br/>dynamo<br/>table)]
    ManualPluto --> uploads-to-pluto
    UploadsOrigin --> CAPI
    MediaAtomMaker --> PublishAtom[Publish Atom<br/>SNS Topic]
    Scheduler[Scheduler lambda<br/>Every 15 minutes]
    Scheduler --> MediaAtomMaker
    CAPI --> Scheduler
    CAPI --> LaunchDetector[LaunchDetector<br/>EC2 Instance <br/>Watches for atom changes]
    LaunchDetector -->|HTTP requests| Pluto
    PublishAtom --> CAPI
    Expirer[Expirer lambda<br/>Every 15 minutes]
    CAPI --> Expirer
    Expirer --> YouTube
    MediaAtomMaker -->|HTTP requests| Workflow
    classDef External padding: 50px, width: 200px, font-size: 25px
    classDef Stream stroke-dasharray: 5 5
```

## Design

Media Atom Maker (MAM) is architected as a collection of resources, primarily a Scala Play App with a React frontend
that facilitates the management and publishing of video content, typically hosted in YouTube. This system is built using
various AWS services, including S3 buckets, Lambdas, Step Functions, and DynamoDB for content state management. It
integrates with Composer, YouTube, the Content API (CAPI), and Pluto for content its publishing workflows.

### EC2 Backend

The main backend of MAM is a Scala Play App running on EC2 serving the client side and providing an API to manage media
atoms and other metadata. It manages the content state using DynamoDB tables.

### Client Side

The client side is a React app bundled using Vite and served via the Play app. The project uses Redux for state
management.

### Uploader

The backend grants browser credentials for direct upload of media content to an S3 bucket. Objects are then picked up by
the Uploader step function and processed. These assets are either sent to YouTube or processed and added to the
`uploads.guim.co.uk` S3 bucket.

[Uploader Step Function README](../uploader/README.md)

### Cloudformation Stacks

The application has several cloudformation stacks:

- media-atom-maker-{Stage} – Primary resources stack
- media-atom-maker-{Stage}-dynamo – DynamoDB tables (AtomMaker, PublishedAtomMaker and ManualPlutoAtomMaker)
- media-atom-pipeline-{Stage} – Uploader step function resources
- media-atom-maker-DEV – Resource for integration with local testing (different template to the primary stack)

## Integrations

### Pluto

Pluto is the Guardian's media asset management system, confusingly also referred to as MAM. Its taxonomy consists of
Commissions, Projects and Deliverables. It provides a way to manage media assets, and expects users to have access to
the shared asset drive.

Media Atom Maker and Pluto communicate via Kinesis and SQS to transmit updates and metadata between each system.

Commissions and Projects are ingested via the Pluto Message Ingestion Kinesis stream and stored in DynamoDB. These can
then be applied to media atoms to provide navigation between content and their assets.

Media Atom Maker events are sent to Pluto via the Upload To Pluto Kinesis stream. These include notifications of when
videos are uploaded, and when Pluto projects are assigned to atoms.

There is also an EC2 [LaunchDetector service](https://github.com/guardian/multimedia-launchdetector-v3) which watches CAPI's firehose stream for atom changes and makes HTTP
requests to Pluto services.

[Pluto Message Ingestion](../pluto-message-ingestion/README.md)

### CAPI

Media atoms are published via CAPI using the Publish Atom SNS topic.

The Expirer lambda periodically checks the state of media atoms in CAPI and if they are expired updates their status in
YouTube to private.

The Scheduler lambda periodically checks the state of media atoms in preview CAPI and if they are scheduled for launch sends a
request to the media atom maker backend API to publish them.

### YouTube

YouTube is the main host for Guardian media content. Media Atom Maker communicates with YouTube via its APIs.

[Documentation](08-youtube.md)

### Composer

Composer is the Guardian's digital CMS. Media Atom Maker makes calls to the Composer API in order to create content
pages for media atoms.

It also allows videos to be embedded into Composer content using an iFrame interface of the MAM UI to choose the right
atom.

[Documentation](09-composer-integration.md)

### Workflow

[Workflow](https://github.com/guardian/workflow/) is a Guardian tool, used for tracking content in production. Media Atom Maker can display the Workflow state for any atom tracked in Workflow, and offers users the ability to edit it.

On loading the Workflow tab in the UI, the atom is looked up in workflow.

```mermaid
---
title: Workflow integration
---
sequenceDiagram
  actor User
  participant MAMFrontend
  User ->> MAMFrontend: Navigate to an atom’s Workflow tab
  MAMFrontend -)+ Workflow: GET /api/sections
  MAMFrontend -)+ Workflow: GET /api/statuses
  MAMFrontend -)+ Workflow: GET /api/priorities
  MAMFrontend -)+ Workflow: GET /api/atom/<ID>
  Workflow --)- MAMFrontend: sections
  Workflow --)- MAMFrontend: statuses
  Workflow --)- MAMFrontend: priorities
  Workflow --)- MAMFrontend: atom info
```
