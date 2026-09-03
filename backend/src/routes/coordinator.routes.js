const express = require("express");
const { getCenterOverview } = require("../controllers/coordinator.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");

const router = express.Router();

router.get("/me/overview", authenticate, requireRole("coordinator"), getCenterOverview);

module.exports = router;
