import groovy.sql.Sql

class ServerConfig {
  static Map<String, String> dbConnectionData = [:]

  static Sql getNewSqlConnection() {
    return Sql.newInstance(dbConnectionData.DB_URL,
            dbConnectionData.DB_USERNAME,
            dbConnectionData.DB_PASSWORD,
            dbConnectionData.DB_DRIVER)
  }
}

