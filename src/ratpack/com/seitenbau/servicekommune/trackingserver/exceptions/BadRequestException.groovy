package com.seitenbau.servicekommune.trackingserver.exceptions

class BadRequestException extends Exception {
  String message
  int responseStatusCode

  BadRequestException(String message, int responseStatusCode) {
    super(message)
    this.message = message
    this.responseStatusCode = responseStatusCode
  }
}
