-- Keep it tiny for now: just ensure DB is reachable and Flyway is alive.
-- We'll evolve schema with more migrations (YAGNI).

CREATE TABLE IF NOT EXISTS flyway_probe (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
