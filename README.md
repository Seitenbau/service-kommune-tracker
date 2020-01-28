# Service-Kommune Tracking Server

A Ratpack REST Service to allow tracking of Service-BW / Amt24 events in processes

## Setup for development
1. Setup a MariaDB database (in the examples, we use `skTracker`)
1. Create the required tables (see `DatabaseHelper.setupTables()`, located at `src/test/groovy/DatabaseHelper.groovy`)
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

## Run tests
   ```bash
   ./gradlew test
   ```
