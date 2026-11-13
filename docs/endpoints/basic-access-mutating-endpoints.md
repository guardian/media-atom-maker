# Mutating endpoints (POST/PUT/DELETE) gated by `basicAccess` only

These endpoints change state but require nothing beyond the baseline
`media_atom_maker_access` permission (enforced by `validateUser` in
`PanDomainAuthActions.scala`).

## Pan-domain user (`APIAuthAction`), basicAccess only

| Method | Path | Action | Description |
|---|---|---|---|
| PUT | `/api/atoms/:id` | `Api.updateMediaAtom` | Saves edits to an atom's metadata (title, description, channel, etc.) to the preview store |
| POST | `/api/atoms/:id/assets` | `Api.addAsset` | Adds a video asset to an atom from a URI (e.g. a YouTube URL) |
| DELETE | `/api/atoms/:id/assets` | `Api.deleteAsset` | Removes a single asset version from an atom |
| DELETE | `/api/atoms/:id/asset-list` | `Api.deleteAssetList` | Removes a list of assets (e.g. all files for a version) from an atom |
| PUT | `/api/atom/:id/asset-active` | `Api.setActiveAsset` | Marks an asset version as the atom's active video (updating YouTube/thumbnail as needed) |
| PUT | `/api/atom/:id/reset-duration-from-active` | `Api.resetDurationFromActive` | Refreshes the atom's duration from the active YouTube asset |
| POST | `/api/workflow/atoms` | `Api.createWorkflowMediaAtom` (CORS for Workflow) | Creates a new media atom from a Workflow request |
| POST | `/api/uploads` | `UploadController.create` | Starts a video upload: claims the next asset version and creates the upload record/parts |

## `LookupPermissions` (loads the user's permissions but never checks them)

| Method | Path | Action | Description |
|---|---|---|---|
| POST | `/api/uploads/:id/credentials` | `UploadController.credentials` | Issues temporary AWS credentials to upload a part of the file to S3 |
| POST | `/api/uploads/:id/:version/subtitle-file` | `UploadController.uploadSubtitleFile` | Attaches a subtitle file to an asset version and reprocesses the upload |
| DELETE | `/api/uploads/:id/:version/subtitle-file` | `UploadController.deleteSubtitleFile` | Removes the subtitle file from an asset version and reprocesses the upload |

## `APIHMACAuthAction` (HMAC shared secret, or pan-domain user with basicAccess)

| Method | Path | Action | Description |
|---|---|---|---|
| POST | `/api/atoms` | `Api.createMediaAtom` | Creates a new media atom |
| PUT | `/api/atom/:id/publish` | `Api.publishMediaAtom` (also checks `set_videos_on_all_channels_public`, but only for YouTube channels that require it) | Publishes the atom (preview → live), updating YouTube metadata and thumbnail |
| DELETE | `/api/pluto/commissions/:id` | `PlutoController.deleteCommission` | Deletes a Pluto commission and its projects |
| PUT | `/api/pluto/projects` | `PlutoController.upsertProject` | Creates or updates a Pluto project record |
| POST | `/api/pluto/resend/:id` | `PlutoController.resendAtomMessage` | Re-sends the atom's current-version metadata to Pluto (resync) |
| DELETE | `/api/iconik/commissions/:id` | `IconikController.deleteCommission` | Deletes an Iconik commission and its projects |
| PUT | `/api/iconik/projects` | `IconikController.upsertIconikData` | Creates or updates Iconik working group/commission/project data |

## Not basicAccess-only

- `DELETE /api/atom/:id` requires **`delete_atom`** (`CanDeleteAtom`).
- `/reindex-*` POST/DELETE are handled by the library `ReindexController` and do not use pan-domain auth.

> Note: `add_self_hosted_asset` is not enforced server-side, including on
> `POST /api/uploads` with `selfHost`.

See also: [API Endpoints: Authentication & Permissions](./authentication-and-permissions.md)
