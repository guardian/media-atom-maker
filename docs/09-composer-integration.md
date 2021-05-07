# Composer Integration

Media Atom Maker integrates with Composer making [client requests](https://github.com/guardian/media-atom-maker/blob/2644c20d9af40db32ede28e91968d67d50f50613/public/video-ui/src/services/VideosApi.js#L199) to the API exposed by the `composer-backend` project . A new Composer piece can be made via the "Create Video Page" button in the atom editor, this will use the [createContent](https://github.com/guardian/flexible-content/blob/main/flexible-content-apiv2/src/main/scala/com/gu/flexiblecontent/apiv2/dispatcher/ApiDispatcher.scala#L373) endpoint to generate a new video article with the [provided fields](https://github.com/guardian/flexible-content/blob/main/flexible-content-common/src/main/scala/com/gu/flexiblecontent/model/MAMVideoPageInformation.scala).
Subsequent changes to the atom will be reflected in Composer via update requests made to the [videoPage](https://github.com/guardian/flexible-content/blob/main/flexible-content-apiv2/src/main/scala/com/gu/flexiblecontent/apiv2/dispatcher/ApiDispatcher.scala#L630) endpoint.

## Limitations

Currently most of the Composer business logic is situated in the client-side app, this means the API is unable to hydrate all the necessary fields. For example, the logic to determine the correct path for a piece lives within the [Composer client](https://github.com/guardian/flexible-content/blob/main/composer/src/js/services/content/path-manager.js#L134). As a result, MAM makes no attempt to set this value.

When a new incomplete piece is opened in the Composer client, it will covertly make update requests to hydrate the missing data. This results in Central Production advising users to navigate to the page and open the furniture panel, to avoid potential issues.

## Possible Solution

The ideal solution would be to move the Composer logic to the backend, allowing MAM to populate everything it requires via the API.
