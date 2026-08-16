const express = require("express");
const { getLevels } = require("../controllers/level.controller");

const router = express.Router();

router.get("/", getLevels);

module.exports = router;