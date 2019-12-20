import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.http.TestHttpClient
import ratpack.test.ServerBackedApplicationUnderTest
import spock.lang.Specification

class GetSumOfGivenEventSpecification extends Specification {

  ServerBackedApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()
  @Delegate
  TestHttpClient client = testHttpClient(aut)


  def "Get the sum of a given Event"() {
    given:
    String processId = "testprozess"
    String eventId = "testevent"

    when:

    get("api/v1.0/processes/$processId/events/$eventId/sum")

    then:
    response.statusCode == 200
    // TODO: Tests should use a database for test data only. So we can verify the response.body here as well.
  }

  def "Get the sum of a given Event with timeFrom"() {
    given:
    String processId = "testprozess"
    String eventId = "testevent"
    String timeFrom = "" // TODO: Use an actual value

    when:
    params({params->
        params.put("timeFrom", timeFrom)
    })
    get("api/v1.0/processes/$processId/events/$eventId/sum")

    then:
    response.statusCode == 200
    // TODO: Tests should use a database for test data only. So we can verify the response.body here as well.
  }

  // TODO: Test for timeUntil
  // TODO: Test for timeFrom & timeUntil
  // TODO: Test for timeUntil is a String (and not a number)
  // TODO: Test for timeUntil is a ridiculously large number (larger than the timestamp supports it)

  // TODO: Check for invalid authorization (in another test specification)
  // TODO: Check for missing authorization (in another test specification)


}
