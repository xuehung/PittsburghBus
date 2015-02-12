package org.Pbus

import org.scalatra._
import scalate.ScalateSupport

class BusServlet extends PbusStack with GZipSupport {

  get("/map") {
    Service.mapJsonObj.toString
  }

  get("/version") {
    Config.DATA_VERSION
  }

}
