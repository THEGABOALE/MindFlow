const pool = require("../database/connection");
const levelRepository = require("../repositories/level.repository");

const getLevels = async (req, res) => {
    try {
        const rows = await levelRepository.findLevelsWithMissions(pool);

    const levelsMap = new Map();

    rows.forEach((row) => {
        if (!levelsMap.has(row.level_id)) {
            levelsMap.set(row.level_id, {
                id: row.level_id,
                name: row.level_name,
                code: row.level_code,
                description: row.level_description,
                orderIndex: row.level_order,
                missions: []
            });
        }
        if (row.mission_id) {
            levelsMap.get(row.level_id).missions.push({
                id: row.mission_id,
                title: row.mission_title,
                description: row.mission_description,
                topic: row.mission_topic,
                orderIndex: row.mission_order,
                pointsReward: row.points_reward,
                mechanic: row.mechanic,
                timeLimitSeconds: row.time_limit_seconds,
                maxPlumas: row.max_plumas,
                isPublished: row.is_published
            });
        }
    })
    res.json(Array.from(levelsMap.values()));
    }catch (error) {
        res.status(500).json({
            message: "Error al obtener los niveles educativos",
            status: "ERROR",
            error: error.message
        });
    }
};

module.exports = {
  getLevels
};