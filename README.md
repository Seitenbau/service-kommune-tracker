# Service-Kommune Tracking Server

A [Ratpack](https://ratpack.io) REST Service to allow tracking of events in Service-BW / Amt24 processes.

Developed by [Service-Kommune](https://www.service-kommune.de), in cooperation with [#ANDI](https://www.netzwerk-agile-verwaltung.de/).

## Prerequisites
1. Setup a MariaDB database (in the examples, we name it `skTracker`)
1. Set required environment variables:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/skTracker";
   export DB_USERNAME="skTrackingService";
   export DB_PASSWORD="<YOUR PASSWORD HERE>";
   export DB_DRIVER="org.mariadb.jdbc.Driver"
   ```

## Running the application

### Via Docker
This assumes that you've already created a docker network called `sk-tracker` and there is a docker container in
this network running MariaDb with the network-name `mariadb`.
This also assumes this MariaDb has a database called `sktracker`, a user `sktracker` (and this user uses the password 
`sktracker`).

1. Pull a image from DockerHub (use the `master` tag for the `master` branch):
   ```bash
   docker pull dweberseitenbau/service-kommune-tracker:master
   ```
1. Run the image (replace 12415 with the port you want to use)
   ```bash
   docker run -d --net=sk-tracker --name=app -p 12415:5050 \
     --restart on-failure \
     -e DB_URL=jdbc:mysql://mariadb:3306/sktracker \
     -e DB_USERNAME=sktracker \
     -e DB_PASSWORD=sktracker \
     -e DB_DRIVER="org.mariadb.jdbc.Driver" \
     dweberseitenbau/service-kommune-tracker:master
   ```

## Development
This project contains all files needed for an instant setup with the [IntelliJ IDEA](https://www.jetbrains.com/idea/) IDE.
We suggest you simply clone the repository and import it via `File --> Open`.

### Testing
After that you should be able to verify everything by running the 'All Tests' run configuration.
Alternatively, you can use Gradle from the command line:

```bash
./gradlew test
```

### Running the application
1. Run in development mode with auto-refresh:
   ```bash
   ./gradlew -t run
   ```
1. *Optional: You can also do this from within IntelliJ.*
   *Just use the provided `Run with local test DB` run config. (Note that it*
   *assumes that the database username and password are both set to `skTracker`.*
   
### Building releases
1. This is super simply and happens all within gradle:
   ```bash
   ./gradlew clean test shadowJar
   ```
