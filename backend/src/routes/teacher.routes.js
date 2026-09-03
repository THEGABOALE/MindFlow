const express = require("express");
const { getMyStudents } = require("../controllers/teacher.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");

const router = express.Router();

router.get("/me/students", authenticate, requireRole("teacher"), getMyStudents);

module.exports = router;
