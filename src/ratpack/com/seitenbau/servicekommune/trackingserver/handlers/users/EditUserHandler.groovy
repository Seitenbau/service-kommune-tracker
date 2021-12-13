package com.seitenbau.servicekommune.trackingserver.handlers.users

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import com.seitenbau.servicekommune.trackingserver.exceptions.HttpClientError
import com.seitenbau.servicekommune.trackingserver.handlers.AbstractTrackingServerHandler
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.handling.GroovyContext
import ratpack.jackson.Jackson

class EditUserHandler extends AbstractTrackingServerHandler {
  Logger logger = LoggerFactory.getLogger(this.class)

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

    // Adding a permission
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

    // Removing a permission
    String permissionToRemove = context.request.queryParams.get("removePermission")
    if (permissionToRemove != null) {
      if (permissionToRemove.isEmpty() || permissionToRemove.isAllWhitespace()) {
        throw new HttpClientError("permissionToRemove must not be empty", 400)
      }

      boolean wasRemoved = removePermission(username, permissionToRemove)
      if (wasRemoved) {
        changeLog.add("Permission for process '$permissionToRemove' was removed.".toString())
      } else {
        changeLog.add(("Permission for process '$permissionToRemove' was not given in the first " +
                "place and therefore doesn't need to be removed.").toString())
      }
    }

    sql.commit()
    sql.close()

    context.response.status(200)
    context.render(Jackson.json([status: "Success", changes: changeLog]))
    logger.info("User '$username' changed. Changelog: ${changeLog.join("; ")}")
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

  static void addPermission(String username, String permissionToAdd) {
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

  /**
   * Removes a permission from a user
   *
   * @param username
   * @param permissionToRemove
   * @return true, if the permission was removed. false, if it was never there in the first place.
   */
  static boolean removePermission(String username, String permissionToRemove) {
    assert permissionToRemove != null
    assert !permissionToRemove.isEmpty()
    assert username != null
    assert !username.isEmpty()

    Sql sql = ServerConfig.getNewSqlConnection()

    sql.execute("DELETE FROM `permissions` WHERE `username` = ? AND `processId` = ?", [username, permissionToRemove])

    assert sql.updateCount == 0 || sql.updateCount == 1

    return sql.updateCount != 0
    // true, if the permission was removed. false, if it was never there in the first place.
  }

  static class PermissionAlreadyExistsException extends Exception {}

}


