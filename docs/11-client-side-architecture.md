# Client side architecture

## Description

This document intends to explain the structure of the Media Atom Maker client, provide some detail of the work that has
been undertaken to modernise it, and what direction future development might be expected to take.

## Structure

### Technologies

- TypeScript (replacing JavaScript)
- React
- React Router
- Redux
- Redux Toolkit

### Overview

The Media Atom Maker client is a SPA React App, using Redux for state management and fetch requests to interact with the
backend API. Users can use the client to create atoms, add metadata, upload multiple assets and choose one to be
featured. Once published, Atoms can be featured in content in various place, but updated and managed in a single
place. Most assets are uploaded and hosted in YouTube, using the [uploader process](../uploader/README.md).

Redux manages much of the application state and is divided into slices, each managing their own state by responding to
dispatched actions. Good examples of this are:

- [Video Slice](../public/video-ui/src/slices/video.ts)
- [Error Slice](../public/video-ui/src/slices/error.ts)

When editing atom metadata, most edits are applied immediately to the Redux state. This is typically done via
the [ManagedForm](../public/video-ui/src/components/ManagedForm/ManagedForm.jsx) components. This can present difficulty
when save requests result in failure or edits are cancelled. In these cases there is no previous state to fall back to
as edits have been immediately applied. The application also orchestrates saves via Thunks, which are often made up of
several sequenced fetch requests, if parts of the process fail, it can be unclear what action users should take to
respond.

Save requests often update the entire atom model, this limits the amount users can work concurrently on atoms.
Therefore, users rely on Presence indicators to determine whether it is safe to work on an atom. A mechanism exists to
[report and reject potentially conflicting writes](https://github.com/guardian/media-atom-maker/blob/0d0535c5d6158a4b077c4b23c8df01790be61071/app/model/commands/UpdateAtomCommand.scala#L77-L85),
but it doesn't work for all endpoints.

## Improvements

Works has been undertaken to improve the codebase in an effort to reduce the number of bugs and improve the overall UX.

### Redux Toolkit

The Redux constructs have been [rewritten using Redux Toolkit](https://github.com/guardian/media-atom-maker/issues/1262)
in order to make the code more concise and the state and interactions of actions easier to reason about. Reducers and
Actions have been converted to TypeScript as slices following
the [RTK migration guide](https://redux.js.org/usage/migrating-to-modern-redux). To a lesser extent components have been
updated, but this has been limited by the use of React Component Classes and JavaScript.

### TypeScript Coverage

The project's overall TypeScript coverage has increased significantly, roughly doubling during the course of this work.
This has helped to identify potential bugs, improve the understanding and readability of the code, and generally make
refactoring safer.

## Future development

When developing the client further, a few key areas should be considered for improvement:

### Function Components

Many React components are written in the now outdated Component Class style. Rewriting these as function components will
make them more familiar to modern React developers. It also allows the use of hooks which work well with Redux and
RTK.

### Strict TypeScript

Continuing to improve the TypeScript coverage will make the project easier to work on in the future and provides
significant benefits to the Developer Experience.

Setting TypeScript to be `strict` would be a sensible improvement and help identify further potential bugs.

### Test coverage

The client is sparsely tested, but some examples do exist. When rewriting components, it would be worth adding test
coverage to ensure consistent behaviour and help document business logic.

### Improving telemetry

It is currently difficult to know which parts of the client are used and how heavily. Adding telemetry (possibly via the
[Telemetry Service](https://github.com/guardian/editorial-tools-user-telemetry-service)), would help to build an
understanding of how users interact with the tool. With this understanding, parts of the tools could be adjusted or even
removed, improving the UX for users.
