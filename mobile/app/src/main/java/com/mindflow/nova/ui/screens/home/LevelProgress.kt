package com.mindflow.nova.ui.screens.home

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.StudentProgress

/**
 * Porcentaje del nivel que se muestra. El servidor lo calcula al guardar cada
 * intento (misiones publicadas completadas sobre las publicadas), pero lo
 * jugado sin conexión o subido en segundo plano todavía no está en ese número.
 * Se calcula igual con lo que se sabe en el teléfono y se usa el mayor: en el
 * servidor el avance de un nivel nunca baja.
 */
internal fun levelProgressPercentage(level: LevelResponse, progress: StudentProgress?): Double {
    val fromServer = progress?.levels?.firstOrNull { it.id == level.id }?.progressPercentage ?: 0.0
    val published = level.missions.filter { it.isPublished }
    if (published.isEmpty()) return fromServer

    val completed = progress?.completedMissionIds?.toSet().orEmpty()
    val local = published.count { it.id in completed } * 100.0 / published.size

    return maxOf(fromServer, local)
}
