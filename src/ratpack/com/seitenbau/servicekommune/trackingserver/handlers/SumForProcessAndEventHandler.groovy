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
    Integer timeUntil
    try {
      timeFrom = extractIntegerFromQueryParams(ctx, "timeFrom")
      timeUntil = extractIntegerFromQueryParams(ctx, "timeUntil")
    } catch (NumberFormatException ignored) {
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
              WHERE `processId` = ?
                AND `eventId`   = ?"""
    List filterValues = [processId, eventId]
    if (timeFrom != null) {
      selectStatement += " AND `timestamp` >= FROM_UNIXTIME(?)"
      filterValues.add(timeFrom.toString())
    }
    if (timeUntil != null) {
      selectStatement += " AND `timestamp` <= FROM_UNIXTIME(?)"
      filterValues.add(timeUntil.toString())
    }
    GroovyRowResult row = sql.firstRow(selectStatement, filterValues)

    // return result to user
    ctx.response.status(200)
    ctx.render(json(row.get("amountTrackedEvent")))
  }
}
