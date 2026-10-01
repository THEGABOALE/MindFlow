package com.mindflow.nova.ui.screens.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindflow.nova.data.mission.AttemptStartResult
import com.mindflow.nova.data.mission.MissionContentResult
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.mission.RemoteMissionRepository
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.MissionContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal const val EMPTY_MISSION_MESSAGE = "Esta misión todavía no tiene preguntas. Vuelve a intentarlo más tarde."

sealed class LessonState {
    /** Se está pidiendo el contenido o abriendo un intento. */
    object Loading : LessonState()

    data class Error(val message: String) : LessonState()

    /**
     * La lección está lista para jugarse. [attemptNumber] sube con cada
     * reintento: la pantalla lo usa para volver a montarse desde cero.
     */
    data class Playing(
        val content: MissionContent,
        val attemptId: Int,
        val attemptNumber: Int
    ) : LessonState()
}

/**
 * Coordina una lección: pide su contenido, abre el intento en el backend y lo
 * cierra cuando la pantalla de la mecánica termina. Las pantallas solo dibujan
 * y avisan; no llaman a la API.
 */
class LessonViewModel(
    private val repository: MissionRepository = RemoteMissionRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<LessonState>(LessonState.Loading)
    val state: StateFlow<LessonState> = _state.asStateFlow()

    private var missionId: Int? = null
    private var content: MissionContent? = null
    private var attemptNumber = 0

    /** Carga la misión y abre su primer intento. Llamarlo de nuevo con la misma misión no hace nada. */
    fun open(missionId: Int) {
        if (this.missionId == missionId) return
        this.missionId = missionId
        content = null
        attemptNumber = 0
        _state.value = LessonState.Loading

        viewModelScope.launch {
            when (val result = repository.loadContent(missionId)) {
                is MissionContentResult.Failed -> _state.value = LessonState.Error(result.message)
                // Sin preguntas no se abre el intento: quedaría abierto en la base
                // sin que el estudiante pueda jugar nada.
                is MissionContentResult.Loaded -> if (!result.content.hasPlayableContent()) {
                    _state.value = LessonState.Error(EMPTY_MISSION_MESSAGE)
                } else {
                    content = result.content
                    startAttempt(missionId, result.content)
                }
            }
        }
    }

    /**
     * Empieza un intento nuevo de la misma misión (por ejemplo tras quedarse
     * sin plumas). El contenido ya cargado se reutiliza; solo se abre otro intento.
     */
    fun retry() {
        val missionId = missionId ?: return
        val content = content ?: return
        _state.value = LessonState.Loading

        viewModelScope.launch { startAttempt(missionId, content) }
    }

    /** Cierra el intento en curso. Devuelve el resultado corregido, o null si no se pudo guardar. */
    suspend fun finishAttempt(answers: List<AnswerSubmission>, timedOut: Boolean): AttemptResult? {
        val playing = _state.value as? LessonState.Playing ?: return null

        return repository.finishAttempt(playing.attemptId, answers, timedOut)
    }

    private suspend fun startAttempt(missionId: Int, content: MissionContent) {
        _state.value = when (val result = repository.startAttempt(missionId)) {
            is AttemptStartResult.Failed -> LessonState.Error(result.message)
            is AttemptStartResult.Started -> {
                attemptNumber += 1
                LessonState.Playing(content, result.attemptId, attemptNumber)
            }
        }
    }
}
