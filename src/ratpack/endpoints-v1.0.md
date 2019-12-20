# General

This document describes the REST API for the Service-Kommune event tracker. After this general section, you will find a chapter for each endpoint.

## Authentication

Some endpoints might require authorization and will return a HTTP 401 "Unauthorized" if no authentication was provided or used the wrong credentials.

To use those endpoints, you need to supply a `Authorization` header with the `type` "Basic" and the credentials `base64Encode(username + ":" + password)`.

Example: For the user "aladdin" with the password "opensesame", you should submit the header: `Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l`

Not determined yet: Who is responsible for the permissions? For now: contact dennis.weber@seitenbau.com

# POST /processes/{processId}/events/{eventId}

Track an event.

Does NOT require a authentication-token.

## parameters
* `processId` (in URL)
  **required**
  Unique identifier for a process.
  Example:`karlsruhe-mietpresrechner`

* `eventId`
  **required** (in URL)
  Unique identifier for a step in a model.
  Example:`before-filling-form`

* `processInstanceId`
  **required**
  Unique identifier for a concrete process instance.
    Can be read from the process instance variable `processInstanceId`
    WARNING: People with access to the system database might be able to get
    personal identifieable information from that. You need to ensure that
    this doesn't happen, or include that fact in your privacy statement.
  Example: `15460988`

*  `userId`
  **optional**
  Unique identifier for a user, to allow tracking accross different process instances.
    Can be read from the process instance variable `startedBy`
    WARNING: People with access to the admin center will be able to get
    the e-mail address for a given userId. As you most likely cannot prevent that you should consider not providing that parameter for data privacy reasons.
  Example: `userId:123` or `sessionId:456`

## response

HTTP 201 ("Created) on sucessful write, no further content

HTTP 400 when parameters are missing or invalid, returns a json document with a error message
```
{
  "errorMsg": "{Can be anything}"
}
```

# GET /processes/{processId}/events/{eventId}/sum

Get the sum of all events for a given process and event ID.

## parameters

* `processId` (in URL)
  **required**
  The unique identifier for a process. (As set by the `processId` parameter at the `/track` endpoint.)

* `eventId` (in URL)
  **required**
  The unique identifier for a event. (As set by the `eventId` parameter at the `/track` endpoint.)

* `timeFrom`
  **optional**
  A UNIX timestamp marking the point in time when events are included in the result.
  If missing, all events are included (as long as they are not filtered by another parameter)

* `timeUntil`
  **optional**
  A UNIX timestamp marking the point in time when events are no longer included in the result.
  If missing, all events are included (as long as they are not filtered by another parameter)

## response

HTTP 200 on a successful call, returns a unstructured numbers which describes how often this event was tracked.
Example:
```
5
```

HTTP 401 when no `Authorization` header was provided, or the username-password combination didn't match.

HTTP 403 when the supplied user (in the `Authorization` header) is not allowed to access this `processId`.

HTTP 404 when the supplied eventId for the supplied processId has no events.

# GET /processes/{processId}/sum

Get the sum of all events for a given process.

## parameters
* `processId` (in URL)
  **required**
  The unique identifier for a process. (As set by the `processId` parameter at the `/track` endpoint.)

* `timeFrom`
  **optional**
  A UNIX timestamp marking the point in time when events are included in the result.
  If missing, all events are included (as long as they are not filtered by another parameter)

* `timeUntil`
  **optional**
  A UNIX timestamp marking the point in time when events are no longer included in the result.
  If missing, all events are included (as long as they are not filtered by another parameter)

## response

HTTP 200 on a successful call, returns a json document with all tracked event IDs and how often that event was tracked. Example:
```
{
  "before-filling-form": 521,
  "after-filling-form": 140
}
```

HTTP 401 when no `Authorization` header was provided, or the username-password combination didn't match.

HTTP 403 when the supplied user (in the `Authorization` header) is not allowed to access this `processId`.

HTTP 404 when the supplied processId has no events.

# GET /processes/{processId}

Get all the details of all events for a given process. Warning: Might be a large result!

## parameters
* `processId` (in URL)
  **required**
  The unique identifier for a process. (As set by the `processId` parameter at the `/track` endpoint.)

* `timeFrom`
  **optional**
  A UNIX timestamp marking the point in time when events are included in the result.
  If missing, all events are included (as long as they are not filtered by another parameter)

* `timeUntil`
  **optional**
  A UNIX timestamp marking the point in time when events are no longer included in the result.
  If missing, all events are included (as long as they are not filtered by another parameter)

## response

HTTP 200 on a successful call, returns a json document with all tracked events Example:
```
[
  {
    "timestamp": 1576505586,
    "processId": "karlsruhe-mietpresrechner",
    "eventId": "before-filling-form",
    "processInstanceId": "15460988",
    // Next value is optional and might be missing in the result set:
    "userId": "userId:123"
  },
  {
    "timestamp": 1576505423,
    "processId": "konstanz-openair",
    "eventId": "before-showing-to-gemeinderat",
    "processInstanceId": "12344455",
  },
  {POSSIBLY_MORE_EVENTS}
}
```

HTTP 401 when no `Authorization` header was provided, or the username-password combination didn't match.

HTTP 403 when the supplied user (in the `Authorization` header) is not allowed to access this `processId`

HTTP 404 when the supplied processId has no events.
