package com.mindflow.nova.ui.screens.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindflow.nova.AppServices
import com.mindflow.nova.data.mission.MissionContentResult
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.mission.RemoteMissionRepository
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.offline.AttemptRecorder
import com.mindflow.nova.data.offline.FinishedAttempt
import com.mindflow.nova.data.offline.LessonOutcome
import com.mindflow.nova.data.offline.OfflineAttemptRecorder
import com.mindflow.nova.data.remote.currentTzOffsetMinutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

internal const val EMPTY_MISSION_MESSAGE = "Esta misión todavía no tiene preguntas. Vuelve a intentarlo más tarde."

sealed class LessonState {
    /** Se está pidiendo el contenido. */
    object Loading : LessonState()

    data class Error(val message: String) : LessonState()

    /**
     * La lección está lista para jugarse. [attemptId] es el id del intento,
     * creado en el teléfono; [attemptNumber] sube con cada reintento: la
     * pantalla lo usa para volver a montarse desde cero.
     */
    data class Playing(
        val content: MissionContent,
        val attemptId: String,
        val attemptNumber: Int
    ) : LessonState()
}

/**
 * Coordina una lección: pide su contenido, crea el intento en el teléfono y lo
 * entrega al [AttemptRecorder] cuando la pantalla de la mecánica termina. No
 * hace falta conexión para jugar: el intento se califica y se guarda en el
 * teléfono, y se sube cuando se puede. Las pantallas solo dibujan y avisan.
 */
class LessonViewModel(
    private val repository: MissionRepository = RemoteMissionRepository(),
    private val recorder: AttemptRecorder = OfflineAttemptRecorder(
        local = AppServices.localStore,
        sync = AppServices.sync,
        currentUserId = { AppServices.session.currentUserId() },
        scheduleSync = { AppServices.scheduleSync() }
    ),
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
    private val tzOffset: () -> Int = ::currentTzOffsetMinutes
) : ViewModel() {

    private val _state = MutableStateFlow<LessonState>(LessonState.Loading)
    val state: StateFlow<LessonState> = _state.asStateFlow()

    private var missionId: Int? = null
    private var content: MissionContent? = null
    private var attemptNumber = 0
    private var startedAtMs = 0L

    /** Carga la misión y empieza su primer intento. Llamarlo de nuevo con la misma misión no hace nada. */
    fun open(missionId: Int) {
        if (this.missionId == missionId) return
        this.missionId = missionId
        content = null
        attemptNumber = 0
        _state.value = LessonState.Loading

        viewModelScope.launch {
            when (val result = repository.loadContent(missionId)) {
                is MissionContentResult.Failed -> _state.value = LessonState.Error(result.message)
                is MissionContentResult.Loaded -> if (!result.content.hasPlayableContent()) {
                    _state.value = LessonState.Error(EMPTY_MISSION_MESSAGE)
                } else {
                    content = result.content
                    startAttempt(result.content)
                }
            }
        }
    }

    /**
     * Empieza un intento nuevo de la misma misión (por ejemplo tras quedarse
     * sin plumas), con otro id y otra hora de inicio, sobre el mismo contenido.
     */
    fun retry() {
        val content = content ?: return
        startAttempt(content)
    }

    /** Guarda el intento en curso y dice cómo quedó: subido, pendiente, rechazado o sin guardar. */
    suspend fun finishAttempt(answers: List<AnswerSubmission>, timedOut: Boolean): LessonOutcome {
        val playing = _state.value as? LessonState.Playing ?: return LessonOutcome.SaveFailed

        return recorder.record(
            FinishedAttempt(
                clientAttemptId = playing.attemptId,
                content = playing.content,
                answers = answers,
                timedOut = timedOut,
                startedAtMs = startedAtMs,
                finishedAtMs = now(),
                tzOffsetMinutes = tzOffset()
            )
        )
    }

    private fun startAttempt(content: MissionContent) {
        attemptNumber += 1
        startedAtMs = now()
        _state.value = LessonState.Playing(content, newId(), attemptNumber)
    }
}
