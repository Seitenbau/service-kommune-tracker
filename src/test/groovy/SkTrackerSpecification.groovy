import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.sql.Sql
import org.mindrot.jbcrypt.BCrypt
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.ServerBackedApplicationUnderTest
import spock.lang.Specification

class SkTrackerSpecification extends Specification {
  static protected final String TESTUSER_NAME = "testuser"
  static protected final String TESTUSER_PASSWORD = "A password only used for running tests"
  static protected final String TESTUSER_AUTHORIZED_PROCESS_ID = "testprozess"

  ServerBackedApplicationUnderTest aut

  def setup() {
    ServerConfig.DB_URL = "jdbc:h2:mem:skTracker;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
    ServerConfig.DB_USERNAME = "sa"
    ServerConfig.DB_PASSWORD = ""
    ServerConfig.DB_DRIVER = "org.h2.Driver"
    aut = new GroovyRatpackMainApplicationUnderTest()

    //setupTestData()
    // TODO: Setting up test data is still missing
  }

  private static setupTestData() {
    Sql sql = ServerConfig.getNewSqlConnection()

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
