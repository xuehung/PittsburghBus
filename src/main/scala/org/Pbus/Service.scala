package org.Pbus

import org.scalatra.scalate
import scalate.ScalateSupport
import scala.collection.mutable.HashMap
import org.json.JSONObject
import org.Pbus.model._

object Service {
  /*
  var routeMap: HashMap[String, Route] = null
  var stopMap: HashMap[Int, Stop] = null
  var routeJson: JSONArray = null
  */

  var stopList: List[Stop] = null
  var routeList: List[Route] = null
  var mapJsonObj: JSONObject = null


  def init() {
    println("Service is initializing")
    routeList = InfoLoader.loadRoutes
    stopList = InfoLoader.loadStops
    mapJsonObj = InfoLoader.getMapJsonObj(routeList, stopList)
  }

}
