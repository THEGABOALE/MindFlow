const { OAuth2Client } = require("google-auth-library");
const env = require("../config/env");

const googleClient = new OAuth2Client(env.auth.googleClientId);

const isGoogleLoginConfigured = () => Boolean(env.auth.googleClientId);

/**
 * Valida con Google el idToken que manda la app y devuelve sus datos (correo,
 * si esta verificado, etc.). Lanza si el token no es valido o no fue emitido
 * para esta app.
 */
const verifyGoogleIdToken = async (idToken) => {
  const ticket = await googleClient.verifyIdToken({
    idToken,
    audience: env.auth.googleClientId
  });

  return ticket.getPayload();
};

module.exports = {
  isGoogleLoginConfigured,
  verifyGoogleIdToken
};
