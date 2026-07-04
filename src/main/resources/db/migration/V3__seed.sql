-- Minimal seed to mirror current frontend expectations.

-- Roles
INSERT INTO role (id, name, scope, max_people, active)
VALUES
  (1, 'ADMIN', 'GLOBAL', 10, TRUE),
  (2, 'ASISTENTE', 'GLOBAL', 100000, TRUE)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  scope = VALUES(scope),
  max_people = VALUES(max_people),
  active = VALUES(active);

-- Person (Admin)
INSERT INTO person (id, first_names, last_name, mother_last_name, birth_date, curp, active)
VALUES (1, 'Admin', 'Principal', 'Root', '1990-01-01', 'PRROAD19900101', TRUE)
ON DUPLICATE KEY UPDATE
  first_names = VALUES(first_names),
  last_name = VALUES(last_name),
  mother_last_name = VALUES(mother_last_name),
  birth_date = VALUES(birth_date),
  active = VALUES(active);

-- Root area
INSERT INTO area (id, name, category_id, parent_area_id, responsible_person_id, helper_person_id, active)
VALUES (1, 'ORGANIZACIÓN', NULL, NULL, 1, NULL, TRUE)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  responsible_person_id = VALUES(responsible_person_id),
  helper_person_id = VALUES(helper_person_id),
  active = VALUES(active);

-- Allowed roles for root (root should be permissive by default)
INSERT IGNORE INTO area_allowed_role (area_id, role_id) VALUES (1, 1);
INSERT IGNORE INTO area_allowed_role (area_id, role_id) VALUES (1, 2);

-- Membership: admin person has ADMIN role in root area
INSERT IGNORE INTO person_membership (person_id, area_id, role_id) VALUES (1, 1, 1);

-- User account
INSERT INTO user_account (id, person_id, username, password)
VALUES (1, 1, 'admin', 'admin')
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  password = VALUES(password);
