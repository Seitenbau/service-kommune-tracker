# Service-Kommune Tracking Server

A [Ratpack](https://ratpack.io) REST Service to allow tracking of events in Service-BW / Amt24 processes.

Developed by [Service-Kommune](https://www.service-kommune.de), in cooperation with [#ANDI](https://www.netzwerk-agile-verwaltung.de/).

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
1. Setup a MariaDB database (in the examples, we name it `skTracker`)
1. Create the required tables (see `SkTrackerSpecification.setupTables()`, located at `src/test/groovy/SkTrackerSpecification.groovy`)
1. Set required environment variables:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/skTracker";
   export DB_USERNAME="skTrackingService";
   export DB_PASSWORD="<YOUR PASSWORD HERE>";
   export DB_DRIVER="org.mariadb.jdbc.Driver"
   ```
1. Run in development mode with auto-refresh:
   ```bash
   ./gradlew -t run
   ```
1. *Optional: You can also do this from within IntelliJ.*
   *Just create a new 'Gradle' run configuration, specify the `run` task, the `-t` argument for auto-refresh and set*
   *and set the environment variables from above.*
