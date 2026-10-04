# RideLink – IT3130 Group Assignment

RideLink is a made-up ride-sharing backend we built for IT3130 Application Development.
It has four Spring Boot microservices. Each one has its own MongoDB database, and they
talk to each other over REST. There's no frontend – everything is tested through
Swagger UI and Postman.

## Team

| Service | Folder | Port | Owner |
|---|---|---|---|
| Account | `account-service` | 8081 | Najan J.A.S (IT24102946) |
| Driver & Vehicle | `driver-vehicle-service` | 8082 | Edirisinghe E.A.I.H. (IT24103022) |
| Ride Management | `ride-service` | 8083 | Jayaweera P.A.M (IT24102760) |
| Fare & Payment | `fare-payment-service` | 8084 | Ganegoda P.H.M.P (IT24102715) |

## What you need

- Java 17 or newer
- MongoDB running on `localhost:27017`
- Postman (optional, for running the full workflow)

You don't need Maven installed – each service has its own Maven wrapper (`mvnw`).

## Setup

All services read their secrets from environment variables, so nothing secret is in this repo.
Copy `local.env.example` to `local.env` in the repo root and fill in your own values:

```
JWT_SECRET=any-random-text-at-least-32-characters-long
INTERNAL_SERVICE_KEY=any-shared-key
ADMIN_EMAIL=admin@ridelink.test
ADMIN_PASSWORD=choose-a-password
```

Use the **same values for all four services**, otherwise tokens from the Account service
won't be accepted by the others. `local.env` is in `.gitignore`, so it never gets committed.

## Running it

Start the services in this order (Ride goes last because it calls Driver and Fare):

1. Account – 8081
2. Driver & Vehicle – 8082
3. Fare & Payment – 8084
4. Ride Management – 8083

In IntelliJ, run each `*Application` class with `local.env` set as its environment file.
Or from a terminal, inside a service folder:

```
./mvnw spring-boot:run        (Windows: .\mvnw.cmd spring-boot:run)
```

## Trying it out

Each service has Swagger UI at:

- http://localhost:8081/swagger-ui.html – Account
- http://localhost:8082/swagger-ui.html – Driver & Vehicle
- http://localhost:8083/swagger-ui.html – Ride
- http://localhost:8084/swagger-ui.html – Fare & Payment

The easiest way to see everything working is the Postman collection in the `postman/` folder.
Import the collection and the environment, then run the whole collection. It registers users,
logs them in, puts a driver online, books a ride, completes it, pays and gets a receipt –
and then runs the negative cases (no token, wrong role, invalid status change, and so on).

**Sample test users** (created by the collection):

| Role | Email | Password |
|---|---|---|
| Passenger | nimal@gmail.com | nimal@123 |
| Driver | kasun@gmail.com | kasun@123 |

Places have to be from the supported list – call `GET /api/fares/places` to see them
(e.g. Colombo, Gampaha, Negombo, Kandy). A driver's service area and a ride's pickup must use
these names.

## Running the tests

Inside each service folder:

```
./mvnw test        (Windows: .\mvnw.cmd test)
```

The context test starts the app, so set the environment variables first (dummy values are fine).

## How we worked

- Each member built their service on their own branch (named by student ID).
- Everything was merged into `main` through pull requests, using merge commits so everyone's
  commit history stays visible.
- GitHub Actions (`.github/workflows/ci.yml`) builds and tests all four services on every push
  and pull request.
- The assessed version is tagged `v1.0.0`.