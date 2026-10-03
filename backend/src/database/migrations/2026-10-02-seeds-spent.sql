-- Semillas que el estudiante gastó en potenciadores durante el intento (hoy,
-- el "+30 s" de las misiones con reloj). Las semillas de su cuenta son
-- points_earned - seeds_spent. Se puede correr más de una vez.
ALTER TABLE mission_attempts ADD COLUMN IF NOT EXISTS seeds_spent INTEGER NOT NULL DEFAULT 0;
