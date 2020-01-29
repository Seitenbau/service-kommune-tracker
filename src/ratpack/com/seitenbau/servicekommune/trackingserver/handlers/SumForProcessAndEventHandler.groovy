package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.groovy.handling.GroovyContext

import static ratpack.jackson.Jackson.json

class SumForProcessAndEventHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext ctx) {
    // get path parameters
    String processId = ctx.allPathTokens.processId
    String eventId = ctx.allPathTokens.eventId

    // get GET parameters
    Integer timeFrom
    try {
      timeFrom = ctx.request.queryParams.timeFrom as Integer
    } catch (NumberFormatException ignored) {
      ctx.response.status(400)
      ctx.render(json(["errorMsg": "Parameter 'timeFrom' must be a valid integer smaller than ${Integer.MAX_VALUE}".toString()]))
      return
    }
    Integer timeUntil
    try {
      timeUntil = ctx.request.queryParams.timeUntil as Integer
    } catch (NumberFormatException ignored) {
      ctx.response.status(400)
      ctx.render(json(["errorMsg": "Parameter 'timeUntil' must be a valid integer smaller than ${Integer.MAX_VALUE}".toString()]))
      return
    }

    // Check authorization
    if (!requireAuthorizationForProcess(processId, ctx)) {
      return
    }

    // get count from database
    Sql sql = ServerConfig.getNewSqlConnection()
    String selectStatement = """SELECT COUNT(*) as amountTrackedEvent
              FROM trackedEvents
              WHERE processId = ?
                AND eventId   = ?"""
    List filterValues = [processId, eventId]
    if (timeFrom != null) {
      selectStatement += " AND timestamp >= FROM_UNIXTIME(?)"
      filterValues.add(timeFrom.toString())
    }
    if (timeUntil != null) {
      selectStatement += " AND timestamp <= FROM_UNIXTIME(?)"
      filterValues.add(timeUntil.toString())
    }
    GroovyRowResult row = sql.firstRow(selectStatement, filterValues)

    // return result to user
    ctx.response.status(200)
    ctx.render(json(row.get("amountTrackedEvent")))
  }
}
