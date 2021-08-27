package com.seitenbau.servicekommune.trackingserver.handlers.users

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import com.seitenbau.servicekommune.trackingserver.exceptions.HttpClientError
import com.seitenbau.servicekommune.trackingserver.handlers.AbstractTrackingServerHandler
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.mindrot.jbcrypt.BCrypt
import ratpack.groovy.handling.GroovyContext
import ratpack.jackson.Jackson

class EditUserHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext context) {
    String username = context.allPathTokens."username"
    if (!username) throw new HttpClientError("Username must not be empty", 400)

    // Verify user exists
    Sql sql = ServerConfig.getNewSqlConnection()
    GroovyRowResult row = sql.firstRow("SELECT COUNT(*) AS count FROM `users` WHERE `username`= ?", [username])
    if (row."count" == 0) throw new HttpClientError("User with username '$username' was not found.", 404)


    List<String> changeLog = []

    // Updating password
    String newPasswordCleartext = context.request.queryParams.get("newPassword")
    if (newPasswordCleartext != null && newPasswordCleartext.isEmpty()) throw new HttpClientError("New password must not be empty.", 400)
    if (newPasswordCleartext) {
      String passwordBcrypted = BCrypt.hashpw(newPasswordCleartext, BCrypt.gensalt())
      int affectedRows = sql.executeUpdate("UPDATE `users` SET `bcryptPassword` = ? WHERE username = ?", [passwordBcrypted, username])
      assert affectedRows == 1
      changeLog.add("Password was updated.")
    }

    // Updating admin status
    String isAdmin = context.request.queryParams.get("isAdmin")
    if (isAdmin != null) {
      if (isAdmin == "true" || isAdmin == "false") {
        // valid values
        boolean newValue = isAdmin.toBoolean()
        int affectedRows = sql.executeUpdate("UPDATE `users` SET `isAdmin` = ? WHERE username = ?", [newValue, username])
        assert affectedRows == 1
        changeLog.add("Admin status was set to ${newValue}.".toString())
      } else {
        throw new HttpClientError("Parameter 'isAdmin' has invalid value '$isAdmin'", 400)
      }
    }

    // TODO: Removing permission

    // TODO: Adding permission

    sql.commit()
    sql.close()

    context.response.status(200)
    context.render(Jackson.json([status: "Success", changes: changeLog]))
  }
}
