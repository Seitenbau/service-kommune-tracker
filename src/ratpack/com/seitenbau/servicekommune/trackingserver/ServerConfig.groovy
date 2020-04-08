package com.seitenbau.servicekommune.trackingserver

import groovy.sql.Sql
import org.mindrot.jbcrypt.BCrypt

class ServerConfig {
  public static String DB_URL
  public static String DB_USERNAME
  public static String DB_PASSWORD
  public static String DB_DRIVER

  // Default entries for tests
  public static boolean SET_UP_TEST_DATA = false
  public static final String TESTUSER_NAME = "testuser"
  public static final String TESTUSER_PASSWORD = "A password only used for running tests"
  public static final String TESTUSER_AUTHORIZED_PROCESS_ID = "testprozess"

  static Sql getNewSqlConnection() {
    return Sql.newInstance(DB_URL, DB_USERNAME, DB_PASSWORD, DB_DRIVER)
  }

  static setupTestData() {
    Sql sql = getNewSqlConnection()

    // one already tracked event
    String insertOneTrackedEventStatement = "INSERT INTO trackedEvents (processId, eventId, processInstanceId) VALUES('testprozess', 'testevent', 123);"
    sql.execute(insertOneTrackedEventStatement)

    // and two more for another event
    String insertOtherEventStatement = "INSERT INTO trackedEvents (processId, eventId, processInstanceId) VALUES('testprozess', 'anotherTestevent', 123);"
    sql.execute(insertOtherEventStatement)
    sql.execute(insertOtherEventStatement)

    // test user with access to a specific process
    String bcryptedPw = BCrypt.hashpw(TESTUSER_PASSWORD, BCrypt.gensalt())
    String insertTestUserStatement = "INSERT INTO users (username, bcryptPassword) VALUES(?, ?)"
    sql.executeInsert(insertTestUserStatement, [TESTUSER_NAME, bcryptedPw])
    String insertPermissionStatement = "INSERT INTO permissions (username, processId) VALUES(?, ?)"
    sql.executeInsert(insertPermissionStatement, [TESTUSER_NAME, TESTUSER_AUTHORIZED_PROCESS_ID])
  }
}

