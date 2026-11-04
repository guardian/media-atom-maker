# API Endpoints: Authentication & Permissions

This document summarises every HTTP endpoint defined in `conf/routes`, its
authentication requirement, and the permission needed to perform the action.

## Authentication model

Three auth wrappers are used (from Guardian's `pandahmac`), plus plain `Action`:

| Wrapper | Who can call | Notes |
|---|---|---|
| `AuthAction` | Logged-in browser user (pan-domain SSO) only | |
| `APIAuthAction` | Browser user **OR** HMAC machine request | |
| `APIHMACAuthAction` | HMAC machine-to-machine only (e.g. scheduler lambda) | |
| `Action` | Unauthenticated | |

**Baseline permission:** every pan-domain user must pass `validateUser` —
`@guardian.co.uk` email + MFA + the **`media_atom_maker_access`** (`basicAccess`)
permission. So *all* `AuthAction`/`APIAuthAction` endpoints implicitly require
`media_atom_maker_access`. HMAC requests bypass the permission check (treated as
the lambda).

## Atoms (`Api.scala`)

| Method & Path | Auth | Extra permission |
|---|---|---|
| GET `/api/atoms` | APIAuthAction | basicAccess |
| POST `/api/atoms` | APIHMACAuthAction | HMAC only |
| GET `/api/atoms/:id` | APIAuthAction | basicAccess |
| PUT `/api/atoms/:id` | APIAuthAction | basicAccess |
| DELETE `/api/atom/:id` | APIAuthAction + `PermissionedAction` | **`delete_atom`** |
| GET `/api/grid/crop-variations` | APIAuthAction | basicAccess |
| GET `/api/atoms/:id/published` | APIAuthAction | basicAccess |
| PUT `/api/atom/:id/publish` | APIHMACAuthAction | HMAC; publish logic checks **`set_videos_on_all_channels_public`** |
| POST `/api/atoms/:id/assets` | APIAuthAction | basicAccess |
| DELETE `/api/atoms/:id/assets` | APIAuthAction | basicAccess |
| DELETE `/api/atoms/:id/asset-list` | APIAuthAction | basicAccess |
| PUT `/api/atom/:id/asset-active` | APIAuthAction | basicAccess |
| PUT `/api/atom/:id/reset-duration-from-active` | APIAuthAction | basicAccess |
| OPTIONS `/api/workflow/*url` | **None** (CORS preflight) | — |
| POST `/api/workflow/atoms` | APIAuthAction (CORS-restricted to workflow URL) | basicAccess |

## Pluto (`PlutoController.scala`)

| Method & Path | Auth |
|---|---|
| GET `/api/pluto/commissions` | APIAuthAction |
| GET `/api/pluto/commissions/:id` | APIAuthAction |
| GET `/api/pluto/commissions/:id/projects` | APIAuthAction |
| DELETE `/api/pluto/commissions/:id` | APIHMACAuthAction |
| PUT `/api/pluto/projects` | APIHMACAuthAction |
| GET `/api/pluto/projects/:id` | APIAuthAction |
| POST `/api/pluto/resend/:id` | APIHMACAuthAction |

## Iconik (`IconikController.scala` / `IconikValidationController.scala`)

| Method & Path | Auth |
|---|---|
| GET `/api/iconik/commissions` | APIAuthAction |
| GET `/api/iconik/commissions/:id` | APIAuthAction |
| GET `/api/iconik/commissions/:id/projects` | APIAuthAction |
| DELETE `/api/iconik/commissions/:id` | APIHMACAuthAction |
| PUT `/api/iconik/projects` | APIHMACAuthAction |
| GET `/api/iconik/projects/:id` | APIAuthAction |
| GET `/api/iconik/working-groups` | APIAuthAction |
| GET `/api/iconik/working-groups/:id` | APIAuthAction |
| GET `/api/iconik/working-groups/:id/commissions` | APIAuthAction |
| GET `/api/iconik/validate` | AuthAction |
| GET `/videos/:id/open-in-iconik` | AuthAction |

## Uploads (`UploadController.scala`)

| Method & Path | Auth | Notes |
|---|---|---|
| GET `/api/uploads` | APIAuthAction | basicAccess |
| POST `/api/uploads` | APIAuthAction | basicAccess (self-hosted uploads gated in UI by **`add_self_hosted_asset`**) |
| POST `/api/uploads/:id/credentials` | `LookupPermissions` (APIAuthAction + loads user's permission set) | basicAccess |
| POST `/api/uploads/:id/:version/subtitle-file` | `LookupPermissions` | basicAccess |
| DELETE `/api/uploads/:id/:version/subtitle-file` | `LookupPermissions` | basicAccess |

## YouTube (`Youtube.scala`)

| Method & Path | Auth | Notes |
|---|---|---|
| GET `/api/youtube/categories` | AuthAction | basicAccess |
| GET `/api/youtube/channels` | AuthAction | filters channels by **`set_videos_on_all_channels_public`** |
| GET `/api/youtube/video-info/:id` | AuthAction | basicAccess |

## Support / UI / misc

| Method & Path | Auth |
|---|---|
| GET `/support/previewCapi/*path` | APIAuthAction |
| GET `/support/liveCapi/*path` | APIAuthAction |
| GET `/`, `/videos`, `/videos/:id`, `/videos/:id/upload`, `/create`, `/help`, `/training` | AuthAction |
| GET `/training/on`, `/training/off` | AuthAction |
| GET `/reauth` | AuthAction |
| GET `/oauthCallback` | **None** (OAuth handshake) |
| GET `/healthcheck` | **None** |
| GET `/assets/*file` | **None** (static assets) |
| POST/GET/DELETE `/reindex-preview`, `/reindex-publish` | Provided by library `com.gu.atom.play.ReindexController` (auth defined in atom-publisher-lib, not this repo) |

## Permissions reference (`common/.../Permissions.scala`)

- **`media_atom_maker_access`** — baseline; required for all authenticated UI/API access.
- **`delete_atom`** — deleting an atom.
- **`set_videos_on_all_channels_public`** — publishing/marking videos public across all channels.
- **`add_self_hosted_asset`** — adding self-hosted assets (surfaced to client, enforced in UI).
- **`pinboard`** (app `pinboard`) — surfaced in the user's permission set, not gating a route here.

See also: [Mutating endpoints gated by `basicAccess` only](./basic-access-mutating-endpoints.md)
