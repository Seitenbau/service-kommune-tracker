import groovy.sql.Sql
import ratpack.handling.Context

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

// Check required environment variables
List<String> requiredEnvVariables = ["DB_URL", "DB_USERNAME", "DB_PASSWORD"]
requiredEnvVariables.each {
  if (System.getenv(it) == null) {
    throw new RuntimeException("Required environment variable '$it' is not set.")
  }
}

ratpack {
  handlers {
    get {
      render "This is the Service-Kommune Tracking API."
    }

    post("api/v1.0/processes/:processId/events/:eventId") { Context ctx ->

      // specify type for path parameters
      String processId = pathTokens.processId
      String eventId = pathTokens.eventId

      // get POST parameters
      String processInstanceId = ctx.request.queryParams.get("processInstanceId")
      String userId = ctx.request.queryParams.get("userId")

      // verify parameters
      if (processInstanceId == null) {
        ctx.response.status(400)
        render(json(["errorMsg": "Paramter 'processInstanceId' is required."]))
        return
      }
      if (!(processInstanceId.isInteger())) {
        ctx.response.status(400)
        render(json(["errorMsg": "'processInstanceId' must be an integer"]))
        return
      }
      if (processId.length() > 190) {
        ctx.response.status(400)
        render(json(["errorMessage": "'processId' must bes shorter than 190 characters"]))
      }
      if (eventId.length() > 190) {
        ctx.response.status(400)
        render(json(["errorMessage": "'eventId' must bes shorter than 190 characters"]))
      }
      if (userId != null) {
        if (userId.length() > 190) {
          ctx.response.status(400)
          render(json(["errorMessage": "'userId' must bes shorter than 190 characters"]))
        }
      }

      // Store result in database
      Sql sql = getNewSqlConnection()
      sql.execute("INSERT INTO trackedEvents (processId, eventId, processInstanceId, userId) VALUES (?, ?, ?, ?)",
              [processId, eventId, processInstanceId, userId])
      sql.commit()

      ctx.response.status(201)
      ctx.response.send() // No further content
    }
  }
}

static Sql getNewSqlConnection() {

  return Sql.newInstance(System.getenv("DB_URL"),
          System.getenv("DB_USERNAME"),
          System.getenv("DB_PASSWORD"),
          "org.mariadb.jdbc.Driver")
}
