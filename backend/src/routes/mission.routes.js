const express = require("express");
const {
  getMissionContent,
  startAttempt,
  finishAttempt
} = require("../controllers/mission.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");

const router = express.Router();

// Va antes que /:missionId para que "attempts" no se lea como un id de mision.
router.post("/attempts/:attemptId/finish", authenticate, requireRole("student"), finishAttempt);

router.get("/:missionId", authenticate, getMissionContent);
router.post("/:missionId/attempts", authenticate, requireRole("student"), startAttempt);

module.exports = router;
