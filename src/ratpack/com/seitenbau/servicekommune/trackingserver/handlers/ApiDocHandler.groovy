package com.seitenbau.servicekommune.trackingserver.handlers

import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import ratpack.groovy.handling.GroovyContext

class ApiDocHandler extends AbstractTrackingServerHandler {
  @Override
  protected void handle(GroovyContext ctx) {
    // Parse markdown
    String documentationAsMarkdown = ctx.file("endpoints-v1.0.md").text
    Node document = Parser.builder().build().parse(documentationAsMarkdown)
    String html = HtmlRenderer.builder().build().render(document)

    ctx.response.contentType("text/html")
    ctx.render(html)
  }
}
