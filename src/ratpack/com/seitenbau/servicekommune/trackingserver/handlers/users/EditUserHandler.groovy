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
      updatePassword(newPasswordCleartext, username)
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

    // Adding permission
    String permissionToAdd = context.request.queryParams.get("addPermission")
    if (permissionToAdd != null) {
      if (permissionToAdd.isEmpty() || permissionToAdd.isAllWhitespace()) {
        throw new HttpClientError("addPermission must not be empty", 400)
      }

      try {
        addPermission(username, permissionToAdd)
        changeLog.add("Added permission for process '$permissionToAdd'.".toString())
      } catch (PermissionAlreadyExistsException ignored) {
        changeLog.add("Permission for process '$permissionToAdd' was already given.".toString())
      }
    }

    // TODO: Removing permission

    sql.commit()
    sql.close()

    context.response.status(200)
    context.render(Jackson.json([status: "Success", changes: changeLog]))
  }

  private static void updatePassword(String newPasswordCleartext, String username) {
    assert newPasswordCleartext != null
    assert !newPasswordCleartext.isEmpty()
    assert username != null
    assert !username.isEmpty()

    Sql sql = ServerConfig.getNewSqlConnection()

    String passwordBcrypted = BCrypt.hashpw(newPasswordCleartext, BCrypt.gensalt())
    int affectedRows = sql.executeUpdate("UPDATE `users` SET `bcryptPassword` = ? WHERE username = ?", [passwordBcrypted, username])
    assert affectedRows == 1
  }

  private static void addPermission(String username, String permissionToAdd) {
    assert permissionToAdd != null
    assert !permissionToAdd.isEmpty()
    assert username != null
    assert !username.isEmpty()

    Sql sql = ServerConfig.getNewSqlConnection()

    boolean permissionAlreadyPresent
    sql.withTransaction {
      GroovyRowResult row = sql.firstRow("SELECT `username` FROM `permissions` WHERE `username` = ? AND `processId` = ?", [username, permissionToAdd])
      permissionAlreadyPresent = (row != null)

      if (!permissionAlreadyPresent) {
        sql.executeInsert("INSERT INTO `permissions` (`username`, `processId`) VALUES (?, ?)", [username, permissionToAdd])
      }
    }
    sql.commit()
    if (permissionAlreadyPresent) {
      // Sadly, this cant be done in the "withTransaction" Closure directly, as it confuses the rollback.
      throw new PermissionAlreadyExistsException()
    }
  }

  static class PermissionAlreadyExistsException extends Exception {}

}


