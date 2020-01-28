import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.http.TestHttpClient
import ratpack.test.ServerBackedApplicationUnderTest
import spock.lang.Specification

class StartupSpecification extends SkTrackerSpecification {

  @Delegate
  TestHttpClient client

  def setup() {
    client = testHttpClient(aut)
  }

  def "Server is starting"() {
    when:
    get("")

    then:
    response.statusCode == 200
    response.body.text.contains('This is the Service-Kommune Tracking API.')
  }
}
