const express = require("express");
const {
    healthCheck,
    databaseHealthCheck
} = require("../controllers/health.controller");

const router = express.Router();

router.get("/", healthCheck);
router.get("/db", databaseHealthCheck);

module.exports = router;