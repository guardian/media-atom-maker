package util

import com.gu.media.logging.Logging
import io.sentry.Sentry

import javax.inject.{Inject, Singleton}
import play.api.Configuration

/** Single source of truth for Sentry setup. Initialises the SDK once at
  * startup; the error handler, the tracing filter and the client-side config
  * all read `enabled` from here so server and browser can never disagree about
  * whether Sentry is on.
  */
@Singleton
class SentryConfig @Inject() (config: Configuration) extends Logging {

  val stage: String = config.getOptional[String]("stage").getOrElse("DEV")

  /** Renamed from the legacy `raven.url`. That key is still set by the private
    * conf in S3 and points at the old per-stage project; by reading a different
    * key we ignore it entirely, so there is no precedence race while that dead
    * entry is cleaned up.
    */
  val dsn: Option[String] = config.getOptional[String]("sentry.dsn")

  private val localEnabled: Boolean =
    config.getOptional[Boolean]("sentry.local.enabled").getOrElse(false)

  val enabled: Boolean = dsn.isDefined && (stage != "DEV" || localEnabled)

  /** Spans are billed individually and the browser SDK already emits a lot of
    * them, so PROD is sampled down. SENTRY_TRACES_SAMPLE_RATE overrides the
    * stage default for controlled cost tuning.
    */
  val tracesSampleRate: Double = {
    val defaultRate = if (stage.equalsIgnoreCase("PROD")) 0.1d else 1.0d
    val rate = config
      .getOptional[Double]("sentry.traces.sample-rate")
      .getOrElse(defaultRate)
    require(
      rate >= 0.0d && rate <= 1.0d,
      "sentry.traces.sample-rate must be between 0.0 and 1.0"
    )
    rate
  }

  /** Session replays are disabled by default; when enabled, only replays
    * attached to error events are uploaded. */
  val replayEnabled: Boolean =
    config.getOptional[Boolean]("sentry.replay.enabled").getOrElse(false)

  /** Ties server events to the same release as the browser bundle, which the
    * Sentry Vite plugin stamps from the commit SHA at build time. CI sets
    * BUILD_VCS_NUMBER to the same SHA so both sides agree.
    *
    * `gitCommitId` falls back to "unknown" when git is unavailable; reporting
    * that as a release would group unrelated deploys together, so skip it.
    */
  val release: Option[String] =
    Some(app.BuildInfo.gitCommitId).filter(_ != "unknown")

  if (enabled) {
    Sentry.init(options => {
      options.setDsn(dsn.get)
      options.setEnvironment(stage)
      options.setAttachStacktrace(true)
      options.setTracesSampleRate(tracesSampleRate)
      release.foreach(options.setRelease)
    })
    log.info(
      s"Sentry enabled for stage=$stage tracesSampleRate=$tracesSampleRate " +
        s"release=${release.getOrElse("<none>")}"
    )
  } else if (stage == "DEV") {
    log.info(
      s"Sentry disabled for stage=$stage (set sentry.local.enabled=true to enable)"
    )
  } else {
    // A deployed stage with no DSN means we are running blind. Log as error so
    // this surfaces rather than sitting unnoticed in a startup warning.
    log.error(
      s"Sentry is DISABLED on stage=$stage - no `sentry.dsn` configured, so no " +
        "errors or traces will be reported for this stage"
    )
  }
}
