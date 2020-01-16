import groovy.sql.Sql

class DatabaseHelper {
  static final Map<String, String> dbConnectionDataForTest = [
          "DB_URL"     : "jdbc:h2:~/skTracker;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
          "DB_USERNAME": "sa",
          "DB_PASSWORD": "",
          "DB_DRIVER"  : "org.h2.Driver"
  ]

  static setupTestDatabase() {
    ServerConfig.dbConnectionData = dbConnectionDataForTest
    setupTables()
    setupTestData()
  }

  private static setupTables() {
    Sql sql = ServerConfig.getNewSqlConnection()

    String createTableStatement = """
        DROP TABLE IF EXISTS `trackedEvents`;
        CREATE TABLE `trackedEvents` (
          `id` int(11) NOT NULL AUTO_INCREMENT,
          `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `processId` varchar(191) NOT NULL,
          `eventId` varchar(191) NOT NULL,
          `processInstanceId` int(11) NOT NULL,
          `userId` varchar(255) DEFAULT NULL,
          PRIMARY KEY (`id`),
          UNIQUE KEY `id` (`id`),
          KEY `trackedEvents_index_processId` (`processId`),
          KEY `trackedEvents_index_eventsAndProcess` (`processId`,`eventId`)
        ) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4;
        """
    sql.execute(createTableStatement)
  }

  private static setupTestData() {
    Sql sql = ServerConfig.getNewSqlConnection()

    String insertStatement = "INSERT INTO trackedEvents (processId, eventId, processInstanceId) VALUES('testprozess', 'testevent', 123);"
    sql.execute(insertStatement)
  }


}
