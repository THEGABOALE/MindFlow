const express = require("express");
const {
    joinGroupByCode
} = require ("../controllers/group.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");
const { joinCodeAttempts } = require("../middleware/rate-limit.middleware");
const router = express.Router();

router.post("/join", authenticate, requireRole("student"), joinCodeAttempts, joinGroupByCode);
module.exports = router;
