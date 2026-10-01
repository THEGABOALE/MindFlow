const express = require("express");
const { syncAttempts } = require("../controllers/sync.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");

const router = express.Router();

router.post("/attempts", authenticate, requireRole("student"), syncAttempts);

module.exports = router;
