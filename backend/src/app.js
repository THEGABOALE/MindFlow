const express = require("express");
const cors = require("cors");

const healthRoutes = require("./routes/health.routes");
const levelRoutes = require("./routes/level.routes");
const groupRoutes = require("./routes/group.routes");
const studentRoutes = require("./routes/student.routes");

const app = express();

app.use(cors());
app.use(express.json());

app.use("/", healthRoutes);
app.use("/api/health", healthRoutes);
app.use("/api/levels", levelRoutes);
app.use("/api/groups", groupRoutes);
app.use("/api/students", studentRoutes);
module.exports = app;