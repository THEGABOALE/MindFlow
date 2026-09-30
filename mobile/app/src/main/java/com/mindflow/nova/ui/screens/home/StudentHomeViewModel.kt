package com.mindflow.nova.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.student.LevelsResult
import com.mindflow.nova.data.student.RemoteStudentRepository
import com.mindflow.nova.data.student.StudentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentHomeState(
    /** True mientras se pide la ruta de aprendizaje por primera vez. */
    val isLoading: Boolean = true,
    /** Por qué no se pudo cargar la ruta, o null si no hubo problema. */
    val errorMessage: String? = null,
    val levels: List<LevelResponse> = emptyList(),
    val user: SessionUser? = null,
    val progress: StudentProgress? = null
)

/**
 * Estado del área del estudiante. Inicio, Lecciones, Progreso y Perfil leen de
 * aquí en vez de pedir cada una lo mismo al backend: la cuenta y el progreso se
 * piden una vez y se comparten.
 */
class StudentHomeViewModel(
    private val repository: StudentRepository = RemoteStudentRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(StudentHomeState())
    val state: StateFlow<StudentHomeState> = _state.asStateFlow()

    private var started = false
    private var refreshJob: Job? = null

    /** Carga todo la primera vez que se entra al área del estudiante; después no hace nada. */
    fun start() {
        if (started) return
        started = true

        viewModelScope.launch {
            val result = repository.loadLevels()

            _state.update { current ->
                when (result) {
                    is LevelsResult.Loaded -> current.copy(isLoading = false, levels = result.levels)
                    is LevelsResult.Failed -> current.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }

        refreshProgress()
    }

    /**
     * Vuelve a pedir el progreso, por ejemplo al salir de una lección, para
     * que la ruta refleje la misión recién completada. Si falla, se conserva
     * el progreso que ya se tenía.
     *
     * Si ya había una actualización en curso se cancela: si no, una respuesta
     * vieja que llegue tarde podría pisar a la nueva y mostrar como pendiente
     * una misión recién completada.
     */
    fun refreshProgress() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val user = _state.value.user ?: repository.loadCurrentUser() ?: return@launch
            _state.update { it.copy(user = user) }

            val progress = repository.loadProgress(user.id) ?: return@launch
            _state.update { it.copy(progress = progress) }
        }
    }

    /**
     * Al cerrar sesión se olvida todo, para que la siguiente cuenta no vea
     * datos de la anterior. También se cancelan las peticiones en curso: una
     * respuesta que llegue tarde no debe escribir sobre el estado ya limpio.
     */
    fun clear() {
        viewModelScope.coroutineContext.cancelChildren()
        started = false
        _state.value = StudentHomeState()
    }
}
