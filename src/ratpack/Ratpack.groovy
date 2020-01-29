import com.seitenbau.servicekommune.trackingserver.ServerConfig
import com.seitenbau.servicekommune.trackingserver.handlers.ApiDocHandler
import com.seitenbau.servicekommune.trackingserver.handlers.SumForProcessAndEventHandler
import com.seitenbau.servicekommune.trackingserver.handlers.TestAuthHandler
import com.seitenbau.servicekommune.trackingserver.handlers.TrackEventHandler
import ratpack.handling.Context

import static ratpack.groovy.Groovy.ratpack

// Check required database config variables
List<String> requiredDbConfigValues = ["DB_URL", "DB_USERNAME", "DB_PASSWORD", "DB_DRIVER"]
requiredDbConfigValues.each {
  if (ServerConfig.dbConnectionData.get(it) == null) {
    // variable is not configured. Maybe we have config data in environment variables?

    String valueFromEnvVariable = System.getenv(it)
    if (valueFromEnvVariable == null) {
      throw new RuntimeException("Required environment variable '$it' is not set.")
    } else {
      ServerConfig.dbConnectionData.put(it, valueFromEnvVariable)
    }
  }
}

ratpack {
  handlers {

    // Start page --> Show some Info
    get { Context ctx ->
      ctx.response.contentType("text/html")
      render("This is the Service-Kommune Tracking API.<br><a href=\"api/v1.0\">Documentation</a>")
    }

    // API Doc page
    get("api/v1.0", new ApiDocHandler())

    // Adding a new tracked event
    post("api/v1.0/processes/:processId/events/:eventId", new TrackEventHandler())

    // Getting the sum of tracked events for a given eventId
    get("api/v1.0/processes/:processId/events/:eventId/sum", new SumForProcessAndEventHandler())

    // Only works for correctly authenticated users. Useful for testing
    get("api/v1.0/testAuth/:processId", new TestAuthHandler())
  }
}

println("Service-Kommune Tracking Server is up and running! ✅")
