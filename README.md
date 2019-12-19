# Service-Kommune Tracking Server

A simple Ratpack REST Service to allow tracking of SBW events

## Setup for development
1. Set required environment variables:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/skTracker";
   export DB_USERNAME="skTrackingService";
   export DB_PASSWORD="<YOUR PASSWORD HERE>";
   ```
1. Run in development mode with auto-refresh:
   ```bash
   ./gradlew -t run
   ```
