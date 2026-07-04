#!/usr/bin/env sh
set -eu

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

DB_CONTAINER="managant-backend-mysql"
API_CONTAINER="managant-backend-api"
NET="managant-net"
VOLUME="managant_backend_mysql_data"

if docker ps -a --format '{{.Names}}' | grep -q "^${API_CONTAINER}$"; then
  docker rm -f "$API_CONTAINER" >/dev/null
fi

if docker ps -a --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
  docker rm -f "$DB_CONTAINER" >/dev/null
fi

# Keep volume by default (so you don't nuke your db by accident)
# Uncomment if you really want a clean slate:
# docker volume rm "$VOLUME" >/dev/null 2>&1 || true

docker network rm "$NET" >/dev/null 2>&1 || true

echo "Stopped containers. Volume preserved: $VOLUME"
