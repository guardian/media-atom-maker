package controllers

import play.api.libs.json.Json
import play.api.mvc.Controller

class PlutoManagement () extends Controller {


  def getAtoms() ={
    Ok(Json.toJson("hello"))
  }
}
