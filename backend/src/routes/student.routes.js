const express = require("express");
const {getStudentContext, getStudentProgress} = require("../controllers/student.controller");
const { authenticate } = require("../middleware/auth.middleware");

const router = express.Router();

router.get("/:studentId/context", authenticate, getStudentContext);
router.get("/:studentId/progress", authenticate, getStudentProgress);

module.exports = router;
