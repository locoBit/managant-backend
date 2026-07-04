-- Set the default admin login to a Gmail account.
-- Keep data (no destructive ops).

INSERT INTO user_account (id, person_id, username, password)
VALUES (1, 1, 'alan.herlo12@gmail.com', 'alan.herlo12@gmail.com')
ON DUPLICATE KEY UPDATE
  person_id = VALUES(person_id),
  username = VALUES(username),
  password = VALUES(password);
