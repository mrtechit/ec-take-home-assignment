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
Restaurants must be open for a deal to be returned. Deal times default to the restaurant's
corresponding open or close time when missing, and closing times are exclusive.

Example request:

```shell
curl --get 'http://localhost:8080/api/deals' --data-urlencode 'timeOfDay=3:00pm'
```

The peak-time endpoint takes no parameters and returns the continuous window with
the highest number of simultaneously active deals:

```shell
curl 'http://localhost:8080/api/deals/peak-time'
```

For the bundled data, the response is `{"peakTimeStart":"18:00","peakTimeEnd":"21:00"}`.

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
