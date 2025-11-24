package util

import software.amazon.awssdk.services.elastictranscoder.model.ListJobsByPipelineRequest

import model.transcoder.JobStatus

import scala.jdk.CollectionConverters._
import java.time.Duration

class Transcoder(awsConfig: AWSConfig) {

  private lazy val transcoderJobsCache =
    Memoize(updateJobsStatus(), Duration.ofSeconds(20))

  def getJobsStatus = transcoderJobsCache.get

  private def updateJobsStatus() = {
    val pipelineRequest: ListJobsByPipelineRequest = ListJobsByPipelineRequest
      .builder()
      .pipelineId(awsConfig.transcodePipelineId)
      .build()

    val jobsByPipeline =
      awsConfig.transcoderClient.listJobsByPipeline(pipelineRequest)

    val jobs = jobsByPipeline
      .jobs()
      .asScala
      .toList
      .map({ job =>
        val statusDetail =
          Option.when(job.status().equals("Error"))(job.output().statusDetail())
        JobStatus(job.id(), job.status(), statusDetail)
      })
    jobs
  }
}
