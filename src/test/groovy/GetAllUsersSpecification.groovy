import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.json.JsonSlurper
import helpers.SkTrackerSpecification
import ratpack.func.Action
import ratpack.http.client.RequestSpec
import ratpack.test.http.TestHttpClient

class GetAllUsersSpecification extends SkTrackerSpecification {

  @Delegate
  TestHttpClient client

  def setup() {
    client = testHttpClient(aut, new Action<RequestSpec>() {
      @Override
      void execute(RequestSpec requestSpec) throws Exception {
        requestSpec.basicAuth(ServerConfig.TESTADMIN_NAME, ServerConfig.TESTADMIN_PASSWORD)
      }
    })
  }

  def "Get all users"() {
    when:
    get("api/v1.0/admin/users")
    Set<Object> result = new JsonSlurper().parseText(response.body.text) as Set<Object>

    then:
    response.statusCode == 200

    long now = System.currentTimeMillis()
    long tenSecondsAgo = System.currentTimeMillis() - (10 * 1000)

    def admin = result.find { it.username == ServerConfig.TESTADMIN_NAME }
    admin != null
    admin.isAdmin == true
    admin.creationDate <= now
    admin.creationDate > tenSecondsAgo

    def user = result.find { it.username == ServerConfig.TESTUSER_NAME }
    user != null
    user.isAdmin == false
    user.creationDate <= now
    user.creationDate > tenSecondsAgo
  }

}
