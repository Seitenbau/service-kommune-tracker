package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.ocpsoft.prettytime.PrettyTime
import ratpack.groovy.handling.GroovyContext

import java.sql.Timestamp

import static ratpack.jackson.Jackson.json

class AllProcessesHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext ctx) {
    List<ReturnDto> result = []

    // get data from database
    Sql sql = ServerConfig.getNewSqlConnection()
    String selectStatement = """\
      SELECT e.`processId`, COUNT(*) as numberOfTrackedEvents, MAX(e.`timestamp`) as lastTrackedEvent, MIN(e.`timestamp`) as firstTrackedEvent 
        FROM `trackedEvents` as e 
        GROUP BY e.processId;"""
    List<GroovyRowResult> rowsOfEvents = sql.rows(selectStatement)
    rowsOfEvents.each { dbEvent ->
      ReturnDto currentDto = new ReturnDto()

      currentDto.processId = dbEvent."processId"
      currentDto.numberOfTrackedEvents = dbEvent."numberOfTrackedEvents"
      currentDto.firstTrackedEventRelative = new PrettyTime().format(dbEvent."firstTrackedEvent" as Timestamp)
      currentDto.lastTrackedEventRelative = new PrettyTime().format(dbEvent."lastTrackedEvent" as Timestamp)

      // Doing sql queries in a loop is probably not the most efficient way, but neither is
      // optimizing it prematurely and spending developer time.
      String selectPermissionsStatement = """\
       SELECT `username` 
         FROM `permissions` p
         WHERE `processId` = ?;"""
      List<GroovyRowResult> rowsOfPermissions = sql.rows(selectPermissionsStatement, [currentDto.processId])
      currentDto.usersWithAccess = rowsOfPermissions.collect { it."username" }

      result.add(currentDto)
    }

    // Sort by name
    result = result.sort { it.processId }

    // Render results
    ctx.render(json(result))
    ctx.response.status(200)

  }

  class ReturnDto {
    String processId
    List<String> usersWithAccess
    int numberOfTrackedEvents
    String firstTrackedEventRelative
    String lastTrackedEventRelative
  }
}
