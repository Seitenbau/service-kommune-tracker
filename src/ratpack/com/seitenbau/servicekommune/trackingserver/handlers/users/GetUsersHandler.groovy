package com.seitenbau.servicekommune.trackingserver.handlers.users

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import com.seitenbau.servicekommune.trackingserver.handlers.AbstractTrackingServerHandler
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.ocpsoft.prettytime.PrettyTime
import ratpack.groovy.handling.GroovyContext
import ratpack.jackson.Jackson
import java.sql.Timestamp


class GetUsersHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext ctx) {
    ctx.render(Jackson.json(getUsersFromDb()))
  }

  private static List getUsersFromDb() {
    Sql sql = ServerConfig.getNewSqlConnection()
    // Note that we do NOT select the bcrypted password here. If this function is added later, ...
    // the password should probably be stripped in the places that call this function
    String selectStatement = """SELECT u.`username`, u.`creationDate`, u.`isAdmin`, p.`processId`
              FROM `users` u
              LEFT OUTER JOIN `permissions` p ON p.`username` = u.`username`"""
    List<GroovyRowResult> rows = sql.rows(selectStatement)

    List result = []
    rows.unique { it.username }.each {
      // TODO: Also return current permissions
      PrettyTime prettyTime = new PrettyTime()
      def user = [
              "username"            : it.username,
              "creationDate"        : it.creationDate,
              "creationDateHuman"   : (it.creationDate as Timestamp).toString(),
              "creationDateRelative": prettyTime.format(it.creationDate as Timestamp),
              "isAdmin"             : it.isAdmin
      ]
      result.add(user)
    }
    return result
  }
}


