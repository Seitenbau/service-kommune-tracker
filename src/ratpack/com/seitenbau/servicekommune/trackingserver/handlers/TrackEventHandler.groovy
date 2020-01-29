package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.sql.Sql
import ratpack.groovy.handling.GroovyContext

import static ratpack.jackson.Jackson.json

class TrackEventHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext ctx) {

    // get path parameters
    String processId = ctx.pathTokens.processId
    String eventId = ctx.pathTokens.eventId

    // get POST parameters
    String processInstanceId = ctx.request.queryParams.get("processInstanceId")
    String userId = ctx.request.queryParams.get("userId")

    // verify parameters
    if (processInstanceId == null) {
      ctx.response.status(400)
      ctx.render(json(["errorMsg": "Parameter 'processInstanceId' is required"]))
      return
    }
    if (!(processInstanceId.isInteger())) {
      ctx.response.status(400)
      ctx.render(json(["errorMsg": "Parameter 'processInstanceId' must be an integer"]))
      return
    }
    if (processId.length() > 190) {
      ctx.response.status(400)
      ctx.render(json(["errorMessage": "Parameter 'processId' must be shorter than 190 characters"]))
      return
    }
    if (eventId.length() > 190) {
      ctx.response.status(400)
      ctx.render(json(["errorMessage": "Parameter 'eventId' must be shorter than 190 characters"]))
      return
    }
    if (userId != null) {
      if (userId.length() > 190) {
        ctx.response.status(400)
        ctx.render(json(["errorMessage": "Parameter 'userId' must be shorter than 190 characters"]))
        return
      }
    }

    // Store result in database
    Sql sql = ServerConfig.getNewSqlConnection()
    sql.execute("INSERT INTO trackedEvents (processId, eventId, processInstanceId, userId) VALUES (?, ?, ?, ?)",
            [processId, eventId, processInstanceId, userId])
    sql.commit()

    ctx.response.status(201)
    ctx.response.send() // No further content
  }
}
