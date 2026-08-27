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

router.post("/students", authenticate, requireRole("coordinator", "admin"), createIdAccount);

module.exports = router;
