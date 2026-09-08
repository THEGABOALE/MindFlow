const express = require("express");
const { getLevels } = require("../controllers/level.controller");
const { authenticate } = require("../middleware/auth.middleware");

const router = express.Router();

router.get("/", authenticate, getLevels);

module.exports = router;
