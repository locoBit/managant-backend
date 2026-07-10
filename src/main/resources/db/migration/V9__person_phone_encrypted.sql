-- Add encrypted phone number for people.
-- We store ciphertext as base64 text produced by the backend (AES-GCM with random IV).

ALTER TABLE person
  ADD COLUMN phone_encrypted VARCHAR(512) NULL;
