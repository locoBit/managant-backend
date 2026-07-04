-- Core schema for Managant (minimal to match current frontend fakeApi).
-- Keep it boring: explicit ids for seed rows so we can reference them.

CREATE TABLE IF NOT EXISTS role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  scope VARCHAR(16) NOT NULL,
  max_people INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE KEY uq_role_name_scope (name, scope)
);

CREATE TABLE IF NOT EXISTS person (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  first_names VARCHAR(120) NOT NULL,
  last_name VARCHAR(120) NOT NULL,
  mother_last_name VARCHAR(120) NOT NULL DEFAULT '',
  birth_date DATE NOT NULL,
  curp VARCHAR(32) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE KEY uq_person_curp (curp)
);

CREATE TABLE IF NOT EXISTS area_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  UNIQUE KEY uq_area_category_name (name)
);

CREATE TABLE IF NOT EXISTS area (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  category_id BIGINT NULL,
  parent_area_id BIGINT NULL,
  responsible_person_id BIGINT NOT NULL,
  helper_person_id BIGINT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE KEY uq_area_name (name),
  CONSTRAINT fk_area_category FOREIGN KEY (category_id) REFERENCES area_category(id),
  CONSTRAINT fk_area_parent FOREIGN KEY (parent_area_id) REFERENCES area(id),
  CONSTRAINT fk_area_responsible FOREIGN KEY (responsible_person_id) REFERENCES person(id),
  CONSTRAINT fk_area_helper FOREIGN KEY (helper_person_id) REFERENCES person(id)
);

CREATE TABLE IF NOT EXISTS area_allowed_role (
  area_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (area_id, role_id),
  CONSTRAINT fk_area_allowed_role_area FOREIGN KEY (area_id) REFERENCES area(id) ON DELETE CASCADE,
  CONSTRAINT fk_area_allowed_role_role FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE IF NOT EXISTS user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  person_id BIGINT NOT NULL,
  username VARCHAR(120) NOT NULL,
  password VARCHAR(255) NOT NULL,
  UNIQUE KEY uq_user_username (username),
  UNIQUE KEY uq_user_person (person_id),
  CONSTRAINT fk_user_person FOREIGN KEY (person_id) REFERENCES person(id)
);

CREATE TABLE IF NOT EXISTS person_membership (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  person_id BIGINT NOT NULL,
  area_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uq_membership (person_id, area_id, role_id),
  CONSTRAINT fk_membership_person FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,
  CONSTRAINT fk_membership_area FOREIGN KEY (area_id) REFERENCES area(id) ON DELETE CASCADE,
  CONSTRAINT fk_membership_role FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE IF NOT EXISTS event_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(180) NOT NULL,
  area_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_by_person_id BIGINT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_event_area FOREIGN KEY (area_id) REFERENCES area(id),
  CONSTRAINT fk_event_creator FOREIGN KEY (created_by_person_id) REFERENCES person(id)
);

CREATE TABLE IF NOT EXISTS attendance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id BIGINT NOT NULL,
  person_id BIGINT NOT NULL,
  attended_at DATETIME NOT NULL,
  UNIQUE KEY uq_attendance_event_person (event_id, person_id),
  CONSTRAINT fk_attendance_event FOREIGN KEY (event_id) REFERENCES event_item(id) ON DELETE CASCADE,
  CONSTRAINT fk_attendance_person FOREIGN KEY (person_id) REFERENCES person(id)
);
