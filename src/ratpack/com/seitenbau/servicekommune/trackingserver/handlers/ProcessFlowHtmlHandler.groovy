package com.seitenbau.servicekommune.trackingserver.handlers

import ratpack.groovy.handling.GroovyContext

import java.nio.file.Files

class ProcessFlowHtmlHandler extends AbstractTrackingServerHandler {

  @Override
  protected void handle(GroovyContext ctx) {
    // TODO: Write OpenAPI Documentation
    // get path parameters
    String processId = ctx.allPathTokens.processId

    // get GET parameters
    Integer timeFrom
    Integer timeUntil
    try {
      timeFrom = extractIntegerFromQueryParams(ctx, "timeFrom")
      timeUntil = extractIntegerFromQueryParams(ctx, "timeUntil")
    } catch (NumberFormatException ignored) {
      return
    }

    // Check authorization
    if (!requireAuthorizationForProcess(processId, ctx)) {
      return
    }

    ctx.response.contentType("text/html")
    String html = new String(Files.readAllBytes(ctx.file("resources/process-flow-base-page.html")))
    html = html.replaceAll("PLACEHOLDER_PROCESS_ID", processId)
    ctx.render(html)
  }
}
