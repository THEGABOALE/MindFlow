const express = require("express");
const {getStudentContext} = require("../controllers/student.controller");

const router = express.Router();

router.get("/:studentId/context", getStudentContext);

module.exports = router;