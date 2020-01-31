import com.seitenbau.servicekommune.trackingserver.ServerConfig
import com.seitenbau.servicekommune.trackingserver.handlers.SumForProcessAndEventHandler
import com.seitenbau.servicekommune.trackingserver.handlers.SumsForProcessHandler
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
    get() { Context ctx ->
      ctx.response.contentType("text/html")
      render("This is the Service-Kommune Tracking API.<br><a href=\"api/v1.0\">Documentation</a>")
    } // Start page

    prefix("api/v1.0") {
      get() { Context ctx ->
        ctx.response.contentType("text/html")
        ctx.render(ctx.file("resources/api-documentation.html").text)
      }

      prefix("processes/:processId") {

        prefix("events/:eventId") {
          post(new TrackEventHandler()) // Adding a new tracked event

          prefix("sum") {
            get(new SumForProcessAndEventHandler()) // Getting the sum of tracked events for a given eventId
          }
        }

        prefix("sums") {
          get(new SumsForProcessHandler()) // Getting the sums of all tracked events for a given processId
        }
      }

      prefix("openapi.yaml") {
        get() { Context ctx ->
          ctx.response.contentType("text/yaml")
          ctx.render(ctx.file("resources/openapi.yaml").text)
        }
      }

      prefix("testAuth/:processId") {
        get("", new TestAuthHandler()) // Only works for correctly authenticated users. Useful for testing
      }
    }
  }
}

println("Service-Kommune Tracking Server is up and running! ✅")
