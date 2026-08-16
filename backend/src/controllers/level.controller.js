const pool = require("../database/connection");

const getLevels = async (req, res) => {
    try {
        const result = await pool.query(`
      SELECT
        el.id AS level_id,
        el.name AS level_name,
        el.code AS level_code,
        el.description AS level_description,
        el.order_index AS level_order,
        m.id AS mission_id,
        m.title AS mission_title,
        m.description AS mission_description,
        m.topic AS mission_topic,
        m.order_index AS mission_order,
        m.points_reward,
        m.is_published
      FROM educational_levels el
      LEFT JOIN missions m 
        ON m.level_id = el.id 
        AND m.is_published = TRUE
      ORDER BY el.order_index ASC, m.order_index ASC;
    `);
    
    const levelsMap = new Map();

    result.rows.forEach((row) => {
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