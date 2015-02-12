package org.Pbus

import org.Pbus.model._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import org.jsoup.Jsoup
import org.jsoup.nodes._
import org.jsoup.select._
import org.json.JSONArray
import org.json.JSONObject
import scala.io.Source._

object InfoLoader {
  def loadRoutes(): List[Route] = {
    var routeList = new ListBuffer[Route]()
    var idIndex = -1
    var shortNameIndex = -1
    var longNameIndex = -1
    var typeIndex = -1

    val lines = fromFile(getFilePath(Config.ROUTE_FILE)).getLines
    val fields = lines.next.split(",")
    fields.indices.foreach { i =>
      fields(i) match {
        case "route_id" => idIndex = i
        case "route_short_name" => shortNameIndex = i
        case "route_long_name" => longNameIndex = i
        case "route_type" => typeIndex = i
        case _ => println("field "+fields(i)+" is ignored")
      }
    }

    lines.foreach { s =>
      val values = s.split(",")
      if (values(typeIndex).toInt == 3) {
        val route = new Route(values(idIndex),
          values(shortNameIndex).replaceAll("\"", ""),
          values(longNameIndex).replaceAll("\"", ""))
        routeList += route
      }
    }
    printf("totally %d routes were parsed\n", routeList.size)

    return routeList.toList
  }

  def loadStops(): List[Stop] = {
    var stopList = new ListBuffer[Stop]()
    var idIndex = -1
    var codeIndex = -1
    var nameIndex = -1
    var latIndex = -1
    var lonIndex = -1

    //val lines = fromFile(Config.RESOURCE_DIR + "/" +Config.DATA_VERSION + "/" + Config.STOP_FILE).getLines
    val lines = fromFile(getFilePath(Config.STOP_FILE)).getLines
    val fields = lines.next.split(",")
    fields.indices.foreach { i =>
      fields(i) match {
        case "stop_id" => idIndex = i
        case "stop_code" => codeIndex = i
        case "stop_name" => nameIndex = i
        case "stop_lat" => latIndex = i
        case "stop_lon" => lonIndex = i
        case _ => println("field "+fields(i)+" is ignored")
      }
    }
    lines.foreach { s =>
      val values = s.split(",")
      val stop = new Stop(values(idIndex),
                          values(codeIndex).toInt,
                          values(nameIndex).replaceAll("\"", ""),
                          values(latIndex).toDouble,
                          values(lonIndex).toDouble)
      stopList += stop
    }
    printf("totally %d stops were parsed\n", stopList.size)
    val immutableList = stopList.toList
    addRouteToStop(immutableList, getTripRouteMapping)
    return immutableList
  }

  def getStopJsonArray(stopList: List[Stop]): JSONArray = {
    val array = new JSONArray()
    stopList.foreach { stop =>
      val stopObj = new JSONObject()
      stopObj.put("id", stop.id)
      stopObj.put("code", stop.code)
      stopObj.put("name", stop.name)
      stopObj.put("lat", stop.lat)
      stopObj.put("lon", stop.lon)
      val routes = new JSONArray()
      stop.passingRoutes.foreach { routeId =>
        routes.put(routeId)
      }
      stopObj.put("route", routes)
      array.put(stopObj)
    }
    return array
  }

  def getRouteJsonArray(routeList: List[Route]): JSONArray = {
    val array = new JSONArray()
    routeList.foreach { route =>
      val routeObj = new JSONObject()
      routeObj.put("id", route.id)
      routeObj.put("short_name", route.shortName)
      routeObj.put("long_name", route.longName)
      array.put(routeObj)
    }
    return array
  }
  def getMapJsonObj(routeList: List[Route], stopList: List[Stop]): JSONObject = {
    val obj = new JSONObject();
    obj.put("route", getRouteJsonArray(routeList))
    obj.put("stop", getStopJsonArray(stopList))
    return obj
  }

  def getTripRouteMapping(): HashMap[String, String] = {
    var mapping = new HashMap[String, String]()
    var routeIdIndex = -1
    var tripIdIndex = -1

    val lines = fromFile(getFilePath(Config.TRIP_FILE)).getLines
    val fields = lines.next.split(",")
    fields.indices.foreach { i =>
      fields(i) match {
        case "route_id" => routeIdIndex = i
        case "trip_id" => tripIdIndex = i
        case _ => println("field "+fields(i)+" is ignored")
      }
    }
    lines.foreach { s =>
      val values = s.split(",")
      mapping += (values(tripIdIndex) -> values(routeIdIndex))
    }

    return mapping
  }

  def addRouteToStop(stopList: List[Stop], tripRouteMapping: HashMap[String, String]) = {
    var stopRouteMapping = new HashMap[String, HashSet[String]]()
    var tripIdIndex = -1
    var stopIdIndex = -1

    val lines = fromFile(getFilePath(Config.STOP_TIMES_FILE)).getLines
    val fields = lines.next.split(",")
    fields.indices.foreach { i =>
      fields(i) match {
        case "trip_id" => tripIdIndex = i
        case "stop_id" => stopIdIndex = i
        case _ => println("field "+fields(i)+" is ignored")
      }
    }
    lines.foreach { s =>
      val values = s.split(",")
      val tripId = values(tripIdIndex)
      if (tripRouteMapping.contains(tripId)) {
        stopRouteMapping.getOrElseUpdate(values(stopIdIndex), new HashSet[String]).add(tripRouteMapping.getOrElse(tripId, ""))
      }
    }
    stopList.foreach {stop =>
      val stopId = stop.id
      if (stopRouteMapping.contains(stopId)) {
        val routeSet = stopRouteMapping.get(stopId).get
        routeSet.foreach {s =>
          stop.addRoute(s)
        }
      } else {
        //println("[Error] no routes passing stop id: "+ stopId)
      }
    }
  }

  def getFilePath(file: String): String = {
    return Config.RESOURCE_DIR + "/" +Config.DATA_VERSION + "/" + file
  }
}
