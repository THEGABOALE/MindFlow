const express = require("express");
const {getStudentContext} = require("../controllers/student.controller");
const { authenticate } = require("../middleware/auth.middleware");

const router = express.Router();

router.get("/:studentId/context", authenticate, getStudentContext);

module.exports = router;