package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.groovy.handling.GroovyContext

import java.sql.Timestamp

import static ratpack.jackson.Jackson.json

class AllDetailsHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext ctx) {
    // get path parameters
    String processId = ctx.allPathTokens.processId

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

    // get data from database
    Sql sql = ServerConfig.getNewSqlConnection()
    String selectStatement = """SELECT `timestamp`, eventId, processInstanceId, userId
              FROM trackedEvents
              WHERE processId = ?"""
    List filterValues = [processId]
    if (timeFrom != null) {
      selectStatement += " AND timestamp >= FROM_UNIXTIME(?)"
      filterValues.add(timeFrom.toString())
    }
    if (timeUntil != null) {
      selectStatement += " AND timestamp <= FROM_UNIXTIME(?)"
      filterValues.add(timeUntil.toString())
    }
    List<GroovyRowResult> rows = sql.rows(selectStatement, filterValues)


    // prepare result for user
    List<OutputTrackedEvent> output = []
    rows.each { row ->
      OutputTrackedEvent ote = new OutputTrackedEvent()
      ote.timestamp = (int) (((row.get("timestamp") as Timestamp).getTime()) / 1000)
      ote.eventId = row.get("eventId")
      ote.processInstanceId = row.get("processInstanceId") as int
      ote.userId = row.get("userId")
      output.add(ote)
    }

    // return result to user
    ctx.response.status(200)
    ctx.render(json(output))
  }

  // Used for generating output
  private class OutputTrackedEvent {
    int timestamp
    String eventId
    int processInstanceId
    String userId
  }
}
