package controllers

import akka.actor.{ActorLogging, Actor, Props, ActorRef}
import play.api.libs.json.{Json, JsValue}
import com.github.nscala_time.time.Imports._

/**
 * @author Ronak.Agrawal - 10/5/2015 - controllers 
 */


case class ActorSchedulerRequest(name:String,phoneNumber:String,
                        reminderMessage:String,fromTime:DateTime,toTime:DateTime)

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor with ActorLogging {

  override def postStop() = {
    log.info(s"Closing Actor")
  }

  override def preStart() = {
    log.info(s"Starting Actor")
  }

  def receive = {

    // Sending Response toFE
    case response:ActorSchedulerRequest => {

      val maxTry = 5

      // Check if Scheduler should sleep or send the reminder message
      val currentTime = DateTime.now
      val fromTimeBool = response.fromTime <= currentTime
      val toTimeBool = currentTime <= response.toTime

      log.info("Running Scheduler")

      if (fromTimeBool && toTimeBool) {

        // Scheduler is Sleeping
        out ! s"""${currentTime} ${response.phoneNumber} - Scheduler is "Sleeping" ${response.name}"""

      } else {

        var msgSend = false
        for (i <- 1 to maxTry if (msgSend == false)) {

          // Random - Checking If Sending Msg was Successful or Not
          val shouldWeSendMsg: Boolean = math.random < 0.25

          if (shouldWeSendMsg) {
            msgSend = true
            out ! s"""${currentTime} ${response.phoneNumber} - ${response.reminderMessage} ${response.name}"""
          } else {
            out ! s"""${currentTime} ${response.phoneNumber} - Sending Msg Failed- ${i} ${response.name}"""
          }
        }

      }

    }

    // Get The Request From FE for Socket
    case request : JsValue =>
      implicit val reads1 = Json.reads[FormResponse]

      val requestData = request.asOpt[FormResponse]

      if(requestData isDefined) {

        //Convert the From & To Date ( When Scheduler will sleep ) to Joda Date Time Object
        val jodaFrom = DateTime.parse(s"""${DateTime.now.toLocalDate} ${requestData.get.fromTime}""",
          DateTimeFormat.forPattern(s"yyyy-MM-dd HH:mm"))
        val jodaTo = DateTime.parse(s"""${DateTime.now.toLocalDate} ${requestData.get.toTime}""",
          DateTimeFormat.forPattern(s"yyyy-MM-dd HH:mm"))

        // Create a case class object
        val actorScheduler = ActorSchedulerRequest(
          name = requestData.get.name,
          phoneNumber = requestData.get.phoneNumber,
          reminderMessage = requestData.get.reminderMessage,
          fromTime = jodaFrom,
          toTime = jodaTo
        )

        // Create the Scheduler. It runs at an interval
        implicit val dispatcher = context.system.dispatcher
        import scala.concurrent.duration._
        context.system.scheduler.schedule(1.millisecond,
          requestData.get.interval.toLong.minutes,
          self,
          actorScheduler)

        log.info("Initalizing Scheduler")
        out ! s"""Initalizing Scheduler for ${actorScheduler.name}"""
      }
  }
}
