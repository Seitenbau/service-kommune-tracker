package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.groovy.handling.GroovyContext

import static ratpack.jackson.Jackson.json

class ProcessFlowHandler extends AbstractTrackingServerHandler {
  private final String NO_FURTHER_EVENT = "SPECIAL_IDENTIFIER_FOR_NO_FURTHER_EVENTS_IN_THIS_FLOW"

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

      // extract flows and events
      Map<String, List<String>> flows = [:]
      Set<String> uniqueEvents = []
      rows.each { row ->
        String processInstanceId = row.get("processInstanceId") as String
        String eventId = row.get("eventId") as String

        assert eventId != NO_FURTHER_EVENT: "A super unlikely case appeared where a event was called '$NO_FURTHER_EVENT', or the developer of this function made a false assumption."

        // extract flow
        List<String> flow = flows.get(processInstanceId)
        if (flow == null) {
          // Initialize flow, if hasn't been initialized yet
          flow = []
          flows.put(processInstanceId, flow)
        }
        flow.add(eventId)

        // extract event. This is a set, therefore no duplicate entries
        uniqueEvents.add(eventId)
      }

      // count number of following events, for each event
      Map<String, Map<String, Integer>> eventsAndFollowingEvents = [:]
      uniqueEvents.each { eventName ->
        eventsAndFollowingEvents.put(eventName, [:])
      }
      flows.values().each { flow ->
        flow.eachWithIndex { currentEventId, index ->
          String followingEvent
          if (index == flow.size() - 1) {
            // this is the last element
            followingEvent = NO_FURTHER_EVENT
          } else {
            followingEvent = flow.get(index + 1)
          }

          int currentCounter = eventsAndFollowingEvents.get(currentEventId).get(followingEvent) ?: 0
          eventsAndFollowingEvents.get(currentEventId).put(followingEvent, currentCounter + 1)
        }
      }

      // return result to user
      ctx.response.status(200)
      ctx.render(json(eventsAndFollowingEvents))
    } finally {
      sql.close()
    }
  }
}
