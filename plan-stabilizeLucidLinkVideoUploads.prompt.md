## Plan: Stabilize LucidLink Video Uploads

Use the reproducible 500 MB file / 100 MB LucidLink cache case to isolate whether the crash is caused by explicit JavaScript buffering, AWS checksum handling, Chromium file read-ahead, or the 100 MB object size. Preserve the fast direct-to-S3 path for normal uploads; only add local/server staging if bounded reads cannot make that path reliable.

**Steps**
1. Establish a repeatable baseline and diagnostics. Record Chrome version/crash ID, LucidLink bytes fetched, per-part read-start/read-end/S3-start/S3-end timestamps, JS heap where available, and S3 objects completed. Run the same case several times so success criteria are measurable: no browser crash, bounded LucidLink lead, correct final S3 object, and no material regression for a fully local file.
2. Build a frontend-only experiment matrix around `uploadPart` in `UploadsApi.js`. Compare the current `slice.arrayBuffer()` body against passing the bounded `Blob` slice directly to the AWS SDK/XHR handler; independently compare the SDK default request checksum behavior with `requestChecksumCalculation: WHEN_REQUIRED`. Keep one in-flight part and the existing 100 MB server boundaries so each variable is isolated. Verify the SDK/XHR path accepts `Blob`, sends the expected content length, and does not silently re-materialize the whole body.
3. If step 2 still crashes, test smaller server-generated object sizes in `Upload.calculateChunks`, starting at 25 MiB and also measuring 10/50 MiB. Keep sequential upload behavior. Validate downstream S3 multipart-copy constraints: every non-final object must remain at least 5 MiB, total parts must remain below S3's 10,000-part limit, and Step Functions/Lambda duration, transition count, credentials calls, and cleanup costs remain acceptable. Update chunk property tests and the end-to-end upload test for the selected size.
4. Add resilience after the crash trigger is controlled. Introduce bounded per-part retries with fresh credentials, exponential backoff, abort support, and resume-from-existing-parts behavior. Treat retries as recovery from network/S3/filesystem errors, not as a fix for a whole-Chrome crash; persistence or server-side knowledge is required for recovery after browser restart.
5. If direct upload remains unreliable, prototype an opt-in compatibility path rather than slowing every upload. Preferred first fallback: copy the selected file in small backpressured reads to OPFS, confirm quota before starting, then upload from the staged browser-local file and delete it on completion/cancel. Compare this with a server-staging endpoint only if OPFS quota/policy is unsuitable; server staging adds infrastructure, storage lifecycle, security, and a second transfer phase but fully decouples S3 processing from LucidLink.
6. Roll out the smallest successful direct-path change behind a configuration flag, retaining metrics for crash-adjacent incomplete uploads, read duration, part duration, retries, total upload time, and browser/SDK version. Expose compatibility mode explicitly because a browser cannot reliably detect that a selected file resides on LucidLink.

**Relevant files**
- `./public/video-ui/src/services/UploadsApi.js` — modify `uploadPart` body handling, S3 client checksum/retry configuration, timing, abort, and optional staging integration.
- `./public/video-ui/src/slices/s3Upload.ts` — represent read/upload/staging progress and resumable/error states without conflating bytes read with bytes accepted by S3.
- `./common/src/main/scala/com/gu/media/upload/model/Upload.scala` — tune/configure chunk boundaries if frontend-only changes are insufficient.
- `./app/util/UploadBuilder.scala` — continue generating part keys from the chosen boundaries and, if needed, select a configured upload profile.
- `./app/controllers/UploadController.scala` — support profile/config selection, resume metadata, or a server-staging endpoint only for later phases.
- `./uploader/src/main/scala/com/gu/media/upload/MultipartCopyChunkInS3.scala` — validate smaller objects against S3 multipart-copy limits and increased part count.
- `./common/src/test/scala/com/gu/media/upload/UploadTest.scala` — update boundary, contiguity, minimum-size, and maximum-part-count tests.
- `./integration-tests/src/test/scala/VideoUploadTests.scala` — verify all independent objects arrive and are assembled correctly.

**Verification**
1. Re-run the 500 MB / 100 MB LucidLink-cache scenario for every experiment variant and compare Chrome survival, LucidLink fetched bytes, maximum lead over uploaded bytes, peak memory, and elapsed time.
2. Run a control matrix with the same file fully local and with ordinary network throttling; reject changes that materially reduce normal direct-to-S3 throughput.
3. Run focused frontend tests for body selection, sequential scheduling, retry/abort, progress, and cleanup using mocked credentials/S3 transport, adding a browser-level test because this path currently has no client upload coverage.
4. Run the Scala chunk property tests and integration upload workflow whenever object size or server metadata changes; include files around exact boundaries and a large file proving fewer than 10,000 assembled parts.
5. For OPFS, test insufficient quota, denied persistence, cancellation, tab reload, browser crash recovery, stale-file cleanup, and successful deletion after upload.

**Decisions**
- Keep uploads sequential; parallelism would increase filesystem pressure and is counter to the suspected failure mode.
- Do not begin with a longer timeout. The crash happens while Chrome is reading/buffering, and timeout changes neither memory nor read-ahead; adjust it only if diagnostics show ordinary request expiry.
- Do not rely on retry alone. A renderer/browser crash cannot execute retry logic, and retrying a problematic read may reproduce the crash.
- First recommendation: direct `Blob` body plus an explicit checksum experiment. It is the smallest change and removes at least one guaranteed 100 MB JS allocation/copy.
- Second recommendation: reduce object size, probably to 25 MiB, only if the frontend-only experiment is insufficient. This bounds each requested slice but raises downstream workflow cost.
- Compatibility fallback: OPFS staging is preferable to mandatory staging because it preserves normal upload speed. It requires enough local quota and temporarily consumes disk roughly equal to the file size.
- Server staging is last resort. A streaming proxy alone may alter buffering but does not guarantee LucidLink is fully decoupled; a true stage-then-S3 design does, at the cost of latency and infrastructure.
- Browser filesystem type is intentionally not auto-detected; web APIs do not reliably reveal LucidLink. Use an explicit user choice, admin configuration, or adaptive fallback based on prior failures.

**Further Considerations**
1. Capture a Chrome crash report and a minimal standalone reproduction using `Blob.slice().arrayBuffer()` and direct XHR. This will distinguish an application/AWS SDK issue from a Chromium/File API issue and provide a useful upstream bug report.
2. Confirm the maximum supported video size before selecting a smaller chunk size; S3 multipart assembly permits at most 10,000 parts and non-final parts must be at least 5 MiB.
3. Consider an operational workaround while code experiments run: instruct LucidLink users to pin/download the source locally or increase cache above the source size for critical uploads.