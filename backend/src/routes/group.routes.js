const express = require("express");
const {
    joinGroupByCode
} = require ("../controllers/group.controller");
const router = express.Router();

router.post("/join", joinGroupByCode);
module.exports = router;