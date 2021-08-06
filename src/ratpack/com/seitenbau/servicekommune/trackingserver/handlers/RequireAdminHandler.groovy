package com.seitenbau.servicekommune.trackingserver.handlers

import ratpack.groovy.handling.GroovyContext

class RequireAdminHandler extends AbstractTrackingServerHandler {

  @Override
  protected void handle(GroovyContext context) {
    // TODO: Check if supplied authorization is valid
  }
}
