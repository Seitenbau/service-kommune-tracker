import com.seitenbau.servicekommune.trackingserver.ServerConfig
import com.seitenbau.servicekommune.trackingserver.handlers.AllDetailsHandler
import com.seitenbau.servicekommune.trackingserver.handlers.ExceptionHandler
import com.seitenbau.servicekommune.trackingserver.handlers.RequireAdminHandler
import com.seitenbau.servicekommune.trackingserver.handlers.SumForProcessAndEventHandler
import com.seitenbau.servicekommune.trackingserver.handlers.SumsForProcessHandler
import com.seitenbau.servicekommune.trackingserver.handlers.TestAuthHandler
import com.seitenbau.servicekommune.trackingserver.handlers.TrackEventHandler
import com.seitenbau.servicekommune.trackingserver.handlers.users.AddUserHandler
import com.seitenbau.servicekommune.trackingserver.handlers.users.GetUserHandler
import com.seitenbau.servicekommune.trackingserver.handlers.users.GetUsersHandler
import org.flywaydb.core.Flyway
import ratpack.handling.Context
import ratpack.http.MutableHeaders

import java.lang.reflect.Field
import java.nio.file.Files

import static ratpack.groovy.Groovy.byMethod
import static ratpack.groovy.Groovy.ratpack

// Check required database config variables
List<String> requiredDbConfigValues = ["DB_URL", "DB_USERNAME", "DB_PASSWORD", "DB_DRIVER"]
requiredDbConfigValues.each {
  Field field = ServerConfig.declaredFields.find({ field -> field.name == it })
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

// Setup database via Flyway
flyway = Flyway.configure().dataSource(ServerConfig.DB_URL, ServerConfig.DB_USERNAME, ServerConfig.DB_PASSWORD).load()

// Clean old values
if (ServerConfig.SET_UP_TEST_DATA) {
  flyway.clean()
}

// Setup database tables
flyway.migrate()

// Create default entries in database
if (ServerConfig.SET_UP_TEST_DATA) {
  ServerConfig.setupTestData()
}

ratpack {
  bindings {
    bind(ExceptionHandler) // custom exception handler
  }

  handlers {
    all {
      // Set CORS headers for all requests
      MutableHeaders headers = response.headers
      headers.set("Access-Control-Allow-Origin", "*")
      headers.set("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE")
      headers.set("Access-Control-Allow-Headers", "Authorization")
      next()
    }

    get() { Context ctx ->
      ctx.response.contentType("text/html")
      render("This is the Serviceportal Tracking Server.<br><a href=\"api/v1.0\">API Documentation</a>")
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
            get(new SumForProcessAndEventHandler())
            // Getting the sum of tracked events for a given eventId
          }
        }

        prefix("sums") {
          get(new SumsForProcessHandler())
          // Getting the sums of all tracked events for a given processId
        }
      }

      prefix("testAuth/:processId") {
        get("", new TestAuthHandler())
        // Only works for correctly authenticated users. Useful for testing
      }

      prefix("admin") {
        all(new RequireAdminHandler())

        prefix("users") {
          path {
            byMethod() {
              get(new GetUsersHandler())
              post(new AddUserHandler())
            }
          }

          prefix(":username") {
            path {
              byMethod {
                get(new GetUserHandler())
                // TODO: Add methods to edit users and their permissions
              }
            }
          }
        }
      }
    }
  }
}

println("Serviceportal Tracking Server is up and running! ✅")
