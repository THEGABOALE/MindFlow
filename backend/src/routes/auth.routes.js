const express = require("express");
const {
  loginWithGoogle,
  loginWithId,
  createIdAccount
} = require("../controllers/auth.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");

const router = express.Router();

router.post("/login/google", loginWithGoogle);
router.post("/login/id", loginWithId);

// Crear cuentas de ID+contraseña es tarea del coordinador de la institución.
// El admin (equipo de MindFlow) también puede, por soporte.
router.post("/students", authenticate, requireRole("coordinator", "admin"), createIdAccount);

module.exports = router;
