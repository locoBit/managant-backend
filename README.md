# managant-backend

Java 21 + Spring Boot 3 + MySQL (Docker).

## Run (Docker)

### Option A (recommended): scripts (no compose drama)

```bash
cp .env.example .env
sh scripts/dev-up.sh
```

If the API container exits immediately and you see a Flyway error like:
`Migration checksum mismatch`, your local MySQL volume is carrying an old schema.
Nuke it and re-run:

```bash
RESET_DB=1 sh scripts/dev-up.sh
```

- API health: http://localhost:8080/api/health
- API index: http://localhost:8080/api
- Login: `POST http://localhost:8080/api/auth/login` (admin/admin)
- MySQL: localhost:3311 (default)

Stop:

```bash
sh scripts/dev-down.sh
```

### Option B: docker compose (needs compose v2 plugin)

```bash
cp .env.example .env
docker compose up --build
```

## Run (local dev)

Start MySQL with docker:

```bash
docker compose up db
```

Then run Spring:

```bash
./mvnw spring-boot:run
```

(We didn't add the Maven wrapper yet — add it if you want. Right now use `mvn`.)
