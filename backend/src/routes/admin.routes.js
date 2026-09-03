const express = require("express");
const { getGlobalOverview, getCenterOverview, listUsers } = require("../controllers/admin.controller");
const { authenticate, requireRole } = require("../middleware/auth.middleware");

const router = express.Router();

router.get("/overview", authenticate, requireRole("admin"), getGlobalOverview);
router.get("/centers/:centerId/overview", authenticate, requireRole("admin"), getCenterOverview);
router.get("/users", authenticate, requireRole("admin"), listUsers);

module.exports = router;
