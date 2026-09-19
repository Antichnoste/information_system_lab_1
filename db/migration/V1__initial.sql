CREATE TABLE coordinates (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY CHECK (id > 0),
 version BIGINT NOT NULL DEFAULT 0,
 x BIGINT NOT NULL,
 y REAL NOT NULL CHECK (y > -629 AND y < 'Infinity'::real)
);
CREATE TABLE car (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY CHECK (id > 0),
 version BIGINT NOT NULL DEFAULT 0,
 name TEXT,
 cool BOOLEAN NOT NULL,
 color TEXT
);
CREATE TABLE human_being (
 id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY CHECK (id > 0),
 version BIGINT NOT NULL DEFAULT 0,
 name TEXT NOT NULL CHECK (length(name) > 0),
 coordinates_id BIGINT NOT NULL REFERENCES coordinates(id),
 creation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 real_hero BOOLEAN NOT NULL,
 has_toothpick BOOLEAN NOT NULL,
 car_id BIGINT REFERENCES car(id),
 mood VARCHAR(32) CHECK (mood IN ('SORROW','LONGING','RAGE','FRENZY')),
 impact_speed BIGINT NOT NULL,
 soundtrack_name TEXT NOT NULL,
 minutes_of_waiting BIGINT,
 weapon_type VARCHAR(32) NOT NULL CHECK (weapon_type IN ('HAMMER','PISTOL','SHOTGUN','RIFLE'))
);
CREATE INDEX human_coordinates_idx ON human_being(coordinates_id);
CREATE INDEX human_car_idx ON human_being(car_id);
CREATE INDEX human_waiting_idx ON human_being(minutes_of_waiting);
CREATE TABLE app_user (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 username VARCHAR(100) NOT NULL UNIQUE,
 password_hash VARCHAR(512) NOT NULL
);
-- One transaction lock serializes mutations across server instances.
CREATE TABLE write_guard (id INTEGER PRIMARY KEY);
INSERT INTO write_guard VALUES (1);
