# Architecture

## Graph
```mermaid
graph LR
    Pluto((Pluto)):::External
    YouTube((YouTube)):::External
    CAPI((CAPI)):::External
    Composer((Composer))

    uploads-to-pluto{{uploads-to-pluto<br/>Kinesis}}:::Stream
    media-atom-maker-ingested-videos{{media-atom-maker-ingested-videos<br/>SQS}}:::Stream
    PlutoIntegrationIncomingStream{{PlutoIntegrationIncomingStream<br/>Kinesis}}:::Stream

    uploads-to-pluto --> Pluto
    Pluto --> media-atom-maker-ingested-videos
    Pluto --> PlutoIntegrationIncomingStream

    SendToPluto[SendToPluto<br/>lambda] --> uploads-to-pluto

    media-atom-maker-ingested-videos --> |Akka Actor| MediaAtomMaker{Media Atom Maker<br/>Scala Play<br/>EC2}

    PlutoIntegrationIncomingStream --> PlutoMessage[Pluto Message<br/>Ingestion<br/>Lambda]

    PlutoMessage --> MediaAtomMaker

    MediaAtomMaker --> PlutoCommissions[(Pluto<br/>Commissions<br/>dynamo<br/>table)]
    MediaAtomMaker --> PlutoProjects[(Pluto<br/>Projects<br/>dynamo<br/>table)]
    MediaAtomMaker --> MediaAtomMakerProd[(MediaAtom<br/>Maker<br/>dynamo<br/>table)]
    MediaAtomMaker --> PublishedMedia[(PublishedMedia<br/>AtomMaker<br/>dynamo<br/>table)]

    MediaAtomMaker --> ClientSide[Client Side UI<br/>React<br/>Vite]
    MediaAtomMaker --> MediaAtomMakerUpload[[Media Atom<br/>Maker Upload<br/>S3 bucket]]

    ClientSide --> MediaAtomMakerUpload
    ClientSide --> |API| Composer

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

    PublishAtom --> CAPI


    Expirer[Expirer lambda<br/>Every 15 minutes]

    CAPI --> Expirer
    Expirer --> YouTube

    classDef External padding:50px, width:200px, font-size:25px
    classDef Stream stroke-dasharray: 5 5
```

## Design


## Integrations

### Pluto

[Pluto Message Ingestion](../pluto-message-ingestion/README.md)

### CAPI

### YouTube

[Uploader](../uploader/README.md)

[Further reading](08-youtube.md)

### Composer

[Further reading](09-composer-integration.md)
