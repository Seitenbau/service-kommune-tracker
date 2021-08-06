package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.exceptions.BadRequestException
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context

import static ratpack.jackson.Jackson.json

class ExceptionHandler implements ServerErrorHandler {
  @Override
  void error(Context context, Throwable throwable) throws Exception {
    switch (throwable.class) {
      case BadRequestException:
        BadRequestException badRequestException = (BadRequestException) throwable
        context.response.status(badRequestException.responseStatusCode)
        context.render(json(["errorMsg": badRequestException.message]))
        break
      default:
        // Unexpected error.
        throwable.printStackTrace()
        context.response.status(500)
        context.render(json([
                "errorClass"  : throwable.class,
                "errorMessage": throwable.message
        ]))
        break
    }
  }
}
