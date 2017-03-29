package controllers

import com.gu.media.pluto.PlutoProject
import com.gu.pandahmac.HMACAuthActions
import data.{DataStores, UnpackedDataStores}
import play.api.libs.json.Json
import play.api.mvc.Controller

class PlutoController (val authActions: HMACAuthActions, override val stores: DataStores) extends Controller
  with UnpackedDataStores
  with JsonRequestParsing {

  import authActions.APIHMACAuthAction

  def listProjects() = APIHMACAuthAction {
    val plutoProjects = stores.plutoProjectStore.list()
    Ok(Json.toJson(plutoProjects))
  }

  def createProject() = APIHMACAuthAction { implicit req =>
    parse[PlutoProject](req) { data: PlutoProject => {
      stores.plutoProjectStore.put(data)
      Ok(Json.toJson(data))
    }}
  }

  def getProject(id: String) = APIHMACAuthAction {
    stores.plutoProjectStore.get(id) match {
      case Some(p) => Ok(Json.toJson(p))
      case _ => NotFound
    }
  }

  def updateProject(id: String) = APIHMACAuthAction { implicit req =>
    parse[PlutoProject](req) { data: PlutoProject => {
      stores.plutoProjectStore.update(id, data) match {
        case Some(_) => Ok(Json.toJson(data))
        case None => NotFound
      }
    }}
  }
}
