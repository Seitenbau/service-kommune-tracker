import groovy.sql.Sql
import org.mindrot.jbcrypt.BCrypt

class DatabaseHelper {
  static final Map<String, String> dbConnectionDataForTest = [
          "DB_URL"     : "jdbc:h2:mem:skTracker;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
          "DB_USERNAME": "sa",
          "DB_PASSWORD": "",
          "DB_DRIVER"  : "org.h2.Driver"
  ]

  static final String TESTUSER_NAME = "testuser"
  static final String TESTUSER_PASSWORD = "A password only used for running tests"
  static final String TESTUSER_AUTHORIZED_PROCESS_ID = "testprozess"

  static setupTestDatabase() {
    ServerConfig.dbConnectionData = dbConnectionDataForTest
    setupTables()
    setupTestData()
  }

  private static setupTables() {
    Sql sql = ServerConfig.getNewSqlConnection()

    String createUsersStatement = """
        DROP TABLE IF EXISTS `permissions`, `users`;
        
        CREATE TABLE `users` (
          `username` VARCHAR(191) NOT NULL,
          `creationDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `bcryptPassword` BINARY(60) NOT NULL,
          PRIMARY KEY (`username`),
          UNIQUE KEY `username` (`username`)
        );
        
        CREATE TABLE `permissions` (
          `username` VARCHAR(191) NOT NULL,
          `processId` varchar(191) NOT NULL,
          PRIMARY KEY (`username`, `processId`),
          CONSTRAINT `fk_username` FOREIGN KEY (`username`) REFERENCES `users`(`username`)
        );
        """
    sql.execute(createUsersStatement)

    String createTrackedEventsStatement = """
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
    sql.execute(createTrackedEventsStatement)
  }

  private static setupTestData() {
    Sql sql = ServerConfig.getNewSqlConnection()

    // one already tracked event
    String insertOneTrackedEventStatement = "INSERT INTO trackedEvents (processId, eventId, processInstanceId) VALUES('testprozess', 'testevent', 123);"
    sql.execute(insertOneTrackedEventStatement)

    // test user with access to a specific process
    String bcryptedPw = BCrypt.hashpw(TESTUSER_PASSWORD, BCrypt.gensalt())
    String insertTestUserStatement = "INSERT INTO users (username, bcryptPassword) VALUES(?, ?)"
    sql.executeInsert(insertTestUserStatement, [TESTUSER_NAME, bcryptedPw])
    String insertPermissionStatement = "INSERT INTO permissions (username, processId) VALUES(?, ?)"
    sql.executeInsert(insertPermissionStatement, [TESTUSER_NAME, TESTUSER_AUTHORIZED_PROCESS_ID])
  }
}
