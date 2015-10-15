package controllers

import akka.actor.{Props, ActorSystem}
import play.api.mvc._
import play.api.libs.json.{JsValue, JsError, Json}
import play.api.routing.JavaScriptReverseRouter
import play.api.Play.current

case class FormResponse(name:String,phoneNumber:String,interval:String,
                         reminderMessage:String,fromTime:String,toTime:String)

class Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Form Input"))
  }

  //Creating Socket
  def socket = WebSocket.acceptWithActor[JsValue, String] { request => out =>
    MyWebSocketActor.props(out)
  }

  // For Handling Ajax Request. Currently Not Implemented
  def ajaxCall = Action(parse.tolerantJson) { implicit request =>
    implicit val writes = Json.writes[FormResponse]
    Ok(Json.toJson(request.body))
  }

  // JavaScript Routes for Play Framework
  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.Application.index,
        routes.javascript.Application.ajaxCall,
        routes.javascript.Application.socket
      )
    ).as("text/javascript")
  }

}
