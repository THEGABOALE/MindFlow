package com.mindflow.nova.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindflow.nova.AppServices
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.mission.RemoteMissionRepository
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.RejectedNotice
import com.mindflow.nova.data.student.LevelsResult
import com.mindflow.nova.data.student.RemoteStudentRepository
import com.mindflow.nova.data.student.StudentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentHomeState(
    /** True mientras se pide la ruta de aprendizaje por primera vez. */
    val isLoading: Boolean = true,
    /** Por qué no se pudo cargar la ruta, o null si no hubo problema. */
    val errorMessage: String? = null,
    val levels: List<LevelResponse> = emptyList(),
    val user: SessionUser? = null,
    /** True si no se pudo saber quién es el estudiante (falló GET /me). */
    val accountFailed: Boolean = false,
    /** El último progreso del servidor más lo jugado que falta subir. */
    val progress: StudentProgress? = null,
    /** True si lo último que se pidió al servidor no llegó y se muestra lo guardado. */
    val offline: Boolean = false,
    /** Resultados jugados en el teléfono que todavía no se subieron. */
    val pendingCount: Int = 0,
    /** Aviso de resultados que el servidor no aceptó, hasta que se cierre. */
    val notice: String? = null
)

/**
 * Nivel que se le muestra al estudiante: el de su sala. Solo se llama cuando
 * ya se sabe quién es; si no tiene sala o su nivel no vino en la lista
 * devuelve null, para no mostrarle nunca el nivel de otro.
 */
internal fun levelForStudent(levels: List<LevelResponse>, user: SessionUser): LevelResponse? =
    levels.firstOrNull { it.id == user.group?.levelId }

/** El texto del aviso de rechazos, o null si no hay ninguno. */
internal fun rejectionNotice(notices: List<RejectedNotice>): String? = when (notices.size) {
    0 -> null
    1 -> "Un resultado no se pudo guardar: ${notices[0].message}"
    else -> "${notices.size} resultados no se pudieron guardar: ${notices[0].message}"
}

/**
 * Estado del área del estudiante. Inicio, Lecciones, Progreso y Perfil leen de
 * aquí en vez de pedir cada una lo mismo al backend: la cuenta y el progreso se
 * piden una vez y se comparten.
 *
 * El progreso se observa desde lo guardado en el teléfono, así que cambia solo
 * cuando llega uno nuevo del servidor o cuando se juega o sube un resultado.
 */
class StudentHomeViewModel(
    private val repository: StudentRepository = RemoteStudentRepository(),
    private val missions: MissionRepository = RemoteMissionRepository(),
    private val rejectedNotices: Flow<List<RejectedNotice>> = AppServices.sync.rejectedNotices,
    private val onNoticesSeen: () -> Unit = { AppServices.sync.consumeNotices() },
    private val networkAvailable: Flow<Boolean> = AppServices.networkAvailable
) : ViewModel() {

    private val _state = MutableStateFlow(StudentHomeState())
    val state: StateFlow<StudentHomeState> = _state.asStateFlow()

    private var started = false
    private var refreshJob: Job? = null
    private var observeJob: Job? = null
    private var observedUserId: Int? = null
    private var noticesJob: Job? = null
    private var networkJob: Job? = null
    /** Las misiones del nivel se descargan una vez, y solo si la ruta llegó por red. */
    private var prefetchDone = false
    private var levelsFromNetwork = false

    /** Carga todo la primera vez que se entra al área del estudiante; después no hace nada. */
    fun start() {
        if (started) return
        started = true

        if (noticesJob?.isActive != true) {
            noticesJob = viewModelScope.launch {
                rejectedNotices.collect { notices -> _state.update { it.copy(notice = rejectionNotice(notices)) } }
            }
        }

        // Al volver la red se vuelve a preguntar al servidor: si responde, se
        // quita "Sin conexión" sin esperar a salir de una lección.
        if (networkJob?.isActive != true) {
            networkJob = viewModelScope.launch {
                networkAvailable.collect { available ->
                    if (available && _state.value.offline) refreshProgress()
                }
            }
        }

        viewModelScope.launch {
            val result = repository.loadLevels()

            _state.update { current ->
                when (result) {
                    is LevelsResult.Loaded -> current.copy(
                        isLoading = false,
                        levels = result.levels,
                        offline = current.offline || result.fromCache
                    )
                    is LevelsResult.Failed -> current.copy(isLoading = false, errorMessage = result.message)
                }
            }

            levelsFromNetwork = result is LevelsResult.Loaded && !result.fromCache
            prefetchLevelMissions()
        }

        refreshProgress()
    }

    /** Quita el aviso de rechazos: ya lo vio. */
    fun dismissNotice() {
        onNoticesSeen()
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
            val user = _state.value.user ?: repository.loadCurrentUser()

            // Sin saber quién es no se sabe su nivel: la pantalla muestra el
            // error con "Reintentar" en vez de quedarse cargando para siempre.
            if (user == null) {
                _state.update { it.copy(accountFailed = true) }
                return@launch
            }

            _state.update { it.copy(user = user, accountFailed = false) }
            observe(user.id)
            prefetchLevelMissions()

            // Lo que llega queda guardado y se ve por observe(); acá solo
            // importa si hubo respuesta del servidor.
            val fresh = repository.loadProgress(user.id)
            _state.update { it.copy(offline = fresh == null) }
        }
    }

    private fun observe(userId: Int) {
        if (observedUserId == userId && observeJob?.isActive == true) return
        observeJob?.cancel()
        observedUserId = userId
        observeJob = viewModelScope.launch {
            combine(repository.observeProgress(userId), repository.observePendingCount(userId)) { progress, pending ->
                progress to pending
            }.collect { (progress, pending) ->
                // Si todavía no hay nada guardado se conserva lo que se tenía.
                _state.update { it.copy(progress = progress ?: it.progress, pendingCount = pending) }
            }
        }
    }

    /**
     * Baja en segundo plano las misiones del nivel del estudiante para que pueda
     * jugarlas sin conexión. Necesita la ruta (por red) y saber quién es.
     */
    private fun prefetchLevelMissions() {
        if (prefetchDone || !levelsFromNetwork) return
        val user = _state.value.user ?: return
        val level = levelForStudent(_state.value.levels, user) ?: return

        prefetchDone = true
        viewModelScope.launch {
            missions.prefetch(level.missions.filter { it.isPublished }.map { it.id })
        }
    }

    /** Vuelve a pedir lo que haya fallado (la ruta o la cuenta), desde el botón "Reintentar". */
    fun retry() {
        started = false
        _state.update { it.copy(isLoading = it.levels.isEmpty(), errorMessage = null, accountFailed = false) }
        start()
    }

    /**
     * Al cerrar sesión se olvida todo, para que la siguiente cuenta no vea
     * datos de la anterior. También se cancelan las peticiones en curso: una
     * respuesta que llegue tarde no debe escribir sobre el estado ya limpio.
     */
    fun clear() {
        viewModelScope.coroutineContext.cancelChildren()
        started = false
        observedUserId = null
        prefetchDone = false
        levelsFromNetwork = false
        _state.value = StudentHomeState()
    }
}
