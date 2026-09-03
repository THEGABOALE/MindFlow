const express = require("express");
const cors = require("cors");

const healthRoutes = require("./routes/health.routes");
const levelRoutes = require("./routes/level.routes");
const groupRoutes = require("./routes/group.routes");
const studentRoutes = require("./routes/student.routes");
const authRoutes = require("./routes/auth.routes");
const missionRoutes = require("./routes/mission.routes");
const teacherRoutes = require("./routes/teacher.routes");
const coordinatorRoutes = require("./routes/coordinator.routes");
const adminRoutes = require("./routes/admin.routes");

const app = express();

app.use(cors());
app.use(express.json());

app.use("/", healthRoutes);
app.use("/api/health", healthRoutes);
app.use("/api/levels", levelRoutes);
app.use("/api/groups", groupRoutes);
app.use("/api/students", studentRoutes);
app.use("/api/auth", authRoutes);
app.use("/api/missions", missionRoutes);
app.use("/api/teacher", teacherRoutes);
app.use("/api/coordinator", coordinatorRoutes);
app.use("/api/admin", adminRoutes);

module.exports = app;