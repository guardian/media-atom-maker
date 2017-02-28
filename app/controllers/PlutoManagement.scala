package controllers

import cats.data.Xor
import com.gu.pandahmac.HMACAuthActions
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.query.UniqueKeys
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.{Configuration, Logger}
import com.gu.scanamo._
import com.gu.media.pluto.AtomResponse
import util.AWSConfig
import cats.data.Xor

class PlutoManagement(val authActions: HMACAuthActions, aws: AWSConfig) extends Controller {
  import authActions.APIHMACAuthAction

  def getAtoms = APIHMACAuthAction {
    val atoms = Scanamo.scan[AtomResponse](aws.dynamoDB)(aws.manualPlutoDynamo)
      .foldRight(List[AtomResponse]())((result, acc) => {
        result match {
          case Xor.Left(error) => {
            //Log error
            acc
          }
          case Xor.Right(result: AtomResponse) => result :: acc
        }
      })

    Ok(Json.toJson(atoms))
  }
}

