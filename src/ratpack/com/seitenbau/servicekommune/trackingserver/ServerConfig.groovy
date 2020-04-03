package com.seitenbau.servicekommune.trackingserver

import groovy.sql.Sql

class ServerConfig {
  public static String DB_URL
  public static String DB_USERNAME
  public static String DB_PASSWORD
  public static String DB_DRIVER

  static Sql getNewSqlConnection() {
    return Sql.newInstance(DB_URL, DB_USERNAME, DB_PASSWORD, DB_DRIVER)
  }
}

