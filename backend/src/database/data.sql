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

INSERT INTO missions (level_id, title, description, topic, order_index, points_reward, mechanic, time_limit_seconds, is_published)
VALUES
(1, 'Bienvenida a NOVA', 'Primera misión introductoria sobre derechos y dignidad.', 'Introducción', 1, 100, 'multiple_choice', NULL, TRUE),
(1, 'Reconocer mis derechos', 'Misión sobre derechos fundamentales y respeto.', 'Derechos', 2, 150, 'matching', 45, TRUE),
(1, 'Decisiones con respeto', 'Caso práctico sobre toma de decisiones y dignidad.', 'Dignidad', 3, 200, 'true_false', NULL, TRUE),
(2, 'Convivencia y equidad', 'Misión introductoria para secundaria baja.', 'Equidad', 1, 250, 'multiple_choice', NULL, TRUE),
(3, 'Análisis de casos', 'Misión de reflexión para secundaria alta.', 'Casos prácticos', 1, 300, 'multiple_choice', NULL, TRUE);

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

-- Misión 2 "Reconocer mis derechos": relación de conceptos.
-- Todo el minijuego es una sola pregunta; los 9 pares van en question_pairs.
INSERT INTO questions (mission_id, question_text, question_type, feedback, order_index, points)
VALUES
(2, 'Relacioná cada concepto con su significado', 'matching', 'Cada concepto se relaciona con la idea que lo define.', 1, 9);

INSERT INTO question_pairs (question_id, term, match_text, order_index)
VALUES
(3, 'Igualdad', 'Mismos derechos', 1),
(3, 'Dignidad', 'Respeto', 2),
(3, 'Equidad', 'Justicia', 3),
(3, 'Empoderamiento', 'Autonomía', 4),
(3, 'Discriminación', 'Exclusión', 5),
(3, 'Violencia', 'Daño', 6),
(3, 'Denuncia', 'Protección', 7),
(3, 'Consentimiento', 'Voluntad', 8),
(3, 'Diversidad', 'Inclusión', 9);

-- Misión 3 "Decisiones con respeto": verdadero/falso.
-- La explicación de cada afirmación va en questions.feedback.
INSERT INTO questions (mission_id, question_text, question_type, feedback, order_index, points)
VALUES
(3, 'La equidad de género en el hogar significa que las niñas deben encargarse prioritariamente de las tareas domésticas, mientras que los niños deben enfocarse únicamente en sus estudios.', 'true_false', 'Las cartillas del MINED enfatizan que la educación en valores promueve la igualdad y la corresponsabilidad. Tanto niños como niñas tienen los mismos derechos a estudiar y la misma responsabilidad de participar en las tareas de la casa.', 1, 1),
(3, 'Los celos extremos, la revisión del teléfono móvil y el control sobre la ropa de la pareja son manifestaciones de afecto y no se consideran formas de violencia.', 'true_false', 'La cartilla identifica el control obsesivo y el aislamiento como signos claros de violencia psicológica y emocional. Estas conductas vulneran la autonomía de la mujer y constituyen señales de alerta temprana ante posibles agresiones físicas.', 2, 1),
(3, 'Cualquier mujer o familiar que identifique una situación de riesgo por violencia de género puede solicitar ayuda y realizar la denuncia a través de la Comisaría de la Mujer o llamando a la línea 118.', 'true_false', 'La Policía Nacional, mediante las Comisarías de la Mujer y la línea de emergencia 118, forma parte de la red de respuesta institucional inmediata para garantizar la protección integral y la atención a las víctimas.', 3, 1),
(3, 'Las Consejerías de las Comunidades Educativas tienen como objetivo principal aplicar medidas disciplinarias y sancionar con expulsión a las estudiantes afectadas por problemas familiares.', 'true_false', 'La función de las Consejerías Educativas es estrictamente preventiva, de acompañamiento, escucha activa y detección temprana de situaciones de vulnerabilidad pedagógica y emocional para brindar apoyo a la comunidad estudiantil.', 4, 1),
(3, 'Toda mujer tiene derecho a tomar sus propias decisiones en cuanto a su educación, trabajo y proyecto de vida sin requerir la aprobación o permiso de su pareja.', 'true_false', 'Las cartillas promueven el principio de dignidad e independencia individual, resaltando que la mujer es un sujeto pleno de derechos con capacidad y libertad de desarrollarse en cualquier ámbito personal, académico o laboral.', 5, 1),
(3, 'El femicidio es una forma extrema de violencia que puede prevenirse si se identifican y denuncian a tiempo las agresiones psicológicas, verbales y físicas.', 'true_false', 'Las cartillas destacan que la violencia suele escalar. Identificar conductas tempranas como amenazas, insultos o chantajes permite activar los mecanismos de protección antes de que ocurra un desenlace fatal.', 6, 1),
(3, 'La responsabilidad del cuidado, alimentación y desarrollo emocional de las hijas e hijos recae de forma exclusiva en la madre.', 'true_false', 'Las cartillas promueven una paternidad responsable donde ambos progenitores asumen de manera equitativa la crianza, el afecto y los cuidados necesarios para el desarrollo integral de los hijos.', 7, 1),
(3, 'Los derechos de las mujeres solo deben respetarse dentro del entorno familiar y pierden vigencia en los espacios de trabajo, escuelas o comunidades.', 'true_false', 'Los derechos humanos de las mujeres son universales e inalienables. Las cartillas remarcan que deben respetarse y garantizarse en todos los espacios sin excepción: el hogar, la escuela, el centro laboral y la comunidad.', 8, 1);

INSERT INTO answer_options (question_id, option_text, is_correct, feedback, order_index)
VALUES
(4, 'Verdadero', FALSE, NULL, 1),
(4, 'Falso', TRUE, NULL, 2),
(5, 'Verdadero', FALSE, NULL, 1),
(5, 'Falso', TRUE, NULL, 2),
(6, 'Verdadero', TRUE, NULL, 1),
(6, 'Falso', FALSE, NULL, 2),
(7, 'Verdadero', FALSE, NULL, 1),
(7, 'Falso', TRUE, NULL, 2),
(8, 'Verdadero', TRUE, NULL, 1),
(8, 'Falso', FALSE, NULL, 2),
(9, 'Verdadero', TRUE, NULL, 1),
(9, 'Falso', FALSE, NULL, 2),
(10, 'Verdadero', FALSE, NULL, 1),
(10, 'Falso', TRUE, NULL, 2),
(11, 'Verdadero', FALSE, NULL, 1),
(11, 'Falso', TRUE, NULL, 2);

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