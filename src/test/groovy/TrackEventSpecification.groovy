import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.http.TestHttpClient
import ratpack.test.ServerBackedApplicationUnderTest
import spock.lang.Specification

class TrackEventSpecification extends Specification {

  ServerBackedApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()
  @Delegate TestHttpClient client = testHttpClient(aut)

  def "Server is starting"() {
    when:
    get("")

    then:
    response.statusCode == 200
    response.body.text.contains('This is the Service-Kommune Tracking API.')
  }
}
