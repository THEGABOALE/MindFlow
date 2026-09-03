const express = require("express");
const {
  getMissionContent,
  startAttempt,
  finishAttempt
} = require("../controllers/mission.controller");
const { authenticate } = require("../middleware/auth.middleware");

const router = express.Router();

// Va antes que /:missionId para que "attempts" no se lea como un id de mision.
router.post("/attempts/:attemptId/finish", authenticate, finishAttempt);

router.get("/:missionId", authenticate, getMissionContent);
router.post("/:missionId/attempts", authenticate, startAttempt);

module.exports = router;
