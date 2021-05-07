# Composer Integration

Media Atom Maker integrates with Composer via the API. A new Composer piece can be made via the "Create Video Page" button in the atom editor, this will use the `createContent` endpoint to generate a new video article with the provided fields. Subsequent changes to the atom will be reflected in Composer via update requests made to the `videoPage` endpoint.

## Limitations

Currently most of the Composer business logic is situated in the client-side app, this means the API is unable to hydrate all the necessary fields. For example, the logic to determine the correct path for a piece lives within the Composer client. As a result, MAM makes no attempt to set this value.

When a new incomplete piece is opened in the Composer client, it will covertly make update requests to hydrate the missing data. This results in Central Production advising users to navigate to the page and open the furniture panel, to avoid potential issues.

## Possible Solution

The ideal solution would be to move the Composer logic to the backend, allowing MAM to populate everything it requires via the API.
