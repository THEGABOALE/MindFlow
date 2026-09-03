-- =========================
-- DATOS INICIALES PARA DESARROLLO
-- =========================

-- student < teacher < coordinator (jefe de profesores del centro) < admin (MindFlow)
INSERT INTO roles (name) VALUES
('student'),
('teacher'),
('coordinator'),
('admin'),
('validator');

INSERT INTO users (full_name, email, password_hash, role_id)
VALUES
('Estudiante Demo', NULL, NULL, (SELECT id FROM roles WHERE name = 'student'));

-- Cuentas de prueba (login por ID, contraseñas en texto plano solo para dev):
--   garciaga / 1234, coordinador / coord2026, profedemo / profe2026

INSERT INTO users (full_name, login_id, password_hash, role_id)
VALUES
('Gabriela García', 'garciaga', '$2b$10$pP0jxzQU/ztVr6XUSwPOCusjAw.s6KC9pSW1slQ9BB0G2DnLQ3rU6', (SELECT id FROM roles WHERE name = 'student')),
('Coordinador Demo', 'coordinador', '$2b$10$iMnPpGfJXiFdNk1K4uzbaOtrOtbjd19tEnNgfOu8CNRS2Z4MUGE1q', (SELECT id FROM roles WHERE name = 'coordinator')),
('Profesor Demo', 'profedemo', '$2b$10$eouv/cWLBUMwE2uOrCKo5O9tkuVaGP/EYVQAkiAtOkg5vuXcHW1FS', (SELECT id FROM roles WHERE name = 'teacher'));

INSERT INTO educational_levels (name, code, description, order_index)
VALUES
('Primaria alta', 'PRIMARIA_ALTA', 'Contenido para estudiantes de tercero a sexto grado.', 1),
('Secundaria baja', 'SECUNDARIA_BAJA', 'Contenido para estudiantes de séptimo a noveno grado.', 2),
('Secundaria alta', 'SECUNDARIA_ALTA', 'Contenido para estudiantes de décimo y undécimo grado.', 3);

INSERT INTO missions (level_id, title, description, topic, order_index, points_reward, is_published)
VALUES
(1, 'Bienvenida a NOVA', 'Primera misión introductoria sobre derechos y dignidad.', 'Introducción', 1, 10, TRUE),
(1, 'Reconocer mis derechos', 'Misión sobre derechos fundamentales y respeto.', 'Derechos', 2, 15, TRUE),
(1, 'Decisiones con respeto', 'Caso práctico sobre toma de decisiones y dignidad.', 'Dignidad', 3, 20, TRUE),
(2, 'Convivencia y equidad', 'Misión introductoria para secundaria baja.', 'Equidad', 1, 15, TRUE),
(3, 'Análisis de casos', 'Misión de reflexión para secundaria alta.', 'Casos prácticos', 1, 20, TRUE);

INSERT INTO questions (mission_id, question_text, question_type, feedback, order_index, points)
VALUES
(1, '¿Qué busca enseñar NOVA?', 'multiple_choice', 'NOVA busca reforzar el aprendizaje sobre derechos, dignidad y respeto.', 1, 1),
(1, '¿Por qué es importante aprender sobre dignidad?', 'multiple_choice', 'La dignidad permite reconocer el valor de cada persona y promover relaciones de respeto.', 2, 1);

INSERT INTO answer_options (question_id, option_text, is_correct, feedback, order_index)
VALUES
(1, 'Derechos y dignidad de la mujer', TRUE, 'Correcto. Ese es el enfoque principal de NOVA.', 1),
(1, 'Solo matemáticas', FALSE, 'No. NOVA se enfoca en derechos, dignidad y prevención de violencia.', 2),
(1, 'Juegos sin contenido educativo', FALSE, 'No. NOVA usa juegos, pero con propósito educativo.', 3),

(2, 'Porque ayuda a reconocer el valor y respeto que merece cada persona', TRUE, 'Correcto. La dignidad se relaciona con el respeto y el valor humano.', 1),
(2, 'Porque solo sirve para ganar puntos', FALSE, 'No. Los puntos motivan, pero el objetivo es aprender.', 2),
(2, 'Porque no tiene relación con la convivencia', FALSE, 'No. La dignidad sí se relaciona con la convivencia.', 3);

INSERT INTO institution_types (name) VALUES
('public'),
('private');

INSERT INTO educational_centers (name, institution_type_id, department, municipality) VALUES
('Centro Educativo Demo', 1, 'Managua', 'Managua');

UPDATE users
SET center_id = (SELECT id FROM educational_centers WHERE name = 'Centro Educativo Demo')
WHERE login_id IN ('coordinador', 'profedemo', 'garciaga');

INSERT INTO class_groups (center_id, level_id, name, grade, section, school_year, teacher_id)
VALUES
(1, 1, 'Primaria alta A', '4to grado', 'A', 2026, (SELECT id FROM users WHERE login_id = 'profedemo'));

INSERT INTO student_group_enrollments (user_id, group_id)
VALUES
((SELECT id FROM users WHERE login_id = 'garciaga'), 1);

INSERT INTO group_access_codes (group_id, code, expires_at, max_uses)
VALUES
(1, 'NOVA123', CURRENT_TIMESTAMP + INTERVAL '5 minutes', 40);