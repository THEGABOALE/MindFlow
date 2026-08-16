const express = require("express");
const cors = require("cors");

const healthRoutes = require("./routes/health.routes");
const levelRoutes = require("./routes/level.routes");

const app = express();

app.use(cors());
app.use(express.json());

app.use("/", healthRoutes);
app.use("/api/health", healthRoutes);
app.use("/api/levels", levelRoutes);

module.exports = app;