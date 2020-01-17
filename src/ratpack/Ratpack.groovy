import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.mindrot.jbcrypt.BCrypt
import ratpack.handling.Context

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

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
    get("api/v1.0") { Context ctx ->
      // Parse markdown
      String documentationAsMarkdown = ctx.file("endpoints-v1.0.md").text
      Node document = Parser.builder().build().parse(documentationAsMarkdown)
      String html = HtmlRenderer.builder().build().render(document)

      ctx.response.contentType("text/html")
      render(html)
    }

    // Adding a new tracked event
    post("api/v1.0/processes/:processId/events/:eventId") { Context ctx ->

      // get path parameters
      String processId = pathTokens.processId
      String eventId = pathTokens.eventId

      // get POST parameters
      String processInstanceId = ctx.request.queryParams.get("processInstanceId")
      String userId = ctx.request.queryParams.get("userId")

      // verify parameters
      if (processInstanceId == null) {
        ctx.response.status(400)
        render(json(["errorMsg": "Parameter 'processInstanceId' is required"]))
        return
      }
      if (!(processInstanceId.isInteger())) {
        ctx.response.status(400)
        render(json(["errorMsg": "Parameter 'processInstanceId' must be an integer"]))
        return
      }
      if (processId.length() > 190) {
        ctx.response.status(400)
        render(json(["errorMessage": "Parameter 'processId' must be shorter than 190 characters"]))
        return
      }
      if (eventId.length() > 190) {
        ctx.response.status(400)
        render(json(["errorMessage": "Parameter 'eventId' must be shorter than 190 characters"]))
        return
      }
      if (userId != null) {
        if (userId.length() > 190) {
          ctx.response.status(400)
          render(json(["errorMessage": "Parameter 'userId' must be shorter than 190 characters"]))
          return
        }
      }

      // Store result in database
      Sql sql = ServerConfig.getNewSqlConnection()
      sql.execute("INSERT INTO trackedEvents (processId, eventId, processInstanceId, userId) VALUES (?, ?, ?, ?)",
              [processId, eventId, processInstanceId, userId])
      sql.commit()

      ctx.response.status(201)
      ctx.response.send() // No further content
    }

    // Getting the sum of tracked events for a given eventId
    get("api/v1.0/processes/:processId/events/:eventId/sum") { Context ctx ->
      // get path parameters
      String processId = pathTokens.processId
      String eventId = pathTokens.eventId

      // get GET parameters
      Integer timeFrom
      try {
        timeFrom = ctx.request.queryParams.timeFrom as Integer
      } catch (NumberFormatException ignored) {
        ctx.response.status(400)
        render(json(["errorMsg": "Parameter 'timeFrom' must be a valid integer smaller than ${Integer.MAX_VALUE}".toString()]))
        return
      }
      Integer timeUntil
      try {
        timeUntil = ctx.request.queryParams.timeUntil as Integer
      } catch (NumberFormatException ignored) {
        ctx.response.status(400)
        render(json(["errorMsg": "Parameter 'timeUntil' must be a valid integer smaller than ${Integer.MAX_VALUE}".toString()]))
        return
      }

      // Check authorization
      if (!requireAuthorizationForProcess(processId, ctx)) {
        return
      }

      // get count from database
      Sql sql = ServerConfig.getNewSqlConnection()
      String selectStatement = """SELECT COUNT(*) as amountTrackedEvent
              FROM trackedEvents
              WHERE processId = ?
                AND eventId   = ?"""
      List filterValues = [processId, eventId]
      if (timeFrom != null) {
        selectStatement += " AND timestamp >= FROM_UNIXTIME(?)"
        filterValues.add(timeFrom.toString())
      }
      if (timeUntil != null) {
        selectStatement += " AND timestamp <= FROM_UNIXTIME(?)"
        filterValues.add(timeUntil.toString())
      }
      GroovyRowResult row = sql.firstRow(selectStatement, filterValues)

      // return result to user
      ctx.response.status(200)
      render(json(row.get("amountTrackedEvent")))
    }

    // Only works for correctly authenticated users. Useful for testing
    get("api/v1.0/testAuth/:processId") { Context ctx ->
      String processId = pathTokens.processId

      // Check authorization
      if (!requireAuthorizationForProcess(processId, ctx)) {
        return
      }

      ctx.response.status(200)
      render(json("success"))
    }
  }
  println("Service-Kommune Tracking Server is up and running! ✅")
}

/**
 * Checks if the user authenticated in the request headers is allowed to access the process.
 *
 * Writes a error status and message into the response if something failed. Do NOT forget you will need to terminate
 * early (i.e. "return") if this method returns false.
 *
 * @param processId the processId to check permissions against
 * @param ctx The HTTP Context with request and response
 * @return true if authentication and authorization are okay!
 */
static boolean requireAuthorizationForProcess(String processId, Context ctx) {
  // Check if header was supplied
  String header = ctx.request.headers.get("Authorization")
  if (header == null) {
    ctx.response.status(401)
    ctx.render(json(["errorMsg": "Required header 'Authorization' is missing"]))
    return false
  }

  // Get username and password from header
  String username
  String password
  try {
    String encodedPart = header.split(" ")[1]
    String[] decoded = new String(encodedPart.decodeBase64()).split(":")
    username = decoded[0]
    password = decoded[1] // watch out! cleartext password
  } catch (Exception ignored) {
    ctx.response.status(400)
    ctx.render(json(["errorMsg": "Authorization header not in correct format. (Basic only)"]))
    return false
  }
  // Check if the password matches the user
  Sql sql = ServerConfig.getNewSqlConnection()
  String getPasswordStatement = "SELECT bcryptPassword FROM users WHERE username = ?"
  GroovyRowResult result = sql.firstRow(getPasswordStatement, [username])
  if (result == null) {
    ctx.response.status(401)
    ctx.render(json(["errorMsg": "Authentication failed. User not found."]))
    return false
  }
  String storedPassword = new String(result.get("bcryptPassword") as byte[])
  if (BCrypt.checkpw(password, storedPassword)) {
    // PW okay!
  } else {
    ctx.response.status(401)
    ctx.render(json(["errorMsg": "Authentication failed. Wrong password."]))
    return false
  }

  // Check permissions
  String getPermissionsStatement = "SELECT 1 FROM permissions WHERE username = ? AND processId = ?"
  int resultSize = sql.rows(getPermissionsStatement, [username, processId]).size()
  if (resultSize == 0) {
    // No results --> No fitting permission!
    ctx.response.status(403)
    ctx.render(json(["errorMsg": "Authorization failed. User '$username' is not allowed to access process '$processId'.".toString()]))
    return false
  }

  return true
}
