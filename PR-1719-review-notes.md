# PR #1719 review notes

Pull request: [Add Iconik entity validation endpoint](https://github.com/guardian/media-atom-maker/pull/1719)

Collected from the PR reviews and inline review threads through 22 October 2026. Deployment-bot comments are omitted.

## Automated review findings

The automated review on 19 October raised these points:

- [Working-group parent mismatches](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4315377780) — `IconikWorkingGroup` has no cached parent, so treating the missing cached value as `None` can report an incorrect parent mismatch. Return `null` when the expected parent is unavailable; keep the comparison for projects and commissions. The 21 October automated review listed this as resolved.
- [Synchronous AWS reads on the shared dispatcher](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4315377851) — Secrets Manager retrieval and three DynamoDB scans run synchronously before the action returns a `Future`. Move these reads to a dedicated bounded blocking execution context, then chain the Iconik requests. The 21 October automated review listed this as resolved.

The automated review on 21 October raised two further points:

- [Retry failed Secrets Manager credential loads](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4335114019) — a failed constructor-time credential load remains cached for the controller's lifetime, so requests continue returning 503 after AWS recovers. Retry failures and keep the synchronous call off the request execution context.
- [Add controller validation action test coverage](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4335114146) — current tests cover only `parentMatches`; add controller-level tests for authentication, credential parsing, mixed Iconik 200/404 responses and summary counts, plus 503/502 failures.

**Implementation update (22 October):** Missing or invalid Iconik credentials and Secrets Manager retrieval failures now throw during controller construction, failing application startup instead of returning 503 on each request. This adopts the fail-fast approach discussed in the config-parsing review comment; it does not retry failed secret loads.

## Reviewer comments and discussion

### Whether this should be an endpoint or script

- [lindseydew, 22 October](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4346960950): “Just curios why you're adding an endpoint here? Could this be a standalone script?”
- [AWare, 22 October](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4349311333): “I want to get this checking live so there's the opportunity to use the iconik API instead of our dynamo cache as we move from linking to projects to linking to commissions”
- [AWare, 22 October](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4349402921): “it is kind of script grade code though :/”
- [lindseydew, 22 October](https://github.com/guardian/media-atom-maker/pull/1719#pullrequestreview-5612424258): “Nice, I think the logic looks fine, though, I think restructuring and moving to a standalone script would make it a bit easier to follow”

### Implementation and readability

- [lindseydew, 22 October](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4347034288), on `app/controllers/IconikValidationController.scala`: “I think this config parsing could be a bit more readable with a for-comp. Or even ditching the either and throwing a RunTime exception if credential data is not available”
- [lindseydew, 22 October](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4347050174), on `app/controllers/IconikValidationController.scala`: “I would separate calling the API and getting a response, and then checking the result based on the response”
- [lindseydew, 22 October](https://github.com/guardian/media-atom-maker/pull/1719#discussion_r4347065328), on `test/controllers/IconikValidationControllerTest.scala`: “If you split out the computation from the printing, the signature here could be a straightforward boolean”

**Implementation update (22 October):** Iconik requests now return a `WSResponse` from `fetchIconikEntity`; `checkIconikResponse` handles response interpretation separately.
