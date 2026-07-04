-- Ensure root areas (parent_area_id is NULL) allow every active role.
-- This makes initial setup smoother: create role -> immediately assignable in ORGANIZACIÓN.

INSERT IGNORE INTO area_allowed_role (area_id, role_id)
SELECT a.id, r.id
FROM area a
JOIN role r
WHERE a.parent_area_id IS NULL
  AND a.active = TRUE
  AND r.active = TRUE;
