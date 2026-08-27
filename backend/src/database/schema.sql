-- =========================
-- LIMPIEZA DE TABLAS EXISTENTES
-- SOLO PARA DESARROLLO LOCAL
-- =========================

DROP TABLE IF EXISTS student_group_enrollments CASCADE;
DROP TABLE IF EXISTS group_access_codes CASCADE;
DROP TABLE IF EXISTS class_groups CASCADE;
DROP TABLE IF EXISTS educational_centers CASCADE;
DROP TABLE IF EXISTS institution_types CASCADE;
DROP TABLE IF EXISTS level_progress CASCADE;
DROP TABLE IF EXISTS attempt_answers CASCADE;
DROP TABLE IF EXISTS mission_attempts CASCADE;
DROP TABLE IF EXISTS answer_options CASCADE;
DROP TABLE IF EXISTS questions CASCADE;
DROP TABLE IF EXISTS missions CASCADE;
DROP TABLE IF EXISTS educational_levels CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- =========================
-- ROLES Y USUARIOS
-- =========================

CREATE TABLE roles (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  full_name VARCHAR(150) NOT NULL,
  email VARCHAR(150) UNIQUE,
  password_hash TEXT,
  role_id INTEGER NOT NULL REFERENCES roles(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE
);

-- =========================
-- CONTENIDO EDUCATIVO
-- =========================

CREATE TABLE educational_levels (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50) UNIQUE NOT NULL,
  description TEXT,
  order_index INTEGER NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE missions (
  id SERIAL PRIMARY KEY,
  level_id INTEGER NOT NULL REFERENCES educational_levels(id) ON DELETE CASCADE,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  topic VARCHAR(100),
  order_index INTEGER NOT NULL,
  points_reward INTEGER DEFAULT 0,
  is_published BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE questions (
  id SERIAL PRIMARY KEY,
  mission_id INTEGER NOT NULL REFERENCES missions(id) ON DELETE CASCADE,
  question_text TEXT NOT NULL,
  question_type VARCHAR(50) DEFAULT 'multiple_choice',
  feedback TEXT,
  order_index INTEGER NOT NULL,
  points INTEGER DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE answer_options (
  id SERIAL PRIMARY KEY,
  question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  option_text TEXT NOT NULL,
  is_correct BOOLEAN DEFAULT FALSE,
  feedback TEXT,
  order_index INTEGER NOT NULL
);

-- =========================
-- PROGRESO DEL ESTUDIANTE
-- =========================

CREATE TABLE mission_attempts (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  mission_id INTEGER NOT NULL REFERENCES missions(id) ON DELETE CASCADE,
  score INTEGER DEFAULT 0,
  correct_answers INTEGER DEFAULT 0,
  wrong_answers INTEGER DEFAULT 0,
  started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  finished_at TIMESTAMP,
  status VARCHAR(50) DEFAULT 'in_progress'
);

CREATE TABLE attempt_answers (
  id SERIAL PRIMARY KEY,
  attempt_id INTEGER NOT NULL REFERENCES mission_attempts(id) ON DELETE CASCADE,
  question_id INTEGER NOT NULL REFERENCES questions(id),
  selected_option_id INTEGER REFERENCES answer_options(id),
  is_correct BOOLEAN DEFAULT FALSE,
  answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE level_progress (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  level_id INTEGER NOT NULL REFERENCES educational_levels(id) ON DELETE CASCADE,
  progress_percentage NUMERIC(5,2) DEFAULT 0,
  status VARCHAR(50) DEFAULT 'locked',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, level_id)
);

-- =========================
-- CENTROS, GRUPOS Y CÓDIGOS DE ACCESO
-- =========================

CREATE TABLE institution_types (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE educational_centers (
  id SERIAL PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  institution_type_id INTEGER REFERENCES institution_types(id),
  department VARCHAR(100),
  municipality VARCHAR(100),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE class_groups (
  id SERIAL PRIMARY KEY,
  center_id INTEGER REFERENCES educational_centers(id),
  level_id INTEGER NOT NULL REFERENCES educational_levels(id),
  name VARCHAR(100) NOT NULL,
  grade VARCHAR(50) NOT NULL,
  section VARCHAR(20),
  school_year INTEGER NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE group_access_codes (
  id SERIAL PRIMARY KEY,
  group_id INTEGER NOT NULL REFERENCES class_groups(id) ON DELETE CASCADE,
  code VARCHAR(30) UNIQUE NOT NULL,
  expires_at TIMESTAMP,
  max_uses INTEGER,
  current_uses INTEGER DEFAULT 0,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student_group_enrollments (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  group_id INTEGER NOT NULL REFERENCES class_groups(id) ON DELETE CASCADE,
  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE,
  UNIQUE(user_id, group_id)
);