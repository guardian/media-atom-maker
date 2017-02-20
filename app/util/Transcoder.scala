package util

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.agent.Agent
import com.amazonaws.services.elastictranscoder.model.ListJobsByPipelineRequest
import model.transcoder.JobStatus

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class Transcoder @Inject()(awsConfig: AWSConfig, val system: ActorSystem) {

  val agent: Agent[List[JobStatus]] = Agent(List.empty)

  system.scheduler.schedule(0.seconds, 2.minutes)(updateJobsStatus)

  def updateJobsStatus {
    val pipelineRequest: ListJobsByPipelineRequest = new ListJobsByPipelineRequest()
    pipelineRequest.setPipelineId(awsConfig.transcodePipelineId)
    //assumption this will be used sparingly so no need to paginate results

    val jobsByPipeline = awsConfig.transcoderClient.listJobsByPipeline(pipelineRequest)

    val jobs = jobsByPipeline.getJobs.asScala.toList.map { job =>

      val statusDetail = if (job.getStatus.equals("Error")) Some(job.getOutput.getStatusDetail) else None
      val x = JobStatus(job.getId, job.getStatus, statusDetail)
      x
    }

    agent send jobs
  }

  def getJobsStatus = agent.get()

}
