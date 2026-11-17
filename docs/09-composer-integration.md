# Composer Integration

Media Atom Maker integrates with Composer making [client requests](https://github.com/guardian/media-atom-maker/blob/2644c20d9af40db32ede28e91968d67d50f50613/public/video-ui/src/services/VideosApi.js#L199) to the API exposed by the `composer-backend` project . A new Composer piece can be made via the "Create Video Page" button in the atom editor, this will use the [createContent](https://github.com/guardian/flexible-content/blob/main/flexible-content-apiv2/src/main/scala/com/gu/flexiblecontent/apiv2/dispatcher/ApiDispatcher.scala#L373) endpoint to generate a new video article with the [provided fields](https://github.com/guardian/flexible-content/blob/main/flexible-content-common/src/main/scala/com/gu/flexiblecontent/model/MAMVideoPageInformation.scala).
Subsequent changes to the atom will be reflected in Composer via update requests made to the [videoPage](https://github.com/guardian/flexible-content/blob/main/flexible-content-apiv2/src/main/scala/com/gu/flexiblecontent/apiv2/dispatcher/ApiDispatcher.scala#L630) endpoint.

## Path Synchronisation

After creating a video page and adding its video block, MAM calls Composer's `POST /api/content/{id}/sync-path` endpoint. Composer derives the path from the page fields and updates the path identifier on the draft and live records. MAM then uses the returned `proposedPath` for its Content API lookup, so the page has a headline-based path without requiring the furniture panel to be opened.

MAM passes `Europe/London` as the timezone, matching Composer's current path-sync configuration. Editors can still change the path later in Composer.
