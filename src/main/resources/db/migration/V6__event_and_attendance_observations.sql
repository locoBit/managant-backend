-- Add observations/notes fields for event attendance.
-- Keep existing data.

ALTER TABLE event_item
  ADD COLUMN observations TEXT NULL;

ALTER TABLE attendance
  ADD COLUMN observations TEXT NULL;
