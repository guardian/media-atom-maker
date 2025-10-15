package util

import com.amazonaws.services.elastictranscoder.model.ListJobsByPipelineRequest
import model.transcoder.JobStatus

import scala.jdk.CollectionConverters._
import java.time.Duration

class Transcoder(awsConfig: AWSConfig) {

  private lazy val transcoderJobsCache =
    Memoize(updateJobsStatus(), Duration.ofSeconds(20))

  def getJobsStatus = transcoderJobsCache.get

  private def updateJobsStatus() = {
    val pipelineRequest: ListJobsByPipelineRequest =
      new ListJobsByPipelineRequest()
    pipelineRequest.setPipelineId(awsConfig.transcodePipelineId)
    // assumption this will be used sparingly so no need to paginate results

    val jobsByPipeline =
      awsConfig.transcoderClient.listJobsByPipeline(pipelineRequest)

    val jobs = jobsByPipeline.getJobs.asScala.toList.map { job =>
      val statusDetail =
        if (job.getStatus.equals("Error")) Some(job.getOutput.getStatusDetail)
        else None
      JobStatus(job.getId, job.getStatus, statusDetail)
    }
    jobs
  }
}
