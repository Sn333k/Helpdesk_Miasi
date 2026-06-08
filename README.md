# Helpdesk_Miasi

Multi-module Maven project (Java 21) structured with Hexagonal Architecture and no Spring Boot.

## Modules
- `helpdesk-context`: Core domain + application services + ports/adapters.
- `users-context`: Supporting domain + application services + ports/adapters.
- `app-runner`: Composition root that wires dependencies and starts the HTTP server (`com.miasi.runner.Main`).

## Configuration
Each module has its own `src/main/resources/application.properties`. The runner merges all `application.properties` files on the classpath, so keep keys module-prefixed (e.g., `helpdesk.*`, `users.*`, `server.*`, `db.*`).

## Handy commands
```bash
mvn spotless:check  ## formatter, use :apply if needed

mvn clean install
mvn -pl :app-runner exec:java -Dexec.mainClass="com.miasi.runner.Main"  ## run the app
mvn -pl :app-runner -am package
```
