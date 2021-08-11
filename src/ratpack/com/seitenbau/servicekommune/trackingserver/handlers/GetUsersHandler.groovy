package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.groovy.handling.GroovyContext
import ratpack.http.HttpMethod
import ratpack.jackson.Jackson

import java.time.LocalDateTime

class GetUsersHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext ctx) {
    ctx.render(Jackson.json(getUsersFromDb()))
  }

  private static List<User> getUsersFromDb() {
    Sql sql = ServerConfig.getNewSqlConnection()
    // Note that we do NOT select the bcrypted password here. If this function is added later, ...
    // the password should probably be stripped in the places that call this function
    String selectStatement = """SELECT `username`, `creationDate`, `isAdmin`
              FROM `users`"""
    List<GroovyRowResult> rows = sql.rows(selectStatement)

    List<User> result = rows as List<User>
    return result
  }
}

class User {
  String username
  LocalDateTime creationDate
  String bcryptPassword
  boolean isAdmin
}
