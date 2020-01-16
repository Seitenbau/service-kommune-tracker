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
    int sum = Integer.parseInt(response.body.text)

    then:
    response.statusCode == 200
    sum >= 0 // the test database should always contain entries for "testprozess" and "testevent"
  }

  def "Adding events increases the sum of events"() {
    given:
    String processId = "testprozess"
    String eventId = "testevent"
    int processInstanceId = 123456

    when:
    // Get current state
    get("api/v1.0/processes/$processId/events/$eventId/sum")
    int oldSum = Integer.parseInt(response.body.text)

    // Track new event
    params({ params ->
      params.put("processInstanceId", processInstanceId)
    })
    post("/api/v1.0/processes/$processId/events/$eventId")

    // Get new state
    get("api/v1.0/processes/$processId/events/$eventId/sum")
    int newSum = Integer.parseInt(response.body.text)

    then:
    newSum - oldSum == 1
  }

  def "Get the sum of a given Event with timeFrom"() {
    given:
    String processId = "testprozess"
    String eventId = "testevent"
    String timeFrom = "2145916800" // 2038-01-01 00:00:00+00:00 i.e. a day far in the future

    when:
    params({params->
        params.put("timeFrom", timeFrom)
    })
    get("api/v1.0/processes/$processId/events/$eventId/sum")

    then:
    int sum = Integer.parseInt(response.body.text)
    sum == 0
  }

  // TODO: Test for timeUntil
  // TODO: Test for timeFrom & timeUntil
  // TODO: Test for timeUntil is a String (and not a number)
  // TODO: Test for timeUntil is a ridiculously large number (larger than the timestamp supports it)

  // TODO: Check for invalid authorization (in another test specification)
  // TODO: Check for missing authorization (in another test specification)


}
