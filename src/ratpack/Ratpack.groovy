import com.seitenbau.servicekommune.trackingserver.ServerConfig
import com.seitenbau.servicekommune.trackingserver.handlers.AllDetailsHandler
import com.seitenbau.servicekommune.trackingserver.handlers.SumForProcessAndEventHandler
import com.seitenbau.servicekommune.trackingserver.handlers.SumsForProcessHandler
import com.seitenbau.servicekommune.trackingserver.handlers.TestAuthHandler
import com.seitenbau.servicekommune.trackingserver.handlers.TrackEventHandler
import org.flywaydb.core.Flyway
import ratpack.handling.Context

import java.lang.reflect.Field
import java.nio.file.Files

import static ratpack.groovy.Groovy.ratpack

// Check required database config variables
List<String> requiredDbConfigValues = ["DB_URL", "DB_USERNAME", "DB_PASSWORD", "DB_DRIVER"]
requiredDbConfigValues.each {
  Field field = ServerConfig.declaredFields.find({ field -> field.name == "DB_URL" })
  if (field.get(null) == null) {
    // variable is not configured. Maybe we have config data in environment variables?

    String valueFromEnvVariable = System.getenv(it)
    if (valueFromEnvVariable == null) {
      throw new RuntimeException("Required environment variable '$it' is not set.")
    } else {
      field.set(null, valueFromEnvVariable)
    }
  }
}

// Setup database via Firefly
// flyway = Flyway.configure().dataSource(ServerConfig.dbConnectionData.DB_URL, databaseUsername, databasePassword).load()
// TODO.

ratpack {
  handlers {
    get() { Context ctx ->
      ctx.response.contentType("text/html")
      render("This is the Service-Kommune Tracking API.<br><a href=\"api/v1.0\">Documentation</a>")
    } // Start page

    prefix("api/v1.0") {
      get() { Context ctx ->
        ctx.response.contentType("text/html")
        ctx.render(new String(Files.readAllBytes(ctx.file("resources/api-documentation.html"))))
      }

      prefix("openapi.yaml") {
        get() { Context ctx ->
          ctx.response.contentType("text/yaml")
          ctx.render(new String(Files.readAllBytes(ctx.file("resources/openapi.yaml"))))
        }
      }

      prefix("processes/:processId") {
        get(new AllDetailsHandler()) // Getting all details of tracked events

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

      prefix("testAuth/:processId") {
        get("", new TestAuthHandler()) // Only works for correctly authenticated users. Useful for testing
      }
    }
  }
}

println("Service-Kommune Tracking Server is up and running! ✅")
