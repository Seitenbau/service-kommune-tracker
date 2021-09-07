package com.seitenbau.servicekommune.trackingserver.handlers

import com.seitenbau.servicekommune.trackingserver.exceptions.HttpClientError
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context

import static ratpack.jackson.Jackson.json

class ExceptionHandler implements ServerErrorHandler {
  @Override
  void error(Context context, Throwable throwable) throws Exception {
    switch (throwable.class) {
      case HttpClientError:
        HttpClientError badRequestException = (HttpClientError) throwable
        context.response.status(badRequestException.responseStatusCode)
        if (badRequestException.responseStatusCode == 401) {
          context.response.headers.add("WWW-Authenticate", "Basic realm=\"Authentication required.\", charset=\"UTF-8\"")
        }
        context.render(json([
                "errorType"   : "Client error",
                "errorMessage": badRequestException.message
        ]))
        break
      default:
        // Unexpected error.
        throwable.printStackTrace()
        context.response.status(500)
        context.render(json([
                "errorType"   : "Server error",
                "errorClass"  : throwable.class,
                "errorMessage": throwable.message
        ]))
        break
    }
  }
}
