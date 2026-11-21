# Restaurant Deals API

A Spring Boot API for querying active restaurant deals.

The project reads its sample restaurant and deal data from
`src/main/resources/challengedata.json`; it does not call the external data endpoint.

## API

```text
GET /api/deals?timeOfDay=3:00pm
```

`timeOfDay` accepts 12-hour values such as `3:00pm` and 24-hour values such as `15:00`.
The response contains a `deals` array. Invalid or missing times return `400 Bad Request`.

## Requirements

- Java 17 or newer
- Maven

## Run

```shell
mvn spring-boot:run
```

## Test

```shell
mvn test
```
