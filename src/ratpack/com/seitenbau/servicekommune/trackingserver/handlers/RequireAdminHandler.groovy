package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import com.seitenbau.servicekommune.trackingserver.exceptions.HttpClientError
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.groovy.handling.GroovyContext

class RequireAdminHandler extends AbstractTrackingServerHandler {

  @Override
  protected void handle(GroovyContext context) {
    def (String username, String password) = getUsernameAndPasswordFromHeaders(context)
    verifyUser(context, username, password)

    // Check if user is admin
    Sql sql = ServerConfig.getNewSqlConnection()
    String getPermissionsStatement = "SELECT `isAdmin` FROM users WHERE username = ?"
    List<GroovyRowResult> rows = sql.rows(getPermissionsStatement, [username])
    assert rows.size() == 1
    if (rows[0]."isAdmin" == false) {
      throw new HttpClientError("Authorization failed. User '$username' is not an admin.", 403)
    } else {
      // User is admin, so we delegate to the next handler.
      context.next()
    }

  }
}
