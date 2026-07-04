-- Allow disabling users without deleting them.

ALTER TABLE user_account
  ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
