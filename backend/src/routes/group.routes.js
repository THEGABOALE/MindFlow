const express = require("express");
const {
    joinGroupByCode
} = require ("../controllers/group.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");
const router = express.Router();

router.post("/join", authenticate, requireRole("student"), joinGroupByCode);
module.exports = router;
