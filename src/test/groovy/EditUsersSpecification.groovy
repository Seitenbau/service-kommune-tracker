import com.seitenbau.servicekommune.trackingserver.ServerConfig
import groovy.json.JsonSlurper
import helpers.SkTrackerSpecification
import ratpack.func.Action
import ratpack.http.client.RequestSpec
import ratpack.test.http.TestHttpClient

class EditUsersSpecification extends SkTrackerSpecification {

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

  def "Update the password of a user"() {
    given:
    String newPassword = "a new password for local tests"

    when:
    params({ params ->
      params.put("newPassword", newPassword)
    })
    patch("api/v1.0/admin/users/${ServerConfig.TESTUSER_NAME}")
    def result = new JsonSlurper().parseText(response.body.text)

    then:
    response.statusCode == 200

    result != null
    result.status == "Success"
    result.changes instanceof List<String>
    (result.changes as List<String>).contains("Password was updated.")
  }

  def "Grant admin rights to user"() {
    when:
    params({ params ->
      params.put("isAdmin", true)
    })
    patch("api/v1.0/admin/users/${ServerConfig.TESTUSER_NAME}")
    def result = new JsonSlurper().parseText(response.body.text)

    then:
    response.statusCode == 200

    result != null
    result.status == "Success"
    result.changes instanceof List<String>
    (result.changes as List<String>) == ["Admin status was set to true."]
  }

  def "Remove admin rights to user"() {
    when:
    params({ params ->
      params.put("isAdmin", false)
    })
    patch("api/v1.0/admin/users/${ServerConfig.TESTADMIN_NAME}")
    def result = new JsonSlurper().parseText(response.body.text)

    then:
    response.statusCode == 200

    result != null
    result.status == "Success"
    result.changes instanceof List<String>
    (result.changes as List<String>) == ["Admin status was set to false."]
  }

}
