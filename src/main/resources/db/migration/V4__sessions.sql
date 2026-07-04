CREATE TABLE IF NOT EXISTS session_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  expires_at DATETIME NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE KEY uq_session_token (token),
  CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE
);
