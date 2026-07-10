#!/usr/bin/env sh
set -eu

# Simple docker-based dev runner (no docker-compose needed).
# Why? Because legacy docker-compose v1 + modern Docker Engine sometimes explode.

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if [ -f "$ROOT_DIR/.env" ]; then
  # shellcheck disable=SC1091
  . "$ROOT_DIR/.env"
fi

DB_NAME="${DB_NAME:-managant}"
DB_USER="${DB_USER:-managant}"
DB_PASSWORD="${DB_PASSWORD:-managant}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-root}"
MYSQL_PORT_HOST="${MYSQL_PORT_HOST:-3311}"
API_PORT_HOST="${API_PORT_HOST:-8080}"
GOOGLE_CLIENT_ID="${GOOGLE_CLIENT_ID:-}"
PHONE_ENCRYPTION_KEY="${PHONE_ENCRYPTION_KEY:-}"

NET="managant-net"
DB_CONTAINER="managant-backend-mysql"
API_CONTAINER="managant-backend-api"
DB_IMAGE="managant-backend-db"
API_IMAGE="managant-backend-api"
VOLUME="managant_backend_mysql_data"

# Optional: wipe the DB volume (useful when Flyway complains about checksum mismatches).
# Usage:
#   RESET_DB=1 ./scripts/dev-up.sh
RESET_DB="${RESET_DB:-0}"

echo "==> Building images..."

docker build -t "$DB_IMAGE" "$ROOT_DIR/db"
docker build -t "$API_IMAGE" "$ROOT_DIR"

echo "==> Ensuring network: $NET"
if ! docker network inspect "$NET" >/dev/null 2>&1; then
  docker network create "$NET" >/dev/null
fi

echo "==> Starting MySQL (port ${MYSQL_PORT_HOST}->3306)..."

if [ "$RESET_DB" = "1" ]; then
  echo "==> RESET_DB=1: removing volume: ${VOLUME} (bye data )"
  docker volume rm "$VOLUME" >/dev/null 2>&1 || true
fi

# Cleanup old container if it exists
if docker ps -a --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
  docker rm -f "$DB_CONTAINER" >/dev/null
fi

docker run -d \
  --name "$DB_CONTAINER" \
  --network "$NET" \
  -e MYSQL_DATABASE="$DB_NAME" \
  -e MYSQL_USER="$DB_USER" \
  -e MYSQL_PASSWORD="$DB_PASSWORD" \
  -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
  -p "${MYSQL_PORT_HOST}:3306" \
  -v "${VOLUME}:/var/lib/mysql" \
  "$DB_IMAGE" >/dev/null

echo "==> Waiting for MySQL to be ready..."
# MySQL image starts a *temporary* server during init, stops it, then starts the real one.
# So we require a few consecutive successful queries to avoid the "ping ok then connection refused" faceplant.

tries=90
consecutive=0
while [ $tries -gt 0 ]; do
  if docker exec "$DB_CONTAINER" mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT 1" >/dev/null 2>&1; then
    consecutive=$((consecutive + 1))
    if [ $consecutive -ge 3 ]; then
      break
    fi
  else
    consecutive=0
  fi

  tries=$((tries - 1))
  sleep 2
done

if [ $tries -le 0 ]; then
  echo "MySQL did not become healthy in time" >&2
  docker logs "$DB_CONTAINER" || true
  exit 1
fi

echo "==> Starting API (port ${API_PORT_HOST}->8080)..."
# Cleanup old container if it exists
if docker ps -a --format '{{.Names}}' | grep -q "^${API_CONTAINER}$"; then
  docker rm -f "$API_CONTAINER" >/dev/null
fi

docker run -d \
  --name "$API_CONTAINER" \
  --network "$NET" \
  -e DB_HOST="$DB_CONTAINER" \
  -e DB_PORT=3306 \
  -e DB_NAME="$DB_NAME" \
  -e DB_USER="$DB_USER" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e GOOGLE_CLIENT_ID="$GOOGLE_CLIENT_ID" \
  -e PHONE_ENCRYPTION_KEY="$PHONE_ENCRYPTION_KEY" \
  -p "${API_PORT_HOST}:8080" \
  "$API_IMAGE" >/dev/null

# Wait a bit so we don't lie to your face with "Done" while the API is already dead.
echo "==> Waiting for API to be ready..."
tries=60
while [ $tries -gt 0 ]; do
  # If the container is not running, fail fast and show logs.
  if [ "$(docker inspect -f '{{.State.Running}}' "$API_CONTAINER" 2>/dev/null || echo false)" != "true" ]; then
    echo "API container exited . Logs:" >&2
    docker logs --tail 200 "$API_CONTAINER" >&2 || true
    echo "" >&2
    echo "Common fix: RESET_DB=1 ./scripts/dev-up.sh  (wipes mysql volume so Flyway can re-migrate)" >&2
    exit 1
  fi

  # Best-effort healthcheck if curl exists.
  if command -v curl >/dev/null 2>&1; then
    if curl -fsS -m 2 "http://localhost:${API_PORT_HOST}/api/health" >/dev/null 2>&1; then
      break
    fi
  else
    # No curl? just give it some time.
    sleep 2
    tries=$((tries - 1))
    continue
  fi

  tries=$((tries - 1))
  sleep 1
done

if [ $tries -le 0 ]; then
  echo "API did not become healthy in time. Logs:" >&2
  docker logs --tail 200 "$API_CONTAINER" >&2 || true
  exit 1
fi

echo "==> Done. Try:"
echo "    curl http://localhost:${API_PORT_HOST}/api/health"
