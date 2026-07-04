FROM mysql:8.0

# Optional: you can drop init scripts here.
# They run on first container start (when /var/lib/mysql is empty).
COPY init/ /docker-entrypoint-initdb.d/
