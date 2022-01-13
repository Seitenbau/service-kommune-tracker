package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.groovy.handling.GroovyContext

import static ratpack.jackson.Jackson.json

class ProcessFlowHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext ctx) {
    // TODO: Write OpenAPI Documentation
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
    try {
      String selectStatement = """SELECT `timestamp`, `eventId`, `processInstanceId`
                FROM trackedEvents
                WHERE `processId` = ?"""
      List filterValues = [processId]
      if (timeFrom != null) {
        selectStatement += " AND `timestamp` >= FROM_UNIXTIME(?)"
        filterValues.add(timeFrom.toString())
      }
      if (timeUntil != null) {
        selectStatement += " AND `timestamp` <= FROM_UNIXTIME(?)"
        filterValues.add(timeUntil.toString())
      }
      selectStatement += "ORDER BY `processInstanceId` ASC, `timestamp` ASC"
      List<GroovyRowResult> rows = sql.rows(selectStatement, filterValues)

      // extract flows
      Map<String, List<String>> flows = [:]
      rows.each { row ->
        String processInstanceId = row.get("processInstanceId") as String
        String eventId = row.get("eventId") as String

        List<String> flow = flows.get(processInstanceId)
        if (flow == null) {
          // Initialize flow, if hasn't been initialized yet
          flow = []
          flows.put(processInstanceId, flow)
        }
        flow.add(eventId)
      }

      // return result to user
      ctx.response.status(200)
      ctx.render(json(flows))
    } finally {
      sql.close()
    }
  }
}
