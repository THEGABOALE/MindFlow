const healthCheck = (req, res) => {
  res.json({
    message: "API de MindFlow funcionando correctamente",
    status: "OK",
    service: "MindFlow Backend"
  });
};

module.exports = {
  healthCheck
};