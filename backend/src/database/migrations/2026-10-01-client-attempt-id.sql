-- Modo offline: cada intento jugado en el teléfono trae su propio id (UUID),
-- para que subir dos veces el mismo intento no lo guarde dos veces.
-- Se puede correr más de una vez sin problema.
ALTER TABLE mission_attempts ADD COLUMN IF NOT EXISTS client_attempt_id UUID;

CREATE UNIQUE INDEX IF NOT EXISTS mission_attempts_client_attempt_id_key
  ON mission_attempts (client_attempt_id);
