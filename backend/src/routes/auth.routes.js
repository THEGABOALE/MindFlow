const express = require("express");
const {
  loginWithGoogle,
  loginWithId,
  getMe,
  createIdAccount
} = require("../controllers/auth.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");

const router = express.Router();

router.post("/login/google", loginWithGoogle);
router.post("/login/id", loginWithId);
router.get("/me", authenticate, getMe);

router.post("/students", authenticate, requireRole("coordinator", "admin"), createIdAccount);

module.exports = router;
