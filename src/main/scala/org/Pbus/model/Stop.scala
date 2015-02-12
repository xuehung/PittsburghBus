package org.Pbus.model

import scala.collection.mutable.ListBuffer

class Stop (_id: String, _code: Int, _name: String, _lat: Double, _lon: Double) {
  val id: String = _id
  val code: Int = _code
  val name: String = _name
  val lat: Double = _lat
  val lon: Double = _lon
  var passingRoutes = new ListBuffer[String]()

  def addRoute(routeId: String) = {
    this.passingRoutes += routeId
  }
}
